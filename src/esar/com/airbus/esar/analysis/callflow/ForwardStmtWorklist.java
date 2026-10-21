/*
 * Copyright (c) 2026 Airbus Defence and Space Oy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.airbus.esar.analysis.callflow;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.ClassType;

/**
 * Plain forward worklist over a StmtGraph. The lattice, transfer function, and join are caller-supplied.
 *
 * Normal and exceptional successors are propagated independently, so a transfer function can model "threw before
 * the effect was applied" (exception edge) versus "completed normally" (normal edge).
 *
 * The engine doesn't know about callees, summaries, or rules – those live in the transfer function.
 */
public final class ForwardStmtWorklist {
    // Guard against runaway analyses
    public static final int DEFAULT_STEP_CAP = 100_000;

    private static final Logger LOGGER = LoggerFactory.getLogger(ForwardStmtWorklist.class);

    private ForwardStmtWorklist() {
    }

    /**
     * Transfer function, split by edge kind. The "normal" result flows to the statement's normal successors;
     * the "exception" result flows to its exceptional successors.
     *
     * forSuccessor is called with the after-normal state (result of normal(pred, in)); the default
     * propagates that unchanged, except rules may narrow per out-edge to next (e.g. if branches).
     *
     * @param <S> lattice type
     */
    public interface Transfer<S> {
        S normal(Stmt stmt, S incoming);

        S exception(Stmt stmt, S incoming);

        default S forSuccessor(final Stmt pred, final S afterNormal, final Stmt next, final Body body) {
            return afterNormal;
        }

        default Map<ClassType, Stmt> exceptionalSuccessors(final StmtGraph<?> graph, final Stmt stmt) {
            return graph.exceptionalSuccessors(stmt);
        }
    }

    /**
     * Join / ascend check. Returns the joined value, or null when the existing state already subsumes the proposal
     * – which short-circuits the fixpoint so we don't re-enqueue the statement.
     *
     * @param <S> lattice type
     */
    public interface StateJoin<S> {
        S joinIfImproves(S existing, S proposed);
    }

    // Run the worklist with an explicit seed map. Every entry is treated as a known incoming state and enqueued.
    // onTail is called with the normal after-state at every CFG tail reachable from the seeds.
    // body: enclosing method (for if-branch fact filtering); null = forSuccessor falls back to normal.
    public static <S> Map<Stmt, S> runFromSeeds(final StmtGraph<?> graph,
                                        final Map<Stmt, S> initialIncoming,
                                        final Transfer<S> transfer,
                                        final StateJoin<S> join,
                                        final BiConsumer<Stmt, S> onTail,
                                        final int stepCap,
                                        final Body body) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(initialIncoming, "initialIncoming");
        Objects.requireNonNull(transfer, "transfer");
        Objects.requireNonNull(join, "join");
        Objects.requireNonNull(onTail, "onTail");

        final Map<Stmt, S> incoming = new HashMap<>();
        final Deque<Stmt> worklist = new ArrayDeque<>();
        for (final Map.Entry<Stmt, S> e : initialIncoming.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) {
                continue;
            }
            mergeAndEnqueue(e.getKey(), e.getValue(), incoming, worklist, join);
        }

        int steps = 0;
        while (!worklist.isEmpty()) {
            if (++steps > stepCap) {
                warnStepCapHit(stepCap, steps, worklist.size(), body);
                break;
            }
            final Stmt s = worklist.removeFirst();
            final S in = incoming.get(s);
            if (in == null) {
                continue;
            }
            final S afterNormal = transfer.normal(s, in);
            final S afterException = transfer.exception(s, in);

            final java.util.List<Stmt> succ = graph.successors(s);
            final Map<?, Stmt> excMap = transfer.exceptionalSuccessors(graph, s);

            if (succ.isEmpty() && excMap.isEmpty()) {
                if (afterNormal != null) {
                    onTail.accept(s, afterNormal);
                }
                continue;
            }
            if (afterNormal != null) {
                for (final Stmt t : succ) {
                    final S toSucc = transfer.forSuccessor(s, afterNormal, t, body);
                    mergeAndEnqueue(t, toSucc, incoming, worklist, join);
                }
            }
            if (afterException != null) {
                for (final Stmt t : excMap.values()) {
                    mergeAndEnqueue(t, afterException, incoming, worklist, join);
                }
            }
        }
        return incoming;
    }

    /**
     * Seed states for the trigger convenience overload: one value for the trigger's normal successors, one for its
     * exceptional successors. Either may be null to skip that edge.
     *
     * @param <S> lattice type
     */
    public static final class TriggerSeeds<S> {
        private final S mNormal;
        private final S mException;

        private TriggerSeeds(final S normal, final S exception) {
            mNormal = normal;
            mException = exception;
        }

        public static <S> TriggerSeeds<S> of(final S normal, final S exception) {
            return new TriggerSeeds<>(normal, exception);
        }

        public S getNormal() {
            return mNormal;
        }

        public S getException() {
            return mException;
        }
    }

    // Treats trigger as already executed and seeds its normal and exceptional successors directly. This is the
    // shape used by per-trigger rules, e.g. open/close pair balance.
    //
    // If trigger has no successors of any kind, onTail is called with the normal seed so the caller still sees
    // the after-state.
    public static <S> void runFromTrigger(final StmtGraph<?> graph,
                                          final Stmt trigger,
                                          final TriggerSeeds<S> seeds,
                                          final Transfer<S> transfer,
                                          final StateJoin<S> join,
                                          final BiConsumer<Stmt, S> onTail,
                                          final int stepCap) {
        Objects.requireNonNull(trigger, "trigger");
        Objects.requireNonNull(seeds, "seeds");
        final S normalSeed = seeds.getNormal();
        final S exceptionSeed = seeds.getException();
        final java.util.List<Stmt> succ = graph.successors(trigger);
        final Map<?, Stmt> excMap = transfer.exceptionalSuccessors(graph, trigger);
        if (succ.isEmpty() && excMap.isEmpty()) {
            if (normalSeed != null) {
                onTail.accept(trigger, normalSeed);
            }
            return;
        }
        final Map<Stmt, S> seedMap = new HashMap<>();
        if (normalSeed != null) {
            for (final Stmt t : succ) {
                seedMap.merge(t, normalSeed, (a, b) -> {
                    final S merged = join.joinIfImproves(a, b);
                    return merged == null ? a : merged;
                });
            }
        }
        if (exceptionSeed != null) {
            for (final Stmt t : excMap.values()) {
                seedMap.merge(t, exceptionSeed, (a, b) -> {
                    final S merged = join.joinIfImproves(a, b);
                    return merged == null ? a : merged;
                });
            }
        }
        runFromSeeds(graph, seedMap, transfer, join, onTail, stepCap, null);
    }

    private static void warnStepCapHit(final int stepCap,
                                       final int steps,
                                       final int queuedStmtCount,
                                       final Body body) {
        if (body == null) {
            LOGGER.warn("Forward stmt worklist hit step cap {} after {} steps; {} statements still queued",
                    stepCap, steps, queuedStmtCount);
            return;
        }
        LOGGER.warn(
                "Forward stmt worklist hit step cap {} after {} steps; {} statements still queued in method body "
                        + "with {} statements",
                stepCap,
                steps,
                queuedStmtCount,
                body.getStmts().size());
    }

    private static <S> void mergeAndEnqueue(final Stmt stmt,
                                            final S proposed,
                                            final Map<Stmt, S> incoming,
                                            final Deque<Stmt> worklist,
                                            final StateJoin<S> join) {
        final S existing = incoming.get(stmt);
        final S merged = join.joinIfImproves(existing, proposed);
        if (merged == null) {
            return;
        }
        incoming.put(stmt, merged);
        worklist.addLast(stmt);
    }
}
