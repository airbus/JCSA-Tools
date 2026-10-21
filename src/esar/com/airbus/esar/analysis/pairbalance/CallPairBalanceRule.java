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

package com.airbus.esar.analysis.pairbalance;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;
import com.airbus.esar.analysis.callflow.ForwardStmtWorklist;
import com.airbus.esar.analysis.callflow.InvokeSignaturePattern;
import com.airbus.esar.analysis.callflow.TypedExceptionalSuccessors;
import com.airbus.esar.detect.AnalysisContextIf;
import com.airbus.esar.detect.BugReporterIf;
import com.airbus.esar.detect.ProgramVisitorIf;
import com.airbus.esar.detect.RuleMessageRef;
import com.airbus.esar.detect.SarifResultLevel;

/**
 * Generic pair-balance rule driven by a CallPairBalanceSpec. For each open-call site in a method, we run a small
 * worklist from its successors and check whether the depth counter returns to zero on every reachable tail. If it
 * doesn't, we report the residual depth at that open.
 *
 * Callee effect is the max transaction depth at any normal exit, when the callee is entered with a given entry
 * depth, using the same transfer as the intraprocedural analysis. Memoized on (method signature, entry depth).
 *
 * The rule's message receives the residual depth as {0}; rules that want a static text can ignore it.
 */
public class CallPairBalanceRule implements ProgramVisitorIf {

    private final CallPairBalanceSpec mSpec;
    private final Map<CalleeDepthKey, Integer> mCalleeMaxCache = new HashMap<>();
    private final Map<MethodSignature, Set<ClassType>> mThrownCache = new HashMap<>();

    public CallPairBalanceRule(final CallPairBalanceSpec spec) {
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

    protected SarifResultLevel getSarifLevel() {
        return SarifResultLevel.WARNING;
    }

    public CallPairBalanceSpec getSpec() {
        return mSpec;
    }

    @Override
    public void endAnalysis(final AnalysisContextIf context, final BugReporterIf bugs) {
        mCalleeMaxCache.clear();
        mThrownCache.clear();
    }

    @Override
    public void visitStmt(final JavaSootMethod method,
                          final Body body,
                          final Stmt stmt,
                          final AnalysisContextIf context,
                          final BugReporterIf bugs) {
        if (!InvokeSignaturePattern.anyMatches(stmt, mSpec.getOpenCalls())) {
            return;
        }
        final JimpleBasedInterproceduralCFG icfg = context.getInterproceduralCfg();
        if (icfg == null) {
            return;
        }
        final int residual = analyseOpen(stmt, body.getStmtGraph(), context.getJavaView(), icfg);
        if (residual < 1) {
            return;
        }
        bugs.report(mSpec.getRuleId(), getSarifLevel(),
                RuleMessageRef.of(mSpec.getMessageId(), Integer.valueOf(residual)),
                method, stmt);
    }

    // Run the worklist from the open-call's successors. Depth is 1 on the normal edge and 0 on the exceptional
    // edge. Returns the largest residual depth at any tail.
    private int analyseOpen(final Stmt openStmt,
                            final StmtGraph<?> graph,
                            final JavaView view,
                            final JimpleBasedInterproceduralCFG icfg) {
        final int[] maxResidual = {0};
        final int cap = mSpec.getDepthCap();
        final Set<MethodSignature> onStack = new HashSet<>();
        final ForwardStmtWorklist.Transfer<Integer> transfer = createDepthTransfer(view, icfg, cap, onStack);
        ForwardStmtWorklist.runFromTrigger(graph, openStmt,
                ForwardStmtWorklist.TriggerSeeds.of(Integer.valueOf(1), Integer.valueOf(0)),
                transfer,
                new ForwardStmtWorklist.StateJoin<Integer>() {
                    @Override
                    public Integer joinIfImproves(final Integer existing, final Integer proposed) {
                        if (existing == null) {
                            return proposed;
                        }
                        return proposed.intValue() > existing.intValue() ? proposed : null;
                    }
                },
                (tail, after) -> {
                    if (after.intValue() > maxResidual[0]) {
                        maxResidual[0] = after.intValue();
                    }
                },
                ForwardStmtWorklist.DEFAULT_STEP_CAP);
        return maxResidual[0];
    }

    private ForwardStmtWorklist.Transfer<Integer> createDepthTransfer(final JavaView view,
                                                                       final JimpleBasedInterproceduralCFG icfg,
                                                                       final int cap,
                                                                       final Set<MethodSignature> onStack) {
        return new ForwardStmtWorklist.Transfer<Integer>() {
            @Override
            public Integer normal(final Stmt stmt, final Integer in) {
                return transferDepthStmt(stmt, in.intValue(), view, icfg, cap, onStack);
            }

            @Override
            public Integer exception(final Stmt stmt, final Integer in) {
                return in;
            }

            @Override
            public Map<ClassType, Stmt> exceptionalSuccessors(final StmtGraph<?> graph, final Stmt stmt) {
                return TypedExceptionalSuccessors.filter(graph, stmt, view, mThrownCache);
            }
        };
    }

    private static int applyDelta(final int current, final int delta, final int cap) {
        if (current == 0) {
            return 0;
        }
        final int next = current + delta;
        if (next < 0) {
            return 0;
        }
        if (next > cap) {
            return cap;
        }
        return next;
    }

    private int transferDepthStmt(final Stmt stmt,
                                  final int d,
                                  final JavaView view,
                                  final JimpleBasedInterproceduralCFG icfg,
                                  final int cap,
                                  final Set<MethodSignature> onStack) {
        if (InvokeSignaturePattern.anyMatches(stmt, mSpec.getOpenCalls())) {
            return applyDelta(d, 1, cap);
        }
        if (InvokeSignaturePattern.anyMatches(stmt, mSpec.getCloseCalls())) {
            return applyDelta(d, -1, cap);
        }
        if (!(stmt instanceof InvokableStmt invokable)) {
            return d;
        }
        final Optional<AbstractInvokeExpr> invOpt = invokable.getInvokeExpr();
        if (invOpt.isEmpty()) {
            return d;
        }
        return calleeMaxTailAtEntry(invokable, invOpt.get().getMethodSignature(), d, view, icfg, onStack);
    }

    // Max depth at any normal exit, when the callee is entered with entryDepth. ICFG dispatch maxes per target.
    private int calleeMaxTailAtEntry(final InvokableStmt callSite,
                                     final MethodSignature staticSig,
                                     final int entryDepth,
                                     final JavaView view,
                                     final JimpleBasedInterproceduralCFG icfg,
                                     final Set<MethodSignature> onStack) {
        final int cap = mSpec.getDepthCap();
        if (entryDepth < 0 || entryDepth > cap) {
            return entryDepth;
        }
        final Collection<SootMethod> callees = icfg.getCalleesOfCallAt(callSite);
        if (callees == null || callees.isEmpty()) {
            return runMethodBodyForMaxDepth(staticSig, entryDepth, view, icfg, cap, onStack);
        }
        int w = 0;
        for (final SootMethod cm : callees) {
            final int t = runMethodBodyForMaxDepth(cm.getSignature(), entryDepth, view, icfg, cap, onStack);
            if (t > w) {
                w = t;
            }
        }
        return w;
    }

    private int runMethodBodyForMaxDepth(final MethodSignature sig,
                                        final int entryDepth,
                                        final JavaView view,
                                        final JimpleBasedInterproceduralCFG icfg,
                                        final int cap,
                                        final Set<MethodSignature> onStack) {
        final CalleeDepthKey key = new CalleeDepthKey(sig, entryDepth);
        final Integer cacheHit = mCalleeMaxCache.get(key);
        if (cacheHit != null) {
            return cacheHit.intValue();
        }
        if (!onStack.add(sig)) {
            return entryDepth;
        }
        final int result;
        try {
            final Optional<JavaSootMethod> mo = view.getMethod(sig);
            if (mo.isEmpty() || !mo.get().hasBody()) {
                result = entryDepth;
            } else {
                final Body body = mo.get().getBody();
                final StmtGraph<?> graph = body.getStmtGraph();
                final Stmt entry = graph.getStartingStmt();
                if (entry == null) {
                    result = entryDepth;
                } else {
                    final int[] max = {0};
                    final int[] hasTail = {0};
                    final Map<Stmt, Integer> seeds = new HashMap<>();
                    seeds.put(entry, Integer.valueOf(entryDepth));
                    final ForwardStmtWorklist.Transfer<Integer> transfer = createDepthTransfer(
                            view, icfg, cap, onStack);
                    ForwardStmtWorklist.runFromSeeds(graph, seeds, transfer,
                            new ForwardStmtWorklist.StateJoin<Integer>() {
                                @Override
                                public Integer joinIfImproves(final Integer existing, final Integer proposed) {
                                    if (existing == null) {
                                        return proposed;
                                    }
                                    return proposed.intValue() > existing.intValue() ? proposed : null;
                                }
                            },
                            (tail, after) -> {
                                hasTail[0] = 1;
                                if (after.intValue() > max[0]) {
                                    max[0] = after.intValue();
                                }
                            },
                            ForwardStmtWorklist.DEFAULT_STEP_CAP,
                            null);
                    if (hasTail[0] == 0) {
                        result = entryDepth;
                    } else {
                        result = max[0];
                    }
                }
            }
        } finally {
            onStack.remove(sig);
        }
        mCalleeMaxCache.put(key, Integer.valueOf(result));
        return result;
    }

    private static final class CalleeDepthKey {
        private final MethodSignature mSig;
        private final int mEntry;

        private CalleeDepthKey(final MethodSignature sig, final int entry) {
            mSig = sig;
            mEntry = entry;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CalleeDepthKey)) {
                return false;
            }
            final CalleeDepthKey k = (CalleeDepthKey) o;
            return mEntry == k.mEntry && Objects.equals(mSig, k.mSig);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mSig, Integer.valueOf(mEntry));
        }
    }
}
