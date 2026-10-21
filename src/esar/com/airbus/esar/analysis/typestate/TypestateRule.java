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

package com.airbus.esar.analysis.typestate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;
import com.airbus.esar.analysis.callflow.ForwardStmtWorklist;
import com.airbus.esar.detect.AnalysisContextIf;
import com.airbus.esar.detect.BugReporterIf;
import com.airbus.esar.detect.ProgramVisitorIf;
import com.airbus.esar.detect.RuleMessageRef;
import com.airbus.esar.detect.SarifResultLevel;

/**
 * Generic typestate rule. Analyses each entry-point method starting from the spec's initial state, flowing a set of
 * possible DFA states through the CFG. Violations are either reported at the offending call (strict mode) or at a
 * CFG tail when the tail state is not accepting.
 *
 * Callee effects come from memoized summaries keyed by (method signature, incoming state). Virtual dispatch is
 * resolved via the ICFG and the resulting summary sets are unioned (may-semantics). Recursion is broken by returning
 * the identity, which is a mild over-approximation but keeps us terminating.
 *
 * Caveat: states are not partitioned per receiver (no alias analysis). This fits single-object protocols such as
 * the APDU outgoing flow; it will over-report when two independent protocol objects are active in the same method.
 *
 * @param <S> state type, usually an enum
 */
public class TypestateRule<S> implements ProgramVisitorIf {
    private final TypestateSpec<S> mSpec;
    private final Map<SummaryKey<S>, Set<S>> mSummaryCache = new HashMap<>();

    public TypestateRule(final TypestateSpec<S> spec) {
        mSpec = spec;
    }

    @Override
    public String ruleId() {
        return mSpec.getRuleId();
    }

    @Override
    public SarifResultLevel severity() {
        return getSarifLevel();
    }

    public TypestateSpec<S> getSpec() {
        return mSpec;
    }

    protected SarifResultLevel getSarifLevel() {
        return SarifResultLevel.WARNING;
    }

    @Override
    public void endAnalysis(final AnalysisContextIf context, final BugReporterIf bugs) {
        mSummaryCache.clear();
    }

    @Override
    public void visitMethod(final JavaSootMethod method,
                            final Body body,
                            final AnalysisContextIf context,
                            final BugReporterIf bugs) {
        final JimpleBasedInterproceduralCFG icfg = context.getInterproceduralCfg();
        if (icfg == null) {
            return;
        }
        // Typestate analyses have to start from a known state. Helpers that are only ever invoked from non-initial
        // states would produce false positives if we ran them from the initial state. So we only analyse configured
        // entry points directly, and let the interprocedural summary mechanism carry state into their callees.
        if (!context.getEntryPoints().contains(method.getSignature())) {
            return;
        }
        analyseMethod(method, body, body.getStmtGraph(), context.getJavaView(), icfg, bugs);
    }

    private void analyseMethod(final JavaSootMethod method,
                               final Body body,
                               final StmtGraph<?> graph,
                               final JavaView view,
                               final JimpleBasedInterproceduralCFG icfg,
                               final BugReporterIf bugs) {
        final Stmt entry = graph.getStartingStmt();
        if (entry == null) {
            return;
        }
        final Set<TypestatePathBranching.PathFact<S>> init = new HashSet<>();
        init.add(new TypestatePathBranching.PathFact<>(mSpec.getInitialState(), Map.of()));
        final Map<Stmt, Set<TypestatePathBranching.PathFact<S>>> seeds = new HashMap<>();
        seeds.put(entry, init);

        final Set<MismatchReport<S>> mismatchesReported = new HashSet<>();
        final Set<TailReport<S>> tailsReported = new HashSet<>();

        final ForwardStmtWorklist.Transfer<Set<TypestatePathBranching.PathFact<S>>> transfer =
                new ForwardStmtWorklist.Transfer<Set<TypestatePathBranching.PathFact<S>>>() {
                    @Override
                    public Set<TypestatePathBranching.PathFact<S>> normal(final Stmt stmt,
                                            final Set<TypestatePathBranching.PathFact<S>> in) {
                        return computeAfterPathFact(stmt, in, view, icfg, method, bugs, mismatchesReported);
                    }

                    @Override
                    public Set<TypestatePathBranching.PathFact<S>> exception(
                            final Stmt stmt, final Set<TypestatePathBranching.PathFact<S>> in) {
                        return in;
                    }

                    @Override
                    public Set<TypestatePathBranching.PathFact<S>> forSuccessor(
                            final Stmt pred,
                            final Set<TypestatePathBranching.PathFact<S>> afterNormal,
                            final Stmt next,
                            final Body b) {
                        if (pred instanceof JIfStmt) {
                            return TypestatePathBranching.filterIfSuccessor(
                                    (JIfStmt) pred, next, afterNormal, b.getStmtGraph());
                        }
                        return afterNormal;
                    }
                };

        final ForwardStmtWorklist.StateJoin<Set<TypestatePathBranching.PathFact<S>>> join =
                new ForwardStmtWorklist.StateJoin<Set<TypestatePathBranching.PathFact<S>>>() {
                    @Override
                    public Set<TypestatePathBranching.PathFact<S>> joinIfImproves(
                            final Set<TypestatePathBranching.PathFact<S>> existing,
                            final Set<TypestatePathBranching.PathFact<S>> proposed) {
                        if (existing == null) {
                            return proposed;
                        }
                        if (existing.containsAll(proposed)) {
                            return null;
                        }
                        final Set<TypestatePathBranching.PathFact<S>> merged = new HashSet<>(existing);
                        merged.addAll(proposed);
                        return merged;
                    }
                };

        ForwardStmtWorklist.runFromSeeds(graph, seeds, transfer, join,
                (tail, after) -> reportTailFromFacts(method, tail, after, bugs, tailsReported),
                ForwardStmtWorklist.DEFAULT_STEP_CAP, body);
    }

    private Set<TypestatePathBranching.PathFact<S>> computeAfterPathFact(
            final Stmt stmt,
            final Set<TypestatePathBranching.PathFact<S>> in,
            final JavaView view,
            final JimpleBasedInterproceduralCFG icfg,
            final JavaSootMethod method,
            final BugReporterIf bugs,
            final Set<MismatchReport<S>> mismatchesReported) {
        final Set<TypestatePathBranching.PathFact<S>> out = new HashSet<>();
        if (mSpec.matchesAnyTrigger(stmt)) {
            for (final TypestatePathBranching.PathFact<S> f : in) {
                final Set<S> step = new HashSet<>();
                applyTriggerTransitions(stmt, f.getState(), step, method, bugs, mismatchesReported);
                for (final S s : step) {
                    out.add(new TypestatePathBranching.PathFact<>(s, f.getBools()));
                }
            }
            return out;
        }
        if (!(stmt instanceof InvokableStmt invokable)) {
            return in;
        }
        final Optional<AbstractInvokeExpr> invOpt = invokable.getInvokeExpr();
        if (invOpt.isEmpty()) {
            return in;
        }
        for (final TypestatePathBranching.PathFact<S> f : in) {
            for (final S s : summaryForInvoke(stmt, f.getState(), invOpt.get(), view, icfg)) {
                out.add(new TypestatePathBranching.PathFact<>(s, f.getBools()));
            }
        }
        return out;
    }

    private void reportTailFromFacts(final JavaSootMethod method,
                                     final Stmt tail,
                                     final Set<TypestatePathBranching.PathFact<S>> after,
                                     final BugReporterIf bugs,
                                     final Set<TailReport<S>> tailsReported) {
        for (final TypestatePathBranching.PathFact<S> f : after) {
            final S s = f.getState();
            if (mSpec.getAcceptingAtTail().contains(s)) {
                continue;
            }
            if (Objects.equals(s, mSpec.getErrorState())) {
                continue;
            }
            final TailReport<S> key = new TailReport<>(tail, s);
            if (!tailsReported.add(key)) {
                continue;
            }
            bugs.report(mSpec.getRuleId(), getSarifLevel(),
                    RuleMessageRef.of(mSpec.getTailMessageId(), String.valueOf(s)),
                    method, tail);
        }
    }

    private void applyTriggerTransitions(final Stmt stmt,
                                         final S state,
                                         final Set<S> out,
                                         final JavaSootMethod method,
                                         final BugReporterIf bugs,
                                         final Set<MismatchReport<S>> mismatchesReported) {
        if (Objects.equals(state, mSpec.getErrorState())) {
            out.add(state);
            return;
        }
        boolean anyApplied = false;
        for (final TypestateTransition<S> t : mSpec.getTransitions()) {
            if (!Objects.equals(t.getFrom(), state)) {
                continue;
            }
            if (!t.getPattern().matches(stmt)) {
                continue;
            }
            out.add(t.getTo());
            anyApplied = true;
        }
        if (anyApplied) {
            return;
        }
        if (mSpec.getMismatchPolicy() == MismatchPolicy.GO_TO_ERROR) {
            final MismatchReport<S> key = new MismatchReport<>(stmt, state);
            if (mismatchesReported.add(key)) {
                bugs.report(mSpec.getRuleId(), getSarifLevel(),
                        RuleMessageRef.of(mSpec.getMismatchMessageId(),
                                String.valueOf(state),
                                calleeName(stmt)),
                        method, stmt);
            }
            out.add(mSpec.getErrorState());
        } else {
            out.add(state);
        }
    }

    private Set<S> summaryForInvoke(final Stmt stmt,
                                    final S inState,
                                    final AbstractInvokeExpr expr,
                                    final JavaView view,
                                    final JimpleBasedInterproceduralCFG icfg) {
        final Collection<SootMethod> callees = icfg.getCalleesOfCallAt(stmt);
        final Set<S> union = new HashSet<>();
        if (callees == null || callees.isEmpty()) {
            union.addAll(summaryFor(expr.getMethodSignature(), inState, view, icfg));
            return union;
        }
        for (final SootMethod callee : callees) {
            union.addAll(summaryFor(callee.getSignature(), inState, view, icfg));
        }
        return union;
    }

    private Set<S> summaryFor(final MethodSignature sig,
                              final S inState,
                              final JavaView view,
                              final JimpleBasedInterproceduralCFG icfg) {
        final SummaryKey<S> key = new SummaryKey<>(sig, inState);
        final Set<S> memo = mSummaryCache.get(key);
        if (memo != null) {
            return memo;
        }
        return computeSummary(sig, inState, view, icfg, new HashSet<>());
    }

    private Set<S> computeSummary(final MethodSignature sig,
                                  final S inState,
                                  final JavaView view,
                                  final JimpleBasedInterproceduralCFG icfg,
                                  final Set<SummaryKey<S>> onStack) {
        final SummaryKey<S> key = new SummaryKey<>(sig, inState);
        final Set<S> cached = mSummaryCache.get(key);
        if (cached != null) {
            return cached;
        }
        if (!onStack.add(key)) {
            // Cycle: identity, a mild over-approximation that keeps us terminating.
            return Collections.singleton(inState);
        }
        final Optional<JavaSootMethod> mo = view.getMethod(sig);
        if (mo.isEmpty() || !mo.get().hasBody()) {
            onStack.remove(key);
            final Set<S> id = Collections.singleton(inState);
            mSummaryCache.put(key, id);
            return id;
        }
        final Body calleeBody = mo.get().getBody();
        final StmtGraph<?> graph = calleeBody.getStmtGraph();
        final Set<S> tails = new LinkedHashSet<>();
        final Stmt entry = graph.getStartingStmt();
        if (entry == null) {
            onStack.remove(key);
            mSummaryCache.put(key, Collections.singleton(inState));
            return Collections.singleton(inState);
        }
        final Set<S> inSet = Collections.singleton(inState);
        final Map<Stmt, Set<S>> seeds = new HashMap<>();
        seeds.put(entry, inSet);

        ForwardStmtWorklist.runFromSeeds(graph, seeds,
                new ForwardStmtWorklist.Transfer<Set<S>>() {
                    @Override
                    public Set<S> normal(final Stmt stmt, final Set<S> in) {
                        return computeSummaryAfter(stmt, in, view, icfg, onStack);
                    }

                    @Override
                    public Set<S> exception(final Stmt stmt, final Set<S> in) {
                        return in;
                    }
                },
                new ForwardStmtWorklist.StateJoin<Set<S>>() {
                    @Override
                    public Set<S> joinIfImproves(final Set<S> existing, final Set<S> proposed) {
                        if (existing == null) {
                            return proposed;
                        }
                        if (existing.containsAll(proposed)) {
                            return null;
                        }
                        final Set<S> merged = new HashSet<>(existing);
                        merged.addAll(proposed);
                        return merged;
                    }
                },
                (tail, after) -> tails.addAll(after),
                ForwardStmtWorklist.DEFAULT_STEP_CAP, calleeBody);
        onStack.remove(key);
        // If no tail is reachable (e.g. infinite loop) fall back to identity so the caller's state isn't swallowed.
        final Set<S> result = tails.isEmpty() ? Collections.singleton(inState) : Set.copyOf(tails);
        mSummaryCache.put(key, result);
        return result;
    }

    private Set<S> computeSummaryAfter(final Stmt stmt,
                                       final Set<S> in,
                                       final JavaView view,
                                       final JimpleBasedInterproceduralCFG icfg,
                                       final Set<SummaryKey<S>> onStack) {
        final Set<S> out = new HashSet<>();
        if (mSpec.matchesAnyTrigger(stmt)) {
            for (final S s : in) {
                applyTriggerTransitionsNoReport(stmt, s, out);
            }
            return out;
        }
        if (!(stmt instanceof InvokableStmt invokable)) {
            return in;
        }
        final Optional<AbstractInvokeExpr> invOpt = invokable.getInvokeExpr();
        if (invOpt.isEmpty()) {
            return in;
        }
        final Collection<SootMethod> callees = icfg.getCalleesOfCallAt(stmt);
        if (callees == null || callees.isEmpty()) {
            for (final S s : in) {
                out.addAll(computeSummary(invOpt.get().getMethodSignature(), s, view, icfg, onStack));
            }
            return out;
        }
        for (final S s : in) {
            for (final SootMethod callee : callees) {
                out.addAll(computeSummary(callee.getSignature(), s, view, icfg, onStack));
            }
        }
        return out;
    }

    private void applyTriggerTransitionsNoReport(final Stmt stmt,
                                                 final S state,
                                                 final Set<S> out) {
        if (Objects.equals(state, mSpec.getErrorState())) {
            out.add(state);
            return;
        }
        boolean anyApplied = false;
        for (final TypestateTransition<S> t : mSpec.getTransitions()) {
            if (!Objects.equals(t.getFrom(), state)) {
                continue;
            }
            if (!t.getPattern().matches(stmt)) {
                continue;
            }
            out.add(t.getTo());
            anyApplied = true;
        }
        if (anyApplied) {
            return;
        }
        if (mSpec.getMismatchPolicy() == MismatchPolicy.GO_TO_ERROR) {
            out.add(mSpec.getErrorState());
        } else {
            out.add(state);
        }
    }

    private static String calleeName(final Stmt stmt) {
        if (!(stmt instanceof InvokableStmt invokable)) {
            return "?";
        }
        final Optional<AbstractInvokeExpr> invOpt = invokable.getInvokeExpr();
        if (invOpt.isEmpty()) {
            return "?";
        }
        final MethodSignature sig = invOpt.get().getMethodSignature();
        return sig.getDeclClassType().getFullyQualifiedName() + "." + sig.getName();
    }

    private static final class SummaryKey<S> {
        private final MethodSignature mSig;
        private final S mInState;

        SummaryKey(final MethodSignature sig, final S inState) {
            mSig = sig;
            mInState = inState;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SummaryKey<?>)) {
                return false;
            }
            final SummaryKey<?> other = (SummaryKey<?>) o;
            return Objects.equals(mSig, other.mSig) && Objects.equals(mInState, other.mInState);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mSig, mInState);
        }
    }

    private static final class MismatchReport<S> {
        private final Stmt mStmt;
        private final S mState;

        MismatchReport(final Stmt stmt, final S state) {
            mStmt = stmt;
            mState = state;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MismatchReport<?>)) {
                return false;
            }
            final MismatchReport<?> other = (MismatchReport<?>) o;
            return mStmt == other.mStmt && Objects.equals(mState, other.mState);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(mStmt) * 31 + Objects.hashCode(mState);
        }
    }

    private static final class TailReport<S> {
        private final Stmt mStmt;
        private final S mState;

        TailReport(final Stmt stmt, final S state) {
            mStmt = stmt;
            mState = state;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TailReport<?>)) {
                return false;
            }
            final TailReport<?> other = (TailReport<?>) o;
            return mStmt == other.mStmt && Objects.equals(mState, other.mState);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(mStmt) * 31 + Objects.hashCode(mState);
        }
    }
}
