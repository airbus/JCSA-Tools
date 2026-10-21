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
import java.util.BitSet;
import java.util.Collection;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;

/**
 * Stmt sets that lie in natural loops of the normal (non-exceptional) CFG.
 *
 * Dominators are computed with the classic iterative dataflow algorithm using {@link BitSet}s
 * indexed by statement position (same result as set-based dominators, much cheaper for large CFGs).
 */
public final class NaturalLoopStmts {

    private NaturalLoopStmts() {
    }

    public static Set<Stmt> of(final Body body) {
        final StmtGraph<?> graph = body.getStmtGraph();
        final Stmt entry = graph.getStartingStmt();
        if (entry == null) {
            return Set.of();
        }
        final DomInfo dom = computeDominators(graph, entry);
        final Set<Stmt> inLoop = new LinkedHashSet<>();
        for (final Stmt tail : graph.getNodes()) {
            for (final Stmt header : graph.successors(tail)) {
                if (header.equals(tail)) {
                    continue;
                }
                if (strictlyDominates(dom, header, tail)) {
                    inLoop.addAll(naturalLoopBody(graph, header, tail));
                }
            }
        }
        return inLoop;
    }

    private static boolean strictlyDominates(final DomInfo dom, final Stmt header, final Stmt tail) {
        final Integer headerIdx = dom.mIndex.get(header);
        final Integer tailIdx = dom.mIndex.get(tail);
        if (headerIdx == null || tailIdx == null || headerIdx.equals(tailIdx)) {
            return false;
        }
        return dom.mDominators[tailIdx].get(headerIdx);
    }

    private static Set<Stmt> naturalLoopBody(final StmtGraph<?> graph,
                                             final Stmt header,
                                             final Stmt tail) {
        final Set<Stmt> loop = new LinkedHashSet<>();
        loop.add(header);
        final Deque<Stmt> pending = new ArrayDeque<>();
        pending.push(tail);
        while (!pending.isEmpty()) {
            final Stmt stmt = pending.pop();
            if (!loop.add(stmt)) {
                continue;
            }
            for (final Stmt pred : graph.predecessors(stmt)) {
                if (!loop.contains(pred)) {
                    pending.push(pred);
                }
            }
        }
        return loop;
    }

    private static DomInfo computeDominators(final StmtGraph<?> graph, final Stmt entry) {
        final Collection<Stmt> nodes = graph.getNodes();
        final int n = nodes.size();
        final Stmt[] stmts = new Stmt[n];
        final Map<Stmt, Integer> index = new IdentityHashMap<>(n);
        int i = 0;
        for (final Stmt node : nodes) {
            stmts[i] = node;
            index.put(node, i);
            i++;
        }

        final Integer entryIdxObj = index.get(entry);
        if (entryIdxObj == null) {
            return new DomInfo(index, new BitSet[0]);
        }
        final int entryIdx = entryIdxObj;

        final BitSet universe = new BitSet(n);
        universe.set(0, n);

        final BitSet[] dominators = new BitSet[n];
        for (int j = 0; j < n; j++) {
            dominators[j] = (BitSet) universe.clone();
        }
        final BitSet entryDom = new BitSet(n);
        entryDom.set(entryIdx);
        dominators[entryIdx] = entryDom;

        final BitSet next = new BitSet(n);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int nodeIdx = 0; nodeIdx < n; nodeIdx++) {
                if (nodeIdx == entryIdx) {
                    continue;
                }
                final List<Stmt> preds = graph.predecessors(stmts[nodeIdx]);
                next.clear();
                boolean havePred = false;
                for (final Stmt pred : preds) {
                    final Integer predIdx = index.get(pred);
                    if (predIdx == null) {
                        continue;
                    }
                    if (!havePred) {
                        next.or(dominators[predIdx]);
                        havePred = true;
                    } else {
                        next.and(dominators[predIdx]);
                    }
                }
                next.set(nodeIdx);
                if (!next.equals(dominators[nodeIdx])) {
                    dominators[nodeIdx] = (BitSet) next.clone();
                    changed = true;
                }
            }
        }
        return new DomInfo(index, dominators);
    }

    private static final class DomInfo {
        private final Map<Stmt, Integer> mIndex;
        private final BitSet[] mDominators;

        private DomInfo(final Map<Stmt, Integer> index, final BitSet[] dominators) {
            mIndex = index;
            mDominators = dominators;
        }
    }
}
