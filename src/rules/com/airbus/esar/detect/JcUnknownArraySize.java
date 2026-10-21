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

import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.java.core.JavaSootMethod;
import com.airbus.esar.analysis.knownvalue.CompileTimeKnownArraySize;

/**
 * Flags {@code newarray} when the size operand is not compile-time known (AFSCM 26).
 */
public final class JcUnknownArraySize implements ProgramVisitorIf {

    public static final String RULE_ID = "JcUnknownArraySize";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.WARNING;
    public static final String MESSAGE_UNKNOWN_ALLOC_SIZE = "javacard.array.unknownAllocSize";


    @Override
    public String ruleId() {
        return RULE_ID;
    }

    @Override
    public SarifResultLevel severity() {
        return DEFAULT_SEVERITY;
    }

    @Override
    public void visitMethod(final JavaSootMethod method,
                            final Body body,
                            final AnalysisContextIf context,
                            final BugReporterIf bugs) {
        if (!context.isMethodInEntryPointReachabilitySlice(method.getSignature())) {
            return;
        }
        for (final Stmt stmt : body.getStmtGraph()) {
            for (final Value value : stmt.getUsesAndDefs().toList()) {
                if (value instanceof final JNewArrayExpr alloc) {
                    if (CompileTimeKnownArraySize.isAllocSizeKnown(alloc, stmt, body, context)) {
                        continue;
                    }
                    bugs.report(RULE_ID, DEFAULT_SEVERITY,
                            RuleMessageRef.of(MESSAGE_UNKNOWN_ALLOC_SIZE, alloc.getBaseType() + "[]"),
                            method, stmt);
                }
            }
        }
    }
}
