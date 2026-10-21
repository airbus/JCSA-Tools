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

import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;
import com.airbus.esar.common.JcTypeUtils;
import com.airbus.esar.soot.TypeUtils;

public final class JcShareableMultiSelectable implements ProgramVisitorIf {
    public static final String RULE_ID = "JcShareableMultiSelectable";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.ERROR;
    public static final String MESSAGE_MUST_BE_MULTISELECTABLE =
            "javacard.shareable.mustBeMultiselectable";

    private static final String MULTISELECTABLE_FQN = "javacard.framework.MultiSelectable";
    private static final String GET_SIO_NAME = "getShareableInterfaceObject";
    private static final List<String> GET_SIO_PARAM_TYPES =
            List.of("javacard.framework.AID", "byte");

    private ClassType mAppletType;
    private ClassType mMultiSelectableType;


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
        mMultiSelectableType = view.getIdentifierFactory().getClassType(MULTISELECTABLE_FQN);
    }

    @Override
    public void endAnalysis(final AnalysisContextIf context, final BugReporterIf bugs) {
        mAppletType = null;
        mMultiSelectableType = null;
    }

    @Override
    public void visitClass(final JavaSootClass clazz,
                           final AnalysisContextIf context,
                           final BugReporterIf bugs) {
        if (mAppletType == null) {
            return;
        }

        if (!JcTypeUtils.isConcreteApplet(context.getJavaView(), clazz, mAppletType)) {
            return;
        }

        final Optional<JavaSootMethod> getSio = findGetShareableInterfaceObject(clazz, context);
        if (getSio.isEmpty()) {
            return;
        }

        if (!TypeUtils.implementsInterface(context.getJavaView(), clazz, mMultiSelectableType)) {
            bugs.report(RULE_ID, DEFAULT_SEVERITY,
                    RuleMessageRef.of(MESSAGE_MUST_BE_MULTISELECTABLE), getSio.get());
        }
    }

    private Optional<JavaSootMethod> findGetShareableInterfaceObject(
            final JavaSootClass clazz, final AnalysisContextIf context) {
        final JavaView view = context.getJavaView();
        return view.getMethod(view.getIdentifierFactory().getMethodSignature(
                clazz.getType(), GET_SIO_NAME,
                "javacard.framework.Shareable", GET_SIO_PARAM_TYPES));
    }
}
