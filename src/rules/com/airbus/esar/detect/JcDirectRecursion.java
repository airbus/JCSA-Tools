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

import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaSootMethod;

/**
 * Reports methods that contain a direct recursive call
 */
public final class JcDirectRecursion implements ProgramVisitorIf {

    public static final String RULE_ID = "JcDirectRecursion";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.ERROR;
    public static final String MESSAGE_DIRECT_RECURSION = "javacard.recursion.directSelfCall";


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
        final MethodSignature self = method.getSignature();
        for (final Stmt stmt : body.getStmtGraph()) {
            if (!(stmt instanceof InvokableStmt)) {
                continue;
            }
            final Optional<AbstractInvokeExpr> inv = ((InvokableStmt) stmt).getInvokeExpr();
            if (inv.isEmpty()) {
                continue;
            }
            if (!inv.get().getMethodSignature().equals(self)) {
                continue;
            }

            bugs.report(RULE_ID, DEFAULT_SEVERITY,
                    RuleMessageRef.of(MESSAGE_DIRECT_RECURSION), method, stmt);
        }
    }
}
