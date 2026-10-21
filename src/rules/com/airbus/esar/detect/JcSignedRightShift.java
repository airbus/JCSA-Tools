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
import sootup.core.jimple.common.expr.JShrExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.java.core.JavaSootMethod;

public final class JcSignedRightShift implements ProgramVisitorIf {
    public static final String RULE_ID = "JcSignedRightShift";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.ERROR;
    public static final String MESSAGE_SIGNED_RIGHT_SHIFT = "javacard.bitshift.signedRight";

    @Override
    public String ruleId() {
        return RULE_ID;
    }

    @Override
    public SarifResultLevel severity() {
        return DEFAULT_SEVERITY;
    }

    public void visitExpr(final JavaSootMethod method,
                          final Body body,
                          final Stmt stmt,
                          final Value value,
                          final AnalysisContextIf context,
                          final BugReporterIf bugs) {
        if (!(value instanceof JShrExpr)) {
            return;
        }

        bugs.report(RULE_ID, DEFAULT_SEVERITY,
                RuleMessageRef.of(MESSAGE_SIGNED_RIGHT_SHIFT), method, stmt);
    }
}
