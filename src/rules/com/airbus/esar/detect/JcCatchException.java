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

import sootup.core.graph.BasicBlock;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

/**
 * Catching java.lang.Exception is not allowed; only specific Java Card exceptions may be caught
 */
public final class JcCatchException implements ProgramVisitorIf {
    public static final String RULE_ID = "JcCatchException";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.ERROR;
    public static final String MESSAGE_CATCH_EXCEPTION = "javacard.exception.catch";

    private ClassType mExceptionType;


    @Override
    public String ruleId() {
        return RULE_ID;
    }

    @Override
    public SarifResultLevel severity() {
        return DEFAULT_SEVERITY;
    }

    @Override
    public void startAnalysis(final AnalysisContextIf context,
                              final BugReporterIf bugs) {
        final JavaView view = context.getJavaView();
        mExceptionType = view.getIdentifierFactory().getClassType("java.lang.Exception");
    }

    @Override
    public void endAnalysis(final AnalysisContextIf context, final BugReporterIf bugs) {
        mExceptionType = null;
    }

    @Override
    public void visitStmt(final JavaSootMethod method,
                          final Body body,
                          final Stmt stmt,
                          final AnalysisContextIf context,
                          final BugReporterIf bugs) {
        if (mExceptionType == null) {
            return;
        }
        // A catch handler's first statement is always JIdentityStmt($e := @caughtexception).
        // JCaughtExceptionRef.getType() always returns Throwable regardless of the caught type,
        // so the actual exception type must be read from the block's exceptional predecessors.
        if (!(stmt instanceof JIdentityStmt identityStmt)) {
            return;
        }
        if (!(identityStmt.getRightOp() instanceof JCaughtExceptionRef)) {
            return;
        }

        final BasicBlock<?> block = body.getStmtGraph().getBlockOf(stmt);
        if (!block.getExceptionalPredecessors().containsKey(mExceptionType)) {
            return;
        }

        final Stmt reportAt = StmtSourceLocations.reportAnchorAfterCaughtException(body, stmt);
        bugs.report(RULE_ID, DEFAULT_SEVERITY,
                RuleMessageRef.of(MESSAGE_CATCH_EXCEPTION), method, reportAt);
    }
}
