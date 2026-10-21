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

package com.airbus.esar.analysis.knownvalue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import sootup.callgraph.CallGraph;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaSootField;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;
import com.airbus.esar.detect.AnalysisContextIf;
import com.airbus.esar.soot.JimpleValues;

/**
 * Shared CFG backward analysis for compile-time known values. Leaf rules are supplied by {@link KnownValueSpec}.
 */
final class CompileTimeKnownValue {

    private enum MemoState {
        TRUE,
        FALSE,
        COMPUTING
    }

    private static final int MAX_INVOKE_DEPTH = 12;

    private static final class CallBinding {
        private final Stmt mCallSite;
        private final AbstractInvokeExpr mInvoke;
        private final Frame mCaller;

        private CallBinding(final Stmt callSite, final AbstractInvokeExpr invoke, final Frame caller) {
            mCallSite = callSite;
            mInvoke = invoke;
            mCaller = caller;
        }
    }

    private static final class TraversalState {
        private final Map<String, MemoState> mMemo;
        private final Set<String> mInvokeStack;
        private final int mInvokeDepth;
        private final Optional<CallBinding> mCallBinding;

        private TraversalState(final Map<String, MemoState> memo,
                               final Set<String> invokeStack,
                               final int invokeDepth,
                               final Optional<CallBinding> callBinding) {
            mMemo = memo;
            mInvokeStack = invokeStack;
            mInvokeDepth = invokeDepth;
            mCallBinding = callBinding;
        }

        private static TraversalState root() {
            return new TraversalState(new HashMap<>(), new HashSet<>(), 0, Optional.empty());
        }

        private TraversalState forCallee(final Set<String> invokeStack,
                                         final int invokeDepth,
                                         final CallBinding callBinding) {
            return new TraversalState(new HashMap<>(), invokeStack, invokeDepth, Optional.of(callBinding));
        }
    }

    private static final class Frame {
        private final Body mBody;
        private final StmtGraph<?> mGraph;
        private final AnalysisContextIf mAnalysisCtx;
        private final KnownValueSpec mSpec;
        private final TraversalState mState;

        private Frame(final Body body,
                      final StmtGraph<?> graph,
                      final AnalysisContextIf analysisCtx,
                      final KnownValueSpec spec,
                      final TraversalState state) {
            mBody = body;
            mGraph = graph;
            mAnalysisCtx = analysisCtx;
            mSpec = spec;
            mState = state;
        }
    }

    private CompileTimeKnownValue() {
    }

    static boolean isKnown(final Value value,
                           final Stmt atStmt,
                           final Body body,
                           final AnalysisContextIf ctx,
                           final KnownValueSpec spec) {
        final Value v = JimpleValues.unwrapCastChain(value);
        final StmtGraph<?> graph = body.getStmtGraph();
        final Frame f = new Frame(body, graph, ctx, spec, TraversalState.root());
        return knownValue(v, atStmt, f);
    }

    private static boolean knownValue(final Value value, final Stmt atStmt, final Frame f) {
        final Value v = JimpleValues.unwrapCastChain(value);
        if (f.mSpec.acceptsConstant(v)) {
            return true;
        }
        if (v instanceof JStaticFieldRef r) {
            return isStaticFinalField(r, f);
        }
        if (v instanceof Local) {
            return knownBefore((Local) v, atStmt, f);
        }
        if (v instanceof final AbstractInvokeExpr inv) {
            return invokeReturnsKnown(inv, atStmt, f);
        }
        return false;
    }

    private static boolean isStaticFinalField(final JStaticFieldRef ref, final Frame f) {
        final JavaView view = f.mAnalysisCtx.getJavaView();
        final Optional<JavaSootField> field = view.getField(ref.getFieldSignature());
        return field.filter(f.mSpec::acceptsStaticFinalField).isPresent();
    }

    private static boolean knownBefore(final Local l, final Stmt atStmt, final Frame f) {
        final String key = beforeKey(f.mBody, atStmt, l);
        final MemoState s = f.mState.mMemo.get(key);
        if (s == MemoState.TRUE) {
            return true;
        }
        if (s == MemoState.FALSE) {
            return false;
        }
        if (s == MemoState.COMPUTING) {
            return false;
        }
        f.mState.mMemo.put(key, MemoState.COMPUTING);
        final boolean out = knownBeforeInner(l, atStmt, f);
        f.mState.mMemo.put(key, out ? MemoState.TRUE : MemoState.FALSE);
        return out;
    }

    private static boolean knownBeforeInner(final Local l, final Stmt atStmt, final Frame f) {
        final List<Stmt> preds = f.mGraph.predecessors(atStmt);
        if (preds.isEmpty()) {
            return resolveCalleeParameterFromCallSite(l, f);
        }
        for (final Stmt p : preds) {
            if (p instanceof final JAssignStmt assign && localIs(assign.getLeftOp(), l)) {
                if (!knownValue(assign.getRightOp(), p, f)) {
                    return false;
                }
            } else if (!knownBefore(l, p, f)) {
                return false;
            }
        }
        return true;
    }

    private static boolean resolveCalleeParameterFromCallSite(final Local l, final Frame f) {
        final OptionalInt idx = parameterIndexOfLocal(l, f.mBody);
        if (idx.isEmpty()) {
            return false;
        }
        final Optional<CallBinding> binding = f.mState.mCallBinding;
        if (binding.isPresent()) {
            final CallBinding b = binding.get();
            final int i = idx.getAsInt();
            if (i < 0 || i >= b.mInvoke.getArgCount()) {
                return false;
            }
            return knownValue(b.mInvoke.getArg(i), b.mCallSite, b.mCaller);
        }
        return resolveParameterFromCallers(idx.getAsInt(), f);
    }

    /**
     * When analyzing a method directly (no call-site binding), require every call-graph caller to pass a
     * compile-time-known actual for parameter {@code paramIndex}.
     *
     * @param paramIndex callee parameter index (0-based, excluding {@code this})
     * @param f current analysis frame for the callee
     * @return true when every caller passes a known actual
     */
    private static boolean resolveParameterFromCallers(final int paramIndex, final Frame f) {
        if (f.mState.mInvokeDepth >= MAX_INVOKE_DEPTH) {
            return false;
        }
        final MethodSignature calleeSig = f.mBody.getMethodSignature();
        final String stackKey = "callers:" + calleeSig;
        if (!f.mState.mInvokeStack.add(stackKey)) {
            return false;
        }
        try {
            final CallGraph cg = f.mAnalysisCtx.getCallGraph();
            if (cg == null) {
                return false;
            }
            final Set<CallGraph.Call> calls = cg.callsTo(calleeSig);
            if (calls == null || calls.isEmpty()) {
                return false;
            }
            final JavaView view = f.mAnalysisCtx.getJavaView();
            boolean anyCaller = false;
            for (final CallGraph.Call call : calls) {
                final InvokableStmt callStmt = call.getInvokableStmt();
                final Optional<AbstractInvokeExpr> invOpt = callStmt.getInvokeExpr();
                if (invOpt.isEmpty()) {
                    return false;
                }
                final AbstractInvokeExpr inv = invOpt.get();
                if (paramIndex < 0 || paramIndex >= inv.getArgCount()) {
                    return false;
                }
                final Optional<JavaSootMethod> callerOpt = view.getMethod(call.getSourceMethodSignature());
                if (callerOpt.isEmpty() || !callerOpt.get().hasBody()) {
                    return false;
                }
                final JavaSootMethod callerMethod = callerOpt.get();
                final Body callerBody = callerMethod.getBody();
                final Set<String> stackCopy = new HashSet<>(f.mState.mInvokeStack);
                final Frame caller = new Frame(
                        callerBody,
                        callerBody.getStmtGraph(),
                        f.mAnalysisCtx,
                        f.mSpec,
                        new TraversalState(
                                new HashMap<>(),
                                stackCopy,
                                f.mState.mInvokeDepth + 1,
                                Optional.empty()));
                anyCaller = true;
                if (!knownValue(inv.getArg(paramIndex), callStmt, caller)) {
                    return false;
                }
            }
            return anyCaller;
        } finally {
            f.mState.mInvokeStack.remove(stackKey);
        }
    }

    private static OptionalInt parameterIndexOfLocal(final Local l, final Body body) {
        for (final Stmt st : body.getStmts()) {
            if (st instanceof final JIdentityStmt id && localIs(id.getLeftOp(), l)) {
                if (id.getRightOp() instanceof final JParameterRef pr) {
                    return OptionalInt.of(pr.getIndex());
                }
                return OptionalInt.empty();
            }
        }
        return OptionalInt.empty();
    }

    private static boolean invokeReturnsKnown(final AbstractInvokeExpr inv,
                                              final Stmt callSiteStmt,
                                              final Frame parent) {
        if (parent.mState.mInvokeDepth >= MAX_INVOKE_DEPTH) {
            return false;
        }
        final String stackKey = inv.getMethodSignature().toString();
        if (!parent.mState.mInvokeStack.add(stackKey)) {
            return false;
        }
        try {
            final Optional<JavaSootMethod> mOpt = parent.mAnalysisCtx.getJavaView().getMethod(inv.getMethodSignature());
            if (mOpt.isEmpty() || !mOpt.get().hasBody()) {
                return false;
            }
            final JavaSootMethod m = mOpt.get();
            if (inv.getArgCount() != m.getParameterCount()) {
                return false;
            }
            final Body calleeBody = m.getBody();
            final StmtGraph<?> calleeGraph = calleeBody.getStmtGraph();
            final Set<String> stackCopy = new HashSet<>(parent.mState.mInvokeStack);
            final Frame callee = new Frame(
                    calleeBody,
                    calleeGraph,
                    parent.mAnalysisCtx,
                    parent.mSpec,
                    parent.mState.forCallee(
                            stackCopy,
                            parent.mState.mInvokeDepth + 1,
                            new CallBinding(callSiteStmt, inv, parent)));
            boolean anyReturn = false;
            for (final Stmt st : calleeBody.getStmts()) {
                if (st instanceof final JReturnStmt ret) {
                    anyReturn = true;
                    if (!knownValue(ret.getOp(), st, callee)) {
                        return false;
                    }
                }
            }
            return anyReturn;
        } finally {
            parent.mState.mInvokeStack.remove(stackKey);
        }
    }

    private static String beforeKey(final Body body, final Stmt atStmt, final Local l) {
        return System.identityHashCode(body) + ":" + System.identityHashCode(atStmt) + ":" + l.getName();
    }

    private static boolean localIs(final Value lhs, final Local r) {
        return lhs instanceof Local && ((Local) lhs).getName().equals(r.getName());
    }
}
