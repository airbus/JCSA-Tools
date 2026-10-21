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
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;
import com.airbus.esar.common.JcTypeUtils;
import com.airbus.esar.soot.CallGraphUtils;

/**
 * Reports applets missing register from life-cycle
 */
public final class JcRegisterMissingLifeCycle implements ProgramVisitorIf {
    public static final String RULE_ID = "JcRegisterMissingLifeCycle";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.ERROR;
    public static final String MESSAGE_REGISTER_MISSING = "javacard.register.missing";

    private static final String JAVACARD_APPLET_SIGNATURE = JcTypeUtils.APPLET_FQN;
    private ClassType mAppletClassType = null;


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
        mAppletClassType = view.getIdentifierFactory().getClassType(JAVACARD_APPLET_SIGNATURE);
    }

    @Override
    public void endAnalysis(final AnalysisContextIf context, final BugReporterIf bugs) {
        mAppletClassType = null;
    }

    @Override
    public void visitClass(final JavaSootClass clazz,
                           final AnalysisContextIf context,
                           final BugReporterIf bugs) {
        if (mAppletClassType == null) {
            return;
        }

        if (!JcTypeUtils.isConcreteApplet(context.getJavaView(), clazz, mAppletClassType)) {
            return;
        }

        final JavaView view = context.getJavaView();
        final MethodSignature installSig = view.getIdentifierFactory().getMethodSignature(
            clazz.getType(),
            "install",
            "void",
            List.of("byte[]", "short", "byte")
        );

        // The class does not have install method at all
        if (view.getMethod(installSig).isEmpty()) {
            return;
        }

        final ClassType appletType = clazz.getType();
        final boolean registerFound = CallGraphUtils.visitReachableInvokes(view, installSig,
            sig -> visitMethod(sig, context, appletType));

        if (!registerFound) {
            final JavaSootMethod method = view.getMethod(installSig).get();
            bugs.report(RULE_ID, DEFAULT_SEVERITY,
                    RuleMessageRef.of(MESSAGE_REGISTER_MISSING), method,
                    method.getBody().getFirstNonIdentityStmt());
        }
    }

    private boolean visitMethod(final MethodSignature signature,
                                final AnalysisContextIf context,
                                final ClassType appletType) {
        final JavaView view = context.getJavaView();
        final TypeHierarchy typeHierarchy = context.getTypeHierarchy();
        final Optional<JavaSootMethod> methodOpt = view.getMethod(signature);
        if (methodOpt.isEmpty()) {
            return false;
        }
        final JavaSootMethod method = methodOpt.get();

        // Skip methods that do not have body
        if (!method.hasBody()) {
            return false;
        }
        for (final Stmt stmt : method.getBody().getStmtGraph()) {
            if (!(stmt instanceof InvokableStmt)) {
                continue;
            }
            final Optional<AbstractInvokeExpr> inv = ((InvokableStmt) stmt).getInvokeExpr();
            if (inv.isEmpty()) {
                continue;
            }
            final MethodSignature callee = inv.get().getMethodSignature();
            if (!"register".equals(callee.getName())) {
                continue;
            }
            final ClassType decl = callee.getDeclClassType();
            if (!decl.equals(mAppletClassType)
                    && !decl.equals(appletType)
                    && !typeHierarchy.isSubtype(mAppletClassType, decl)) {
                continue;
            }
            return true;
        }

        return false;
    }
}
