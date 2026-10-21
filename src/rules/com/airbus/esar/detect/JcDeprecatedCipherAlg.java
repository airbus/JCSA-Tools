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

/**
 * Deprecated Cipher {@code ALG_*} bytes at {@code Cipher.getInstance}; shared primitive-taint IFDS.
 *
 * {@code Cipher.OneShot.open} takes {@code CIPHER_*} and {@code PAD_*}, not deprecated {@code ALG_*},
 * so OneShot is out of scope for this rule (unlike {@link JcDeprecatedRandomDataAlg}).
 */
public final class JcDeprecatedCipherAlg implements ProgramVisitorIf {
    public static final String RULE_ID = "JcDeprecatedCipherAlg";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.ERROR;
    public static final String MESSAGE_DEPRECATED_CIPHER = "javacard.cipher.deprecatedAlgorithm";
    private static final StaticInvokeSignature CIPHER_GET_INSTANCE_BYTE_BOOLEAN =
            StaticInvokeSignature.of(
                    JcConstants.CIPHER_CLASS,
                    "getInstance",
                    JcConstants.CIPHER_CLASS,
                    PrimitiveType.getByte(),
                    PrimitiveType.getBoolean());

    private static final StaticInvokeSignature CIPHER_GET_INSTANCE_BYTE_BYTE_BOOLEAN =
            StaticInvokeSignature.of(
                    JcConstants.CIPHER_CLASS,
                    "getInstance",
                    JcConstants.CIPHER_CLASS,
                    PrimitiveType.getByte(),
                    PrimitiveType.getByte(),
                    PrimitiveType.getBoolean());

    private static final PrimitiveTaintIfdsQuery<JcAlgTaintTag> IFDS_QUERY =
            new PrimitiveTaintIfdsQuery<>(
                    JcAlgTaintTag::isDeprecatedCipherAlgorithm,
                    JcDeprecatedCipherAlg::keepFirstMatchingFact,
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
                    CIPHER_GET_INSTANCE_BYTE_BOOLEAN,
                    CIPHER_GET_INSTANCE_BYTE_BYTE_BOOLEAN);

            if (expr == null) {
                continue;
            }

            final Optional<String> algId = IFDS_QUERY.query(mSolver, stmt, body, expr.getArg(0));
            if (!algId.isPresent()) {
                continue;
            }

            bugs.report(RULE_ID, DEFAULT_SEVERITY,
                    RuleMessageRef.of(MESSAGE_DEPRECATED_CIPHER, algId.get()), method, stmt);
        }
    }

    private static TrackedPrimitiveFact<JcAlgTaintTag> keepFirstMatchingFact(
            final TrackedPrimitiveFact<JcAlgTaintTag> current,
            final TrackedPrimitiveFact<JcAlgTaintTag> candidate) {
        return current != null ? current : candidate;
    }
}
