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

import java.util.Optional;

import heros.InterproceduralCFG;

import sootup.analysis.interprocedural.ifds.JimpleIFDSSolver;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.types.PrimitiveType;
import sootup.java.core.JavaSootMethod;
import com.airbus.esar.analysis.primitivetaint.PrimitiveTaintIfdsQuery;
import com.airbus.esar.analysis.primitivetaint.TrackedPrimitiveFact;
import com.airbus.esar.common.JcConstants;
import com.airbus.esar.jcsolvers.JcAlgTaintTag;
import com.airbus.esar.jcsolvers.JcAlgTaintWarningDetail;
import com.airbus.esar.jcsolvers.JcSharedSolvers;
import com.airbus.esar.jcsolvers.JcSolverFactory;
import com.airbus.esar.soot.InvokeMatchers;
import com.airbus.esar.soot.StaticInvokeSignature;

/** RandomData.getInstance/open(byte) with deprecated algorithm (JC 3.1). Shared primitive-taint IFDS. */
public final class JcDeprecatedRandomDataAlg implements ProgramVisitorIf {

    public static final String RULE_ID = "JcDeprecatedRandomDataAlg";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.ERROR;

    public static final String MESSAGE_DEPRECATED_ALGORITHM = "javacard.randomData.deprecatedAlgorithm";

    private static final StaticInvokeSignature RANDOM_DATA_GET_INSTANCE =
            StaticInvokeSignature.of(
                    JcConstants.RANDOM_DATA_CLASS,
                    "getInstance",
                    JcConstants.RANDOM_DATA_CLASS,
                    PrimitiveType.getByte());

    private static final StaticInvokeSignature RANDOM_DATA_ONESHOT_OPEN =
            StaticInvokeSignature.of(
                    JcConstants.RANDOM_DATA_ONESHOT_CLASS,
                    "open",
                    JcConstants.RANDOM_DATA_ONESHOT_CLASS,
                    PrimitiveType.getByte());

    private static final PrimitiveTaintIfdsQuery<JcAlgTaintTag> RANDOM_DATA_IFDS =
            new PrimitiveTaintIfdsQuery<>(
            JcAlgTaintTag::isRandomDataAlgorithm,
            JcDeprecatedRandomDataAlg::preferRandomDataPseudoOverSecure,
            JcAlgTaintWarningDetail::messageForFact);

    private JimpleIFDSSolver<TrackedPrimitiveFact<JcAlgTaintTag>, InterproceduralCFG<Stmt, SootMethod>> mSolver;


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
        mSolver = registry.lazyRegister(JcSharedSolvers.JC_DEPRECATED_ALG_TAINT,
                                        () -> JcSolverFactory.buildPrimitiveTaintSolver(context));
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
            final AbstractInvokeExpr expr = InvokeMatchers.getMatchingStaticInvoke(
                    stmt,
                    RANDOM_DATA_GET_INSTANCE,
                    RANDOM_DATA_ONESHOT_OPEN);
            if (expr == null) {
                continue;
            }
            final Optional<String> algId = RANDOM_DATA_IFDS.query(mSolver, stmt, body, expr.getArg(0));
            if (!algId.isPresent()) {
                continue;
            }

            bugs.report(RULE_ID, DEFAULT_SEVERITY,
                    RuleMessageRef.of(MESSAGE_DEPRECATED_ALGORITHM, algId.get()), method, stmt);
        }
    }

    private static TrackedPrimitiveFact<JcAlgTaintTag> preferRandomDataPseudoOverSecure(
            final TrackedPrimitiveFact<JcAlgTaintTag> current,
            final TrackedPrimitiveFact<JcAlgTaintTag> candidate) {
        if (current == null) {
            return candidate;
        }
        if (current.getTag() == JcAlgTaintTag.ALG_PSEUDO_RANDOM) {
            return current;
        }
        if (candidate.getTag() == JcAlgTaintTag.ALG_PSEUDO_RANDOM) {
            return candidate;
        }
        return current;
    }
}
