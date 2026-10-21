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

package com.airbus.esar.soot;

import java.util.Optional;

import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.core.types.JavaClassType;

/**
 * Match invokes and MethodSignatures against StaticInvokeSignature patterns (e.g. static factories).
 */
public final class InvokeMatchers {

    private InvokeMatchers() {
    }

    /**
     * @param stmt statement (any subtype)
     * @param patterns alternatives; matches if {@code expr} is a static invoke and its signature matches any pattern
     *
     * @return matching static invoke, return null if no match
     */
    public static AbstractInvokeExpr getMatchingStaticInvoke(final Stmt stmt, final StaticInvokeSignature... patterns) {
        if (!(stmt instanceof InvokableStmt)) {
            return null;
        }
        final Optional<AbstractInvokeExpr> invOpt = ((InvokableStmt) stmt).getInvokeExpr();
        if (invOpt.isEmpty()) {
            return null;
        }
        final AbstractInvokeExpr expr = invOpt.get();

        if (matchesStaticInvoke(expr, patterns)) {
            return expr;
        } else {
            return null;
        }
    }

    /**
     * @param stmt statement (any subtype)
     * @param patterns alternatives; matches static invokes or {@code <init>} special invokes
     * @return matching invoke, or null if no match
     */
    public static AbstractInvokeExpr getMatchingInvoke(final Stmt stmt, final StaticInvokeSignature... patterns) {
        final AbstractInvokeExpr staticMatch = getMatchingStaticInvoke(stmt, patterns);
        if (staticMatch != null) {
            return staticMatch;
        }
        if (!(stmt instanceof InvokableStmt)) {
            return null;
        }
        final Optional<AbstractInvokeExpr> invOpt = ((InvokableStmt) stmt).getInvokeExpr();
        if (invOpt.isEmpty()) {
            return null;
        }
        final AbstractInvokeExpr expr = invOpt.get();
        if (!(expr instanceof JSpecialInvokeExpr)) {
            return null;
        }
        final MethodSignature sig = expr.getMethodSignature();
        if (!"<init>".equals(sig.getName())) {
            return null;
        }
        for (final StaticInvokeSignature p : patterns) {
            if (matchesMethodSignature(sig, p)) {
                return expr;
            }
        }
        return null;
    }

    /**
     * @param expr invoke expression (any subtype)
     * @param patterns alternatives; matches if {@code expr} is a static invoke and its signature matches any pattern
     * @return true for static invoke matching any pattern
     */
    public static boolean matchesStaticInvoke(final AbstractInvokeExpr expr, final StaticInvokeSignature... patterns) {
        if (!(expr instanceof JStaticInvokeExpr)) {
            return false;
        }
        final MethodSignature sig = expr.getMethodSignature();
        for (final StaticInvokeSignature p : patterns) {
            if (matchesMethodSignature(sig, p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param sig method signature from the program
     * @param pattern expected shape
     * @return true if declaring class, name, parameters, and optional return class match
     */
    public static boolean matchesMethodSignature(final MethodSignature sig, final StaticInvokeSignature pattern) {
        if (!pattern.getDeclaringClassFqn().equals(sig.getDeclClassType().getFullyQualifiedName())) {
            return false;
        }
        if (!pattern.getMethodName().equals(sig.getName())) {
            return false;
        }
        if (sig.getParameterCount() != pattern.getParameterTypes().size()) {
            return false;
        }
        for (int i = 0; i < sig.getParameterCount(); i++) {
            if (!parameterTypeMatches(sig.getParameterType(i), pattern.getParameterTypes().get(i))) {
                return false;
            }
        }
        final String retFqn = pattern.getReturnClassFqn();
        if (retFqn == null) {
            return true;
        }
        final Type ret = sig.getType();
        if (!(ret instanceof ClassType)) {
            return false;
        }
        return retFqn.equals(((ClassType) ret).getFullyQualifiedName());
    }

    /**
     * @param fqn fully qualified class name for a reference-typed parameter
     * @return class type for {@link StaticInvokeSignature} patterns
     */
    public static Type classType(final String fqn) {
        final int dot = fqn.lastIndexOf('.');
        if (dot < 0) {
            return new JavaClassType(fqn, PackageName.DEFAULT_PACKAGE);
        }
        return new JavaClassType(fqn.substring(dot + 1), new PackageName(fqn.substring(0, dot)));
    }

    private static boolean parameterTypeMatches(final Type actual, final Type expected) {
        if (actual.equals(expected)) {
            return true;
        }
        if (actual instanceof ClassType actualClass && expected instanceof ClassType expectedClass) {
            return actualClass.getFullyQualifiedName().equals(expectedClass.getFullyQualifiedName());
        }
        return false;
    }
}
