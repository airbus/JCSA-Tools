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

package com.airbus.esar.analysis.primitivetaint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import heros.DefaultSeeds;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Identity;
import heros.flowfunc.KillAll;

import sootup.analysis.interprocedural.ifds.DefaultJimpleIFDSTabulationProblem;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;
import com.airbus.esar.soot.JimpleValues;

/**
 * IFDS reachability for configured primitive sources (static fields and Jimple constants), interprocedural.
 *
 * @param <L> label type carried on tracked facts
 */
public final class PrimitiveTaintIfdsProblem<L>
        extends DefaultJimpleIFDSTabulationProblem<TrackedPrimitiveFact<L>, InterproceduralCFG<Stmt, SootMethod>> {

    private final InterproceduralCFG<Stmt, SootMethod> mIcfg;
    private final List<Stmt> mEntryStarts;
    private final PrimitiveSourceTrackingSpec<L> mSpec;

    public PrimitiveTaintIfdsProblem(final InterproceduralCFG<Stmt, SootMethod> icfg,
                                     final JavaView view,
                                     final List<MethodSignature> entryPoints,
                                     final PrimitiveSourceTrackingSpec<L> spec) {
        super(icfg);
        mIcfg = icfg;
        mSpec = spec;
        mEntryStarts = collectStartingStmts(view, entryPoints);
    }

    private static List<Stmt> collectStartingStmts(final JavaView view, final List<MethodSignature> entryPoints) {
        final List<Stmt> starts = new ArrayList<>();
        for (final MethodSignature sig : entryPoints) {
            final Optional<JavaSootMethod> m = view.getMethod(sig);
            if (m.isPresent() && m.get().hasBody()) {
                starts.add(m.get().getBody().getStmtGraph().getStartingStmt());
            }
        }
        return starts.isEmpty() ? List.of() : List.copyOf(starts);
    }

    @Override
    public java.util.Map<Stmt, Set<TrackedPrimitiveFact<L>>> initialSeeds() {
        if (mEntryStarts.isEmpty()) {
            return Collections.emptyMap();
        }
        return DefaultSeeds.make(mEntryStarts, zeroValue());
    }

    @Override
    protected FlowFunctions<Stmt, TrackedPrimitiveFact<L>, SootMethod> createFlowFunctionsFactory() {
        return new FlowFunctions<Stmt, TrackedPrimitiveFact<L>, SootMethod>() {
            @Override
            public FlowFunction<TrackedPrimitiveFact<L>> getNormalFlowFunction(final Stmt curr, final Stmt succ) {
                return getNormalFlow(curr);
            }

            @Override
            public FlowFunction<TrackedPrimitiveFact<L>> getCallFlowFunction(final Stmt callStmt,
                                                                            final SootMethod destinationMethod) {
                return getCallFlow(callStmt.asInvokableStmt(), destinationMethod);
            }

            @Override
            public FlowFunction<TrackedPrimitiveFact<L>> getReturnFlowFunction(final Stmt callSite,
                                                                              final SootMethod calleeMethod,
                                                                              final Stmt exitStmt,
                                                                              final Stmt returnSite) {
                return getReturnFlow(callSite.asInvokableStmt(), calleeMethod, exitStmt, returnSite);
            }

            @Override
            public FlowFunction<TrackedPrimitiveFact<L>> getCallToReturnFlowFunction(final Stmt callSite,
                                                                                     final Stmt returnSite) {
                return getCallToReturnFlow(callSite.asInvokableStmt(), returnSite);
            }
        };
    }

    @Override
    protected TrackedPrimitiveFact<L> createZeroValue() {
        return TrackedPrimitiveFact.zero();
    }

    private FlowFunction<TrackedPrimitiveFact<L>> getNormalFlow(final Stmt curr) {
        if (!(curr instanceof JAssignStmt)) {
            final Set<TrackedPrimitiveFact<L>> invokedFacts = factsFromInvokableStmt(curr);
            return source -> {
                final Set<TrackedPrimitiveFact<L>> res = new HashSet<>(invokedFacts);
                res.add(source);
                return res;
            };
        }

        final JAssignStmt assign = (JAssignStmt) curr;
        final Value leftOp = assign.getLeftOp();
        final Value rightOp = JimpleValues.unwrapCastChain(assign.getRightOp());
        final Optional<L> rhsTag = factTagFromSource(rightOp);

        return source -> {
            // Kill overwritten facts (both locals and static fields) upon reassignment
            if (!source.isZero() && source.getCarrier().equivTo(leftOp)) {
                return Collections.emptySet();
            }
            final Set<TrackedPrimitiveFact<L>> res = new HashSet<>();
            res.add(source);

            if (!source.isZero() && leftOp instanceof Local && source.getCarrier().equivTo(rightOp)) {
                res.add(TrackedPrimitiveFact.of(source.getTag(), leftOp));
            }
            if (source.isZero() && rhsTag.isPresent() && leftOp instanceof Local) {
                res.add(TrackedPrimitiveFact.of(rhsTag.get(), leftOp));
            }
            return res;
        };
    }

    private Set<TrackedPrimitiveFact<L>> factsFromInvokableStmt(final Stmt stmt) {
        if (!(stmt instanceof InvokableStmt)) {
            return Collections.emptySet();
        }
        final InvokableStmt inv = (InvokableStmt) stmt;
        if (inv.getInvokeExpr().isEmpty()) {
            return Collections.emptySet();
        }
        return factsFromInvokeArgs(inv.getInvokeExpr().get());
    }

    private Set<TrackedPrimitiveFact<L>> factsFromInvokeArgs(final AbstractInvokeExpr ie) {
        final Set<TrackedPrimitiveFact<L>> out = new HashSet<>();
        for (final Immediate arg : ie.getArgs()) {
            final Value v = JimpleValues.unwrapCastChain(arg);
            factTagFromSource(v).ifPresent(t -> out.add(TrackedPrimitiveFact.of(t, v)));
        }
        return out;
    }

    private FlowFunction<TrackedPrimitiveFact<L>> getCallFlow(final InvokableStmt callStmt,
                                                              final SootMethod destinationMethod) {
        if ("<clinit>".equals(destinationMethod.getName())) {
            return KillAll.v();
        }
        if (!destinationMethod.hasBody()) {
            return Identity.v();
        }
        final AbstractInvokeExpr ie = callStmt.getInvokeExpr()
                .orElseThrow(() -> new IllegalStateException("Call statement has no invoke expression: " + callStmt));
        final List<Immediate> callArgs = ie.getArgs();
        final List<Value> paramLocals = new ArrayList<>();
        for (int i = 0; i < destinationMethod.getParameterCount(); i++) {
            paramLocals.add(destinationMethod.getBody().getParameterLocal(i));
        }

        return source -> {
            final Set<TrackedPrimitiveFact<L>> ret = new HashSet<>();
            if (source.isZero()) {
                ret.add(source);
                return ret;
            }
            if (source.getCarrier() instanceof JStaticFieldRef) {
                ret.add(source);
            }
            for (int i = 0; i < callArgs.size(); i++) {
                if (i < paramLocals.size() && passesArgumentToCallee(callArgs.get(i), source)) {
                    ret.add(TrackedPrimitiveFact.of(source.getTag(), paramLocals.get(i)));
                }
            }
            return ret;
        };
    }

    private FlowFunction<TrackedPrimitiveFact<L>> getReturnFlow(final InvokableStmt callSite,
                                                                final SootMethod calleeMethod,
                                                                final Stmt exitStmt,
                                                                final Stmt returnSite) {
        final AbstractInvokeExpr ie = callSite.getInvokeExpr()
                .orElseThrow(() -> new IllegalStateException("Return site has no invoke expression: " + callSite));
        final Value base = invokeBase(ie);

        if (exitStmt instanceof JReturnStmt) {
            final JReturnStmt returnStmt = (JReturnStmt) exitStmt;
            final Value retOp = JimpleValues.unwrapCastChain(returnStmt.getOp());
            final Optional<L> retTag = factTagFromSource(retOp);
            return source -> {
                final Set<TrackedPrimitiveFact<L>> ret = new HashSet<>();
                if (!source.isZero()) {
                    if (source.getCarrier() instanceof JStaticFieldRef) {
                        ret.add(source);
                    }
                    if (callSite instanceof AbstractDefinitionStmt && carrierMatchesReturn(source, retOp)) {
                        final AbstractDefinitionStmt defn = (AbstractDefinitionStmt) callSite;
                        ret.add(TrackedPrimitiveFact.of(source.getTag(), defn.getLeftOp()));
                    }
                    if (base != null && calleeMethod.hasBody()
                            && source.getCarrier() == calleeMethod.getBody().getThisLocal()) {
                        ret.add(TrackedPrimitiveFact.of(source.getTag(), base));
                    }
                } else {
                    ret.add(source);
                }
                if (retTag.isPresent() && callSite instanceof JAssignStmt) {
                    final Value leftOp = ((JAssignStmt) callSite).getLeftOp();
                    if (leftOp instanceof Local) {
                        ret.add(TrackedPrimitiveFact.of(retTag.get(), leftOp));
                    }
                }
                return ret;
            };
        }
        if (exitStmt instanceof JReturnVoidStmt) {
            return source -> {
                final Set<TrackedPrimitiveFact<L>> ret = new HashSet<>();
                if (source.isZero()) {
                    return ret;
                }
                if (source.getCarrier() instanceof JStaticFieldRef) {
                    ret.add(source);
                }
                if (base != null && calleeMethod.hasBody()
                        && source.getCarrier() == calleeMethod.getBody().getThisLocal()) {
                    ret.add(TrackedPrimitiveFact.of(source.getTag(), base));
                }
                return ret;
            };
        }
        // Exceptional exits (JThrowStmt): kill all facts — taint through thrown exceptions is not tracked.
        return KillAll.v();
    }

    private static boolean carrierMatchesReturn(final TrackedPrimitiveFact<?> source, final Value retOp) {
        if (source.isZero()) {
            return false;
        }
        return source.getCarrier().equivTo(retOp);
    }

    private FlowFunction<TrackedPrimitiveFact<L>> getCallToReturnFlow(final InvokableStmt callSite,
                                                                      final Stmt returnSite) {
        final AbstractInvokeExpr ie = callSite.getInvokeExpr()
            .orElseThrow(() -> new IllegalStateException("Call-to-return site has no invoke expression: " + callSite));
        final List<Immediate> callArgs = ie.getArgs();
        final Value base = invokeBase(ie);
        final Value leftOp = callSite instanceof AbstractDefinitionStmt
                ? ((AbstractDefinitionStmt) callSite).getLeftOp() : null;
        final boolean hasCallees = !mIcfg.getCalleesOfCallAt(callSite).isEmpty();
        final Set<TrackedPrimitiveFact<L>> invokedFacts = factsFromInvokeArgs(ie);

        return source -> {
            if (source.isZero()) {
                final Set<TrackedPrimitiveFact<L>> ret = new HashSet<>(invokedFacts);
                ret.add(source);
                return ret;
            }
            final boolean keep = leftOp == null || !source.getCarrier().equivTo(leftOp);
            if (hasCallees) {
                final Set<TrackedPrimitiveFact<L>> ret = new HashSet<>(invokedFacts);
                if (keep && source.getCarrier() instanceof Local) {
                    ret.add(source);
                }
                return ret;
            }
            final Set<TrackedPrimitiveFact<L>> ret = unresolvedTaints(source, base, leftOp, callArgs);
            if (keep) {
                ret.add(source);
            }
            return ret;
        };
    }

    private static Value invokeBase(final AbstractInvokeExpr ie) {
        if (ie instanceof JVirtualInvokeExpr) {
            return ((JVirtualInvokeExpr) ie).getBase();
        }
        if (ie instanceof JInterfaceInvokeExpr) {
            return ((JInterfaceInvokeExpr) ie).getBase();
        }
        if (ie instanceof JSpecialInvokeExpr) {
            return ((JSpecialInvokeExpr) ie).getBase();
        }
        return null;
    }

    private Set<TrackedPrimitiveFact<L>> unresolvedTaints(final TrackedPrimitiveFact<L> source,
                                                          final Value base,
                                                          final Value leftOp,
                                                          final List<Immediate> callArgs) {
        final Set<TrackedPrimitiveFact<L>> ret = new HashSet<>();
        if (base != null && leftOp != null && source.getCarrier().equivTo(base)) {
            ret.add(TrackedPrimitiveFact.of(source.getTag(), leftOp));
        }
        for (final Immediate arg : callArgs) {
            if (passesArgumentToCallee(arg, source)) {
                if (leftOp != null) {
                    ret.add(TrackedPrimitiveFact.of(source.getTag(), leftOp));
                }
                if (base != null) {
                    ret.add(TrackedPrimitiveFact.of(source.getTag(), base));
                }
                break;
            }
        }
        return ret;
    }

    /**
     * @param imm actual argument at the call
     * @param source fact reaching the call
     * @return whether that fact should be mapped onto the matching formal parameter
     */
    private boolean passesArgumentToCallee(final Immediate imm, final TrackedPrimitiveFact<L> source) {
        if (source.isZero()) {
            return false;
        }
        final Value carrier = source.getCarrier();
        if (immediateMatchesCarrier(imm, carrier)) {
            return true;
        }
        return constantInvokeArgMatchesTaggedLocal(imm, source);
    }

    /**
     * @param imm call argument (may be a folded literal)
     * @param source fact whose carrier is a local
     * @return true when the literal maps in spec to the same tag as the fact
     */
    private boolean constantInvokeArgMatchesTaggedLocal(final Immediate imm, final TrackedPrimitiveFact<L> source) {
        if (!(source.getCarrier() instanceof Local)) {
            return false;
        }
        final Optional<L> litTag = mSpec.tagForConstant(JimpleValues.unwrapCastChain(imm));
        return litTag.isPresent() && litTag.get().equals(source.getTag());
    }

    private static boolean immediateMatchesCarrier(final Immediate imm, final Value carrier) {
        if (carrier == null) {
            return false;
        }
        final Value u = JimpleValues.unwrapCastChain(imm);
        return carrier.equivTo(u) || carrier == u;
    }

    private Optional<L> factTagFromSource(final Value v) {
        if (v instanceof JStaticFieldRef) {
            return mSpec.tagForField((JStaticFieldRef) v);
        }
        return mSpec.tagForConstant(v);
    }
}
