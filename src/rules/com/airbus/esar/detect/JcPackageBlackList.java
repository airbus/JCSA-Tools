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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JInstanceOfExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JNewMultiArrayExpr;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;

/**
 * Reports references to types belonging to blacklisted packages
 */
public final class JcPackageBlackList implements ProgramVisitorIf {
    public static final String RULE_ID = "JcPackageBlackList";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.ERROR;
    public static final String MESSAGE_BLACKLISTED_API = "javacard.api.blacklist";

    private static final List<String> DEFAULT_BLACKLISTED_PREFIXES = List.of(
            "java.rmi.",
            "javacard.framework.service."
    );

    private final List<String> mBlacklistedPrefixes;

    public JcPackageBlackList() {
        mBlacklistedPrefixes = List.copyOf(DEFAULT_BLACKLISTED_PREFIXES);
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
        // Don't check black listed classes itself
        if (isBlackListed(clazz.getType().getFullyQualifiedName())) {
            return;
        }
        clazz.getSuperclass().ifPresent(sc -> checkType(sc, clazz, bugs));
        for (final ClassType iface : clazz.getInterfaces()) {
            checkType(iface, clazz, bugs);
        }
    }

    @Override
    public void visitMethod(final JavaSootMethod method,
                            final Body body,
                            final AnalysisContextIf context,
                            final BugReporterIf bugs) {
        if (isBlackListed(method.getDeclClassType().getFullyQualifiedName())) {
            return;
        }
        final Set<String> seen = new HashSet<>();
        for (final Stmt stmt : body.getStmtGraph()) {
            for (final Type t : collectReferencedTypes(stmt)) {
                final String fqn = extractFqn(t);
                if (fqn != null && seen.add(fqn)) {
                    checkType(fqn, method, stmt, bugs);
                }
            }
        }
    }

    private static Set<Type> collectReferencedTypes(final Stmt stmt) {
        final Set<Type> types = new HashSet<>();
        stmt.getUsesAndDefs().forEach(v -> collectFromValue(v, types));
        return types;
    }

    private static void collectFromValue(final Value value, final Set<Type> out) {
        switch (value) {
        case AbstractInvokeExpr val:
            out.add(val.getMethodSignature().getDeclClassType());
            out.add(val.getMethodSignature().getType());
            val.getMethodSignature().getParameterTypes().forEach(out::add);
            break;
        case JNewExpr val:
            out.add((val).getType());
            break;
        case JCastExpr val:
            out.add((val).getType());
            break;
        case JInstanceOfExpr val:
            out.add((val).getCheckType());
            break;
        case JNewArrayExpr val:
            out.add((val).getBaseType());
            break;
        case JNewMultiArrayExpr val:
            out.add((val).getBaseType());
            break;
        case JFieldRef val:
            out.add(val.getFieldSignature().getDeclClassType());
            out.add(val.getFieldSignature().getType());
            break;
        default:
            break;
        }
    }

    private static String extractFqn(final Type type) {
        Type t = type;
        while (t instanceof ArrayType) {
            t = ((ArrayType) t).getBaseType();
        }
        if (t instanceof ClassType) {
            return ((ClassType) t).getFullyQualifiedName();
        }
        return null;
    }

    private void checkType(final String fqn,
                           final JavaSootMethod method,
                           final Stmt stmt,
                           final BugReporterIf bugs) {
        if (!isBlackListed(fqn)) {
            return;
        }

        bugs.report(RULE_ID, DEFAULT_SEVERITY,
                RuleMessageRef.of(MESSAGE_BLACKLISTED_API, fqn), method, stmt);
    }

    private void checkType(final ClassType type,
                           final JavaSootClass sourceClazz,
                           final BugReporterIf bugs) {
        final String fqn = type.getFullyQualifiedName();
        if (!isBlackListed(fqn)) {
            return;
        }
        bugs.report(RULE_ID, DEFAULT_SEVERITY,
                RuleMessageRef.of(MESSAGE_BLACKLISTED_API, fqn), sourceClazz);
    }

    private boolean isBlackListed(final String fqn) {
        for (final String prefix : mBlacklistedPrefixes) {
            if (!fqn.startsWith(prefix)) {
                continue;
            }
            return true;
        }
        return false;
    }
}
