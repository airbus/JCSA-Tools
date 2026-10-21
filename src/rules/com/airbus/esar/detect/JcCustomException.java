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

import java.util.Set;

import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;
import com.airbus.esar.soot.TypeUtils;

/**
 * Application-specific exceptions are not allowed; only Java Card API exceptions may appear on the classpath (TCA 35)
 */
public final class JcCustomException implements ProgramVisitorIf {
    public static final String RULE_ID = "JcCustomException";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.ERROR;
    public static final String MESSAGE_CUSTOM_EXCEPTION = "javacard.exception.custom";

    private static final Set<String> WHITELISTED_PACKAGES = Set.of(
            "java",
            "javacard",
            "javacardx"
    );

    private ClassType mExceptionType;
    private ClassType mRuntimeExceptionType;


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
        final JavaView view = context.getJavaView();
        mExceptionType = view.getIdentifierFactory().getClassType("java.lang.Exception");
        mRuntimeExceptionType = view.getIdentifierFactory().getClassType("java.lang.RuntimeException");
    }

    @Override
    public void endAnalysis(final AnalysisContextIf context, final BugReporterIf bugs) {
        mExceptionType = null;
        mRuntimeExceptionType = null;
    }

    @Override
    public void visitClass(final JavaSootClass clazz,
                           final AnalysisContextIf context,
                           final BugReporterIf bugs) {
        if (mExceptionType == null) {
            return;
        }

        final String fqn = clazz.getType().getFullyQualifiedName();
        if (isWhitelistedPackage(fqn)) {
            return;
        }

        final JavaView view = context.getJavaView();
        final boolean isException = TypeUtils.extendsClass(view, clazz, mExceptionType)
                || TypeUtils.extendsClass(view, clazz, mRuntimeExceptionType);
        if (!isException) {
            return;
        }

        clazz.getMethods().stream()
                .filter(JavaSootMethod::isConcrete)
                .findFirst()
                .ifPresentOrElse(
                        m -> bugs.report(RULE_ID, DEFAULT_SEVERITY,
                                RuleMessageRef.of(MESSAGE_CUSTOM_EXCEPTION, fqn), m),
                        () -> bugs.report(RULE_ID, DEFAULT_SEVERITY,
                                RuleMessageRef.of(MESSAGE_CUSTOM_EXCEPTION, fqn), clazz));
    }

    private static boolean isWhitelistedPackage(final String fqn) {
        for (final String pkg : WHITELISTED_PACKAGES) {
            if (fqn.equals(pkg) || fqn.startsWith(pkg + ".")) {
                return true;
            }
        }
        return false;
    }
}
