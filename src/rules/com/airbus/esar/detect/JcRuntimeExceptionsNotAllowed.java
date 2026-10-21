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

import sootup.core.jimple.common.stmt.JThrowStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;
import com.airbus.esar.common.JcConstants;
import com.airbus.esar.soot.TypeUtils;

/**
 * Reports a thrown {@code java.lang.RuntimeException} or subtype.
 * {@code CardRuntimeException} and its subtypes are allowed (AFSCM 45).
 */
public final class JcRuntimeExceptionsNotAllowed implements ProgramVisitorIf {
    public static final String RULE_ID = "JcRuntimeExceptionsNotAllowed";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.WARNING;
    public static final String MESSAGE_THROW_RUNTIME = "javacard.exception.runtime.throw";

    private ClassType mRuntimeExceptionType;
    private ClassType mCardRuntimeExceptionType;


    @Override
    public String ruleId() {
        return RULE_ID;
    }

    @Override
    public SarifResultLevel severity() {
        return DEFAULT_SEVERITY;
    }

    @Override
    public void startAnalysis(final AnalysisContextIf context, final BugReporterIf bugs) {
        mRuntimeExceptionType = context.getJavaView()
                .getIdentifierFactory()
                .getClassType("java.lang.RuntimeException");
        mCardRuntimeExceptionType = context.getJavaView()
                .getIdentifierFactory()
                .getClassType(JcConstants.CARD_RUNTIME_EXCEPTION_CLASS);
    }

    @Override
    public void endAnalysis(final AnalysisContextIf context, final BugReporterIf bugs) {
        mRuntimeExceptionType = null;
        mCardRuntimeExceptionType = null;
    }

    @Override
    public void visitStmt(final JavaSootMethod method,
                          final Body body,
                          final Stmt stmt,
                          final AnalysisContextIf context,
                          final BugReporterIf bugs) {
        if (mRuntimeExceptionType == null || mCardRuntimeExceptionType == null) {
            return;
        }
        final JavaView view = context.getJavaView();
        if (!(stmt instanceof JThrowStmt throwStmt)) {
            return;
        }
        final Type thrownType = throwStmt.getOp().getType();
        if (!(thrownType instanceof ClassType ct) || !isProhibitedRuntimeException(view, ct)) {
            return;
        }

        bugs.report(RULE_ID, DEFAULT_SEVERITY,
                RuleMessageRef.of(MESSAGE_THROW_RUNTIME, ct.getFullyQualifiedName()),
                method, stmt);
    }

    private boolean isProhibitedRuntimeException(final JavaView view, final ClassType type) {
        if (isAllowedCardRuntimeException(view, type)) {
            return false;
        }
        final String fqn = type.getFullyQualifiedName();
        if ("java.lang.RuntimeException".equals(fqn)) {
            return true;
        }
        final Optional<JavaSootClass> cls = view.getClass(type);
        return cls.isPresent() && TypeUtils.extendsClass(view, cls.get(), mRuntimeExceptionType);
    }

    /**
     * Java Card unchecked API exceptions: {@code CardRuntimeException} and its subtypes
     * (ISOException, CryptoException, TransactionException, APDUException, …).
     *
     * @param view analysis view used to walk the superclass chain
     * @param type thrown exception type
     * @return true when {@code type} is CardRuntimeException or a subtype
     */
    private boolean isAllowedCardRuntimeException(final JavaView view, final ClassType type) {
        final String fqn = type.getFullyQualifiedName();
        if (JcConstants.CARD_RUNTIME_EXCEPTION_CLASS.equals(fqn)) {
            return true;
        }
        final Optional<JavaSootClass> cls = view.getClass(type);
        return cls.isPresent()
                && TypeUtils.extendsClass(view, cls.get(), mCardRuntimeExceptionType);
    }
}
