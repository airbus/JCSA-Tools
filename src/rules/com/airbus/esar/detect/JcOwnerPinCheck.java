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

import java.util.List;
import java.util.Optional;

import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.ArrayType;
import sootup.core.types.PrimitiveType;
import sootup.java.core.JavaSootMethod;
import com.airbus.esar.analysis.callflow.InvokeSignaturePattern;

/**
 * {@link javacard.framework.OwnerPIN#check(byte[], short, byte)} must not be called as a stand-alone statement;
 * the {@code boolean} result must be assigned, tested, returned, or passed as an argument.
 */
public final class JcOwnerPinCheck implements ProgramVisitorIf {

    public static final String RULE_ID = "JcOwnerPinCheck";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.WARNING;
    public static final String MESSAGE_CHECK_IGNORED = "javacard.ownerPin.checkIgnored";

    private static final ArrayType BYTE_ARRAY = new ArrayType(PrimitiveType.getByte(), 1);

    private static final List<InvokeSignaturePattern> CHECK_CALLS = List.of(
            InvokeSignaturePattern.instanceCallIgnoringReturn(
                    "javacard.framework.OwnerPIN", "check", BYTE_ARRAY,
                    PrimitiveType.getShort(), PrimitiveType.getByte()),
            InvokeSignaturePattern.instanceCallIgnoringReturn(
                    "javacard.framework.PIN", "check", BYTE_ARRAY,
                    PrimitiveType.getShort(), PrimitiveType.getByte()));


    @Override
    public String ruleId() {
        return RULE_ID;
    }

    @Override
    public SarifResultLevel severity() {
        return DEFAULT_SEVERITY;
    }

    @Override
    public void visitStmt(final JavaSootMethod method,
                          final Body body,
                          final Stmt stmt,
                          final AnalysisContextIf context,
                          final BugReporterIf bugs) {
        if (!(stmt instanceof JInvokeStmt)) {
            return;
        }
        final Optional<AbstractInvokeExpr> invOpt = ((JInvokeStmt) stmt).getInvokeExpr();
        if (invOpt.isEmpty() || !isOwnerPinCheck(invOpt.get())) {
            return;
        }
        bugs.report(RULE_ID, DEFAULT_SEVERITY, RuleMessageRef.of(MESSAGE_CHECK_IGNORED), method, stmt);
    }

    private static boolean isOwnerPinCheck(final AbstractInvokeExpr expr) {
        for (final InvokeSignaturePattern pattern : CHECK_CALLS) {
            if (pattern.matches(expr)) {
                return true;
            }
        }
        return false;
    }
}
