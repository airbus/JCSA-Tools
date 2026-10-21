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

package com.airbus.esar.detect;

import heros.InterproceduralCFG;

import sootup.analysis.interprocedural.ide.JimpleIDESolver;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.types.PrimitiveType;
import sootup.java.core.JavaSootMethod;
import com.airbus.esar.analysis.constantpropagation.ConstantLatticeValue;
import com.airbus.esar.analysis.constantpropagation.ConstantPropagationSolverFactory;
import com.airbus.esar.analysis.constantpropagation.ConstantPropagatorFact;
import com.airbus.esar.common.JcConstants;
import com.airbus.esar.jcsolvers.JcSharedSolvers;
import com.airbus.esar.soot.InvokeMatchers;
import com.airbus.esar.soot.StaticInvokeSignature;

/**
 * Flags {@code ISOException.throwIt(REPLY_BUSY)} as an invalid event-processing conclusion (AFSCM 49).
 */
public final class JcIsoExceptionReplyBusy implements ProgramVisitorIf {

    public static final String RULE_ID = "JcIsoExceptionReplyBusy";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.ERROR;
    public static final String MESSAGE_REPLY_BUSY = "javacard.isoException.replyBusy";

    private static final StaticInvokeSignature ISO_EXCEPTION_THROW_IT = StaticInvokeSignature.ignoringReturn(
            JcConstants.ISO_EXCEPTION_CLASS,
            JcConstants.THROW_IT,
            PrimitiveType.getShort());

    private JimpleIDESolver<
            ConstantPropagatorFact, ConstantLatticeValue, InterproceduralCFG<Stmt, SootMethod>> mSolver;


    @Override
    public String ruleId() {
        return RULE_ID;
    }

    @Override
    public SarifResultLevel severity() {
        return DEFAULT_SEVERITY;
    }

    @Override
    public void initAnalysis(final SharedSolverRegistryIf registry, final AnalysisContextIf context) {
        mSolver = registry.lazyRegister(
                JcSharedSolvers.JC_CONSTANT_PROPAGATION,
                () -> ConstantPropagationSolverFactory.buildSolver(context));
    }

    @Override
    public void visitMethod(final JavaSootMethod method,
                            final Body body,
                            final AnalysisContextIf context,
                            final BugReporterIf bugs) {
        if (!context.isMethodInEntryPointReachabilitySlice(method.getSignature())) {
            return;
        }
        for (final Stmt stmt : body.getStmts()) {
            final AbstractInvokeExpr expr = InvokeMatchers.getMatchingStaticInvoke(stmt, ISO_EXCEPTION_THROW_IT);
            if (expr == null) {
                continue;
            }
            if (!IsoExceptionReplyBusyChecker.isReplyBusyThrow(mSolver, stmt, expr)) {
                continue;
            }
            bugs.report(RULE_ID, DEFAULT_SEVERITY, RuleMessageRef.of(MESSAGE_REPLY_BUSY), method, stmt);
        }
    }
}
