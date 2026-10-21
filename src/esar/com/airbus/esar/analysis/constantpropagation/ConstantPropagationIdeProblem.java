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

package com.airbus.esar.analysis.constantpropagation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

import heros.DefaultSeeds;
import heros.EdgeFunction;
import heros.EdgeFunctions;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.MeetLattice;
import heros.edgefunc.AllTop;
import heros.edgefunc.EdgeIdentity;
import heros.flowfunc.Identity;
import heros.flowfunc.KillAll;

import sootup.analysis.interprocedural.ide.DefaultJimpleIDETabulationProblem;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.views.JavaView;
import com.airbus.esar.soot.JimpleValues;

/**
 * IDE integer constant propagation: IFDS facts are tracked locals; edge functions copy or generate constants.
 */
public final class ConstantPropagationIdeProblem
        extends DefaultJimpleIDETabulationProblem<
                ConstantPropagatorFact,
                ConstantLatticeValue,
                InterproceduralCFG<Stmt, SootMethod>> {

    private final InterproceduralCFG<Stmt, SootMethod> mIcfg;
    private final List<Stmt> mEntryStarts;

    public ConstantPropagationIdeProblem(final InterproceduralCFG<Stmt, SootMethod> icfg,
                                         final JavaView view,
                                         final List<MethodSignature> entryPoints) {
        super(icfg);
        mIcfg = icfg;
        mEntryStarts = collectStartingStmts(view, entryPoints);
    }

    private static List<Stmt> collectStartingStmts(final JavaView view, final List<MethodSignature> entryPoints) {
        final List<Stmt> starts = new ArrayList<>();
        for (final MethodSignature sig : entryPoints) {
            final var m = view.getMethod(sig);
            if (m.isPresent() && m.get().hasBody()) {
                starts.add(m.get().getBody().getStmtGraph().getStartingStmt());
            }
        }
        return starts.isEmpty() ? List.of() : List.copyOf(starts);
    }

    @Override
    public java.util.Map<Stmt, Set<ConstantPropagatorFact>> initialSeeds() {
        if (mEntryStarts.isEmpty()) {
            return Collections.emptyMap();
        }
        return DefaultSeeds.make(mEntryStarts, zeroValue());
    }

    @Override
    protected ConstantPropagatorFact createZeroValue() {
        return ConstantPropagatorFact.zero();
    }

    @Override
    protected FlowFunctions<Stmt, ConstantPropagatorFact, SootMethod> createFlowFunctionsFactory() {
        return new FlowFunctions<Stmt, ConstantPropagatorFact, SootMethod>() {
            @Override
            public FlowFunction<ConstantPropagatorFact> getNormalFlowFunction(final Stmt curr, final Stmt succ) {
                return getNormalFlow(curr);
            }

            @Override
            public FlowFunction<ConstantPropagatorFact> getCallFlowFunction(final Stmt callStmt,
                                                                            final SootMethod destinationMethod) {
                return getCallFlow(callStmt, destinationMethod);
            }

            @Override
            public FlowFunction<ConstantPropagatorFact> getReturnFlowFunction(final Stmt callSite,
                                                                            final SootMethod calleeMethod,
                                                                            final Stmt exitStmt,
                                                                            final Stmt returnSite) {
                return getReturnFlow(callSite, calleeMethod, exitStmt);
            }

            @Override
            public FlowFunction<ConstantPropagatorFact> getCallToReturnFlowFunction(final Stmt callSite,
                                                                                    final Stmt returnSite) {
                return getCallToReturnFlow(callSite);
            }
        };
    }

    @Override
    protected MeetLattice<ConstantLatticeValue> createMeetLattice() {
        return new MeetLattice<ConstantLatticeValue>() {
            @Override
            public ConstantLatticeValue topElement() {
                return ConstantLatticeValue.top();
            }

            @Override
            public ConstantLatticeValue bottomElement() {
                return ConstantLatticeValue.bottom();
            }

            @Override
            public ConstantLatticeValue meet(final ConstantLatticeValue first, final ConstantLatticeValue second) {
                return first.meet(second);
            }
        };
    }

    @Override
    protected EdgeFunction<ConstantLatticeValue> createAllTopFunction() {
        return new AllTop<>(ConstantLatticeValue.top());
    }

    @Override
    protected EdgeFunctions<Stmt, ConstantPropagatorFact, SootMethod, ConstantLatticeValue>
            createEdgeFunctionsFactory() {
        return new EdgeFunctions<Stmt, ConstantPropagatorFact, SootMethod, ConstantLatticeValue>() {
            @Override
            public EdgeFunction<ConstantLatticeValue> getNormalEdgeFunction(final Stmt curr,
                                                                            final ConstantPropagatorFact src,
                                                                            final Stmt succ,
                                                                            final ConstantPropagatorFact tgt) {
                return normalEdge(curr, src, tgt);
            }

            @Override
            public EdgeFunction<ConstantLatticeValue> getCallEdgeFunction(final Stmt call,
                                                                          final ConstantPropagatorFact src,
                                                                          final SootMethod dest,
                                                                          final ConstantPropagatorFact tgt) {
                return EdgeIdentity.v();
            }

            @Override
            public EdgeFunction<ConstantLatticeValue> getReturnEdgeFunction(final Stmt call,
                                                                            final SootMethod dest,
                                                                            final Stmt exit,
                                                                            final ConstantPropagatorFact src,
                                                                            final Stmt retSite,
                                                                            final ConstantPropagatorFact tgt) {
                return returnEdge(call, src, exit, tgt);
            }

            @Override
            public EdgeFunction<ConstantLatticeValue> getCallToReturnEdgeFunction(final Stmt call,
                                                                                  final ConstantPropagatorFact src,
                                                                                  final Stmt retSite,
                                                                                  final ConstantPropagatorFact tgt) {
                return EdgeIdentity.v();
            }
        };
    }

    private EdgeFunction<ConstantLatticeValue> normalEdge(final Stmt curr,
                                                          final ConstantPropagatorFact src,
                                                          final ConstantPropagatorFact tgt) {
        if (tgt.isZero()) {
            return EdgeIdentity.v();
        }

        if (curr instanceof JIdentityStmt) {
            return sameVariableEdge(src, tgt);
        }
        if (!(curr instanceof JAssignStmt assign)) {
            return sameVariableEdge(src, tgt);
        }
        final Value left = assign.getLeftOp();
        final Value right = JimpleValues.unwrapCastChain(assign.getRightOp());
        if (!(left instanceof Local) || !variableMatches(tgt.getVariable(), left)) {
            return sameVariableEdge(src, tgt);
        }
        final OptionalInt constant = intConstant(right);
        if (constant.isPresent()) {
            return ConstantGenEdgeFunction.of(ConstantLatticeValue.of(constant.getAsInt()));
        }
        if (right instanceof Local rightLocal) {
            if (!src.isZero() && variableMatches(src.getVariable(), rightLocal)) {
                return EdgeIdentity.v();
            }
            return new AllTop<>(ConstantLatticeValue.top());
        }
        return new AllTop<>(ConstantLatticeValue.top());
    }

    private EdgeFunction<ConstantLatticeValue> returnEdge(final Stmt call,
                                                          final ConstantPropagatorFact src,
                                                          final Stmt exit,
                                                          final ConstantPropagatorFact tgt) {
        if (tgt.isZero()) {
            return EdgeIdentity.v();
        }
        Local callLhs = null;
        if (call instanceof AbstractDefinitionStmt defn && defn.getLeftOp() instanceof Local local) {
            callLhs = local;
        }
        if (callLhs == null || !variableMatches(tgt.getVariable(), callLhs)) {
            return sameVariableEdge(src, tgt);
        }
        if (!(exit instanceof JReturnStmt returnStmt)) {
            return sameVariableEdge(src, tgt);
        }
        final Value retOp = JimpleValues.unwrapCastChain(returnStmt.getOp());
        final OptionalInt constant = intConstant(retOp);
        if (constant.isPresent()) {
            return ConstantGenEdgeFunction.of(ConstantLatticeValue.of(constant.getAsInt()));
        }
        if (!src.isZero() && variableMatches(src.getVariable(), retOp)) {
            return EdgeIdentity.v();
        }
        return new AllTop<>(ConstantLatticeValue.top());
    }

    private static EdgeFunction<ConstantLatticeValue> sameVariableEdge(final ConstantPropagatorFact src,
                                                                       final ConstantPropagatorFact tgt) {
        if (!src.isZero() && !tgt.isZero() && variableMatches(src.getVariable(), tgt.getVariable())) {
            return EdgeIdentity.v();
        }
        return new AllTop<>(ConstantLatticeValue.top());
    }

    private FlowFunction<ConstantPropagatorFact> getNormalFlow(final Stmt curr) {
        if (!(curr instanceof JAssignStmt assign)) {
            return source -> {
                final Set<ConstantPropagatorFact> res = new HashSet<>();
                res.add(source);
                return res;
            };
        }
        final Value left = assign.getLeftOp();
        final Value right = JimpleValues.unwrapCastChain(assign.getRightOp());
        final boolean lhsIsLocal = left instanceof Local;
        final OptionalInt rightConst = intConstant(right);

        return source -> {
            if (!source.isZero() && lhsIsLocal && variableMatches(source.getVariable(), left)) {
                return Collections.emptySet();
            }
            final Set<ConstantPropagatorFact> res = new HashSet<>();
            res.add(source);

            if (lhsIsLocal) {
                if (source.isZero() && rightConst.isPresent()) {
                    res.add(ConstantPropagatorFact.of(left));
                }
                if (!source.isZero() && variableMatches(source.getVariable(), right)) {
                    res.add(ConstantPropagatorFact.of(left));
                }
            }
            return res;
        };
    }

    private FlowFunction<ConstantPropagatorFact> getCallFlow(final Stmt callStmt,
                                                             final SootMethod destinationMethod) {
        if ("<clinit>".equals(destinationMethod.getName())) {
            return KillAll.v();
        }
        if (!destinationMethod.hasBody() || !(callStmt instanceof InvokableStmt invokable)) {
            return Identity.v();
        }
        if (invokable.getInvokeExpr().isEmpty()) {
            return Identity.v();
        }
        final AbstractInvokeExpr ie = invokable.getInvokeExpr().get();
        final List<Immediate> callArgs = ie.getArgs();
        final List<Value> paramLocals = new ArrayList<>();
        for (int i = 0; i < destinationMethod.getParameterCount(); i++) {
            paramLocals.add(destinationMethod.getBody().getParameterLocal(i));
        }
        return source -> {
            if (source.isZero()) {
                return Collections.singleton(source);
            }
            final Set<ConstantPropagatorFact> ret = new HashSet<>();
            for (int i = 0; i < callArgs.size(); i++) {
                if (i < paramLocals.size() && argumentMatches(callArgs.get(i), source.getVariable())) {
                    ret.add(ConstantPropagatorFact.of(paramLocals.get(i)));
                }
            }
            return ret;
        };
    }

    private FlowFunction<ConstantPropagatorFact> getReturnFlow(final Stmt callSite,
                                                               final SootMethod calleeMethod,
                                                               final Stmt exitStmt) {
        if (!(exitStmt instanceof JReturnStmt returnStmt)) {
            if (exitStmt instanceof JReturnVoidStmt) {
                return source -> source.isZero() ? Collections.singleton(source) : Collections.emptySet();
            }
            return KillAll.v();
        }
        final Value retOp = JimpleValues.unwrapCastChain(returnStmt.getOp());
        if (!(callSite instanceof AbstractDefinitionStmt defn) || !(defn.getLeftOp() instanceof Local leftLocal)) {
            return source -> source.isZero() ? Collections.singleton(source) : Collections.emptySet();
        }
        final OptionalInt retConst = intConstant(retOp);
        return source -> {
            final Set<ConstantPropagatorFact> ret = new HashSet<>();
            if (source.isZero()) {
                ret.add(source);
                if (retConst.isPresent()) {
                    ret.add(ConstantPropagatorFact.of(leftLocal));
                }
                return ret;
            }
            if (variableMatches(source.getVariable(), retOp)) {
                ret.add(ConstantPropagatorFact.of(leftLocal));
            }
            return ret;
        };
    }

    private FlowFunction<ConstantPropagatorFact> getCallToReturnFlow(final Stmt callSite) {
        if (!(callSite instanceof InvokableStmt)) {
            return Identity.v();
        }
        final Value leftOp;
        if (callSite instanceof AbstractDefinitionStmt defn) {
            leftOp = defn.getLeftOp();
        } else {
            leftOp = null;
        }
        return source -> {
            if (source.isZero()) {
                return Collections.singleton(source); // Preserve zero-seed Lambda across call sites
            }
            // Kill any variable clobbered by the LHS of an invocation assignment across all calls
            if (leftOp instanceof Local && variableMatches(source.getVariable(), leftOp)) {
                return Collections.emptySet();
            }
            return Collections.singleton(source); // All other stack locals survive call-to-return safely
        };
    }

    private static OptionalInt intConstant(final Value value) {
        final Value target = JimpleValues.unwrapCastChain(value);
        if (target instanceof IntConstant ic) {
            return OptionalInt.of(ic.getValue());
        }
        return OptionalInt.empty();
    }

    private static boolean argumentMatches(final Immediate arg, final Value variable) {
        return variableMatches(variable, JimpleValues.unwrapCastChain(arg));
    }

    private static boolean variableMatches(final Value variable, final Value other) {
        if (variable == null || other == null) {
            return false;
        }
        return variable == other || variable.equivTo(other);
    }
}
