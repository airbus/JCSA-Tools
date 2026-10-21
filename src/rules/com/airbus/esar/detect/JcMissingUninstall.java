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

import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;
import com.airbus.esar.common.JcTypeUtils;
import com.airbus.esar.soot.TypeUtils;

/**
 * Every applet must implement AppletEvent (which provides uninstall())
 */
public final class JcMissingUninstall implements ProgramVisitorIf {
    public static final String RULE_ID = "JcMissingUninstall";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.ERROR;
    public static final String MESSAGE_MISSING_UNINSTALL = "javacard.applet.missingUninstall";

    private static final String APPLET_EVENT_FQN = "javacard.framework.AppletEvent";
    private static final String INSTALL = "install";
    private static final String INIT = "<init>";

    private ClassType mAppletType;
    private ClassType mAppletEventType;


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
        mAppletType = view.getIdentifierFactory().getClassType(JcTypeUtils.APPLET_FQN);
        mAppletEventType = view.getIdentifierFactory().getClassType(APPLET_EVENT_FQN);
    }

    @Override
    public void endAnalysis(final AnalysisContextIf context, final BugReporterIf bugs) {
        mAppletType = null;
        mAppletEventType = null;
    }

    @Override
    public void visitClass(final JavaSootClass clazz,
                           final AnalysisContextIf context,
                           final BugReporterIf bugs) {
        if (mAppletType == null) {
            return;
        }

        final JavaView view = context.getJavaView();
        if (!JcTypeUtils.isConcreteApplet(view, clazz, mAppletType)) {
            return;
        }

        if (TypeUtils.implementsInterface(view, clazz, mAppletEventType)) {
            return;
        }

        final Optional<JavaSootMethod> anchor = reportAnchor(clazz);
        if (anchor.isPresent()) {
            bugs.report(RULE_ID, DEFAULT_SEVERITY, RuleMessageRef.of(MESSAGE_MISSING_UNINSTALL), anchor.get());
        } else {
            bugs.report(RULE_ID, DEFAULT_SEVERITY, RuleMessageRef.of(MESSAGE_MISSING_UNINSTALL), clazz);
        }
    }

    /**
     * Prefer {@code install(byte[],short,byte)}, then a concrete {@code <init>}, else empty (report on class).
     *
     * @param clazz applet class under inspection
     * @return preferred method for SARIF location, if any
     */
    private static Optional<JavaSootMethod> reportAnchor(final JavaSootClass clazz) {
        final Optional<JavaSootMethod> install = clazz.getMethodsByName(INSTALL).stream()
                .filter(JavaSootMethod::isConcrete)
                .filter(JcMissingUninstall::isStandardInstall)
                .findFirst();
        if (install.isPresent()) {
            return install;
        }
        return clazz.getMethodsByName(INIT).stream()
                .filter(JavaSootMethod::isConcrete)
                .findFirst();
    }

    private static boolean isStandardInstall(final JavaSootMethod method) {
        final List<Type> params = method.getParameterTypes();
        if (params.size() != 3) {
            return false;
        }
        return params.get(0) instanceof ArrayType arr
                && arr.getBaseType().equals(PrimitiveType.getByte())
                && params.get(1).equals(PrimitiveType.getShort())
                && params.get(2).equals(PrimitiveType.getByte());
    }
}
