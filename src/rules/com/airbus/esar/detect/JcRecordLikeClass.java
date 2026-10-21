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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.VoidType;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;

/** Flags concrete types whose declared API is only field-read getters and field-write setters. */
public final class JcRecordLikeClass implements ProgramVisitorIf {
    public static final String RULE_ID = "JcRecordLikeClass";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.WARNING;
    public static final String MESSAGE_KEY = "javacard.recordlike.settergetter";

    /**
     * @param method candidate instance method
     * @return true if the body only loads one instance field and returns that value
     */
    public static boolean isGetter(final JavaSootMethod method) {
        if (method.isStatic() || isConstructorOrClassInit(method)) {
            return false;
        }
        if (method.getParameterCount() != 0) {
            return false;
        }
        if (method.getReturnType().equals(VoidType.getInstance())) {
            return false;
        }
        return method.hasBody() && isGetterBody(method.getBody());
    }

    /**
     * @param method candidate instance method
     * @return true if the body only stores the sole parameter into one instance field then returns void
     */
    public static boolean isSetter(final JavaSootMethod method) {
        if (method.isStatic() || isConstructorOrClassInit(method)) {
            return false;
        }
        if (method.getParameterCount() != 1) {
            return false;
        }
        if (!method.getReturnType().equals(VoidType.getInstance())) {
            return false;
        }
        return method.hasBody() && isSetterBody(method.getBody());
    }

    private static boolean isConstructorOrClassInit(final JavaSootMethod method) {
        final String n = method.getName();
        return "<init>".equals(n) || "<clinit>".equals(n);
    }

    // Non-identity stmts must be: assign local from JInstanceFieldRef, then return that local.
    private static boolean isGetterBody(final Body body) {
        final List<Stmt> stmts = nonIdentityStmts(body);
        if (stmts.size() != 2) {
            return false;
        }
        if (!(stmts.get(0) instanceof final JAssignStmt assign)) {
            return false;
        }
        if (!(assign.getLeftOp() instanceof final Local retLocal)) {
            return false;
        }
        if (!(assign.getRightOp() instanceof JInstanceFieldRef)) {
            return false;
        }
        if (!(stmts.get(1) instanceof final JReturnStmt ret)) {
            return false;
        }
        return ret.getOp() instanceof final Local l && l.equivTo(retLocal);
    }

    // Non-identity stmts must be: assign JInstanceFieldRef from the parameter local, then return void.
    private static boolean isSetterBody(final Body body) {
        final List<Stmt> stmts = nonIdentityStmts(body);
        if (stmts.size() != 2) {
            return false;
        }
        if (!(stmts.get(0) instanceof final JAssignStmt assign)) {
            return false;
        }
        if (!(assign.getLeftOp() instanceof JInstanceFieldRef)) {
            return false;
        }
        if (!(assign.getRightOp() instanceof final Local rhs)) {
            return false;
        }
        if (!(stmts.get(1) instanceof JReturnVoidStmt)) {
            return false;
        }
        // RHS must be the parameter local (bound by a @parameter identity stmt).
        for (final Stmt s : body.getStmts()) {
            if (s instanceof final JIdentityStmt id
                    && id.getRightOp() instanceof JParameterRef
                    && id.getLeftOp() instanceof final Local paramLocal
                    && paramLocal.equivTo(rhs)) {
                return true;
            }
        }
        return false;
    }

    private static List<Stmt> nonIdentityStmts(final Body body) {
        final List<Stmt> result = new ArrayList<>();
        for (final Stmt s : body.getStmts()) {
            if (!(s instanceof JIdentityStmt)) {
                result.add(s);
            }
        }
        return result;
    }


    @Override
    public String ruleId() {
        return RULE_ID;
    }

    @Override
    public SarifResultLevel severity() {
        return DEFAULT_SEVERITY;
    }

    @Override
    public void visitClass(final JavaSootClass clazz,
                           final AnalysisContextIf context,
                           final BugReporterIf bugs) {
        boolean sawAccessor = false;
        for (final JavaSootMethod m : clazz.getMethods()) {
            if (isConstructorOrClassInit(m)) {
                continue;
            }
            if (isGetter(m) || isSetter(m)) {
                sawAccessor = true;
                continue;
            }
            return;
        }
        if (!sawAccessor) {
            return;
        }
        final Optional<JavaSootMethod> ctor = clazz.getMethodsByName("<init>").stream().findFirst();
        if (ctor.isPresent()) {
            bugs.report(RULE_ID, DEFAULT_SEVERITY, RuleMessageRef.of(MESSAGE_KEY), ctor.get());
        } else {
            bugs.report(RULE_ID, DEFAULT_SEVERITY, RuleMessageRef.of(MESSAGE_KEY), clazz);
        }
    }
}
