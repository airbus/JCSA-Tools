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

package com.airbus.esar.analysis.callflow;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

/**
 * Pattern that matches a method invocation by its declared signature and dispatch kind (static, instance, or any).
 *
 * Declaring class, method name, and parameter types are always compared. The return type is compared too unless
 * the pattern was built with one of the ignoringReturn factories.
 */
public final class InvokeSignaturePattern {

    /**
     * Which invoke shapes are accepted.
     */
    public enum Kind {
        /** Static invokes only. */
        STATIC,
        /** Virtual, interface, and special invokes. */
        INSTANCE,
        /** Any invoke. */
        ANY
    }

    private final Kind mKind;
    private final String mDeclaringClassFqn;
    private final String mMethodName;
    private final List<Type> mParameterTypes;
    private final String mReturnClassFqn;

    private InvokeSignaturePattern(final Kind kind,
                                   final String declaringClassFqn,
                                   final String methodName,
                                   final List<Type> parameterTypes,
                                   final String returnClassFqn) {
        mKind = Objects.requireNonNull(kind, "kind");
        mDeclaringClassFqn = Objects.requireNonNull(declaringClassFqn, "declaringClassFqn");
        mMethodName = Objects.requireNonNull(methodName, "methodName");
        mParameterTypes = List.copyOf(parameterTypes);
        mReturnClassFqn = returnClassFqn;
    }

    public static InvokeSignaturePattern staticCall(final String declaringClassFqn,
                                                    final String methodName,
                                                    final String returnClassFqn,
                                                    final Type... parameterTypes) {
        return new InvokeSignaturePattern(
            Kind.STATIC,
            declaringClassFqn, methodName,
            Arrays.asList(parameterTypes),
            returnClassFqn
        );
    }

    public static InvokeSignaturePattern staticCallIgnoringReturn(final String declaringClassFqn,
                                                                  final String methodName,
                                                                  final Type... parameterTypes) {
        return new InvokeSignaturePattern(
            Kind.STATIC,
            declaringClassFqn,
            methodName,
            Arrays.asList(parameterTypes),
            null
        );
    }

    public static InvokeSignaturePattern instanceCall(final String declaringClassFqn,
                                                      final String methodName,
                                                      final String returnClassFqn,
                                                      final Type... parameterTypes) {
        return new InvokeSignaturePattern(
            Kind.INSTANCE,
            declaringClassFqn,
            methodName,
            Arrays.asList(parameterTypes),
            returnClassFqn
        );
    }

    public static InvokeSignaturePattern instanceCallIgnoringReturn(final String declaringClassFqn,
                                                                    final String methodName,
                                                                    final Type... parameterTypes) {
        return new InvokeSignaturePattern(
            Kind.INSTANCE,
            declaringClassFqn,
            methodName,
            Arrays.asList(parameterTypes),
            null
        );
    }

    public static InvokeSignaturePattern anyCall(final String declaringClassFqn,
                                                 final String methodName,
                                                 final String returnClassFqn,
                                                 final Type... parameterTypes) {
        return new InvokeSignaturePattern(
            Kind.ANY,
            declaringClassFqn,
            methodName,
            Arrays.asList(parameterTypes),
            returnClassFqn
        );
    }

    public static InvokeSignaturePattern anyCallIgnoringReturn(final String declaringClassFqn,
                                                               final String methodName,
                                                               final Type... parameterTypes) {
        return new InvokeSignaturePattern(
            Kind.ANY,
            declaringClassFqn,
            methodName,
            Arrays.asList(parameterTypes),
            null
        );
    }

    public Kind getKind() {
        return mKind;
    }

    public boolean matches(final Stmt stmt) {
        if (!(stmt instanceof InvokableStmt)) {
            return false;
        }
        final Optional<AbstractInvokeExpr> inv = ((InvokableStmt) stmt).getInvokeExpr();
        return inv.isPresent() && matches(inv.get());
    }

    public boolean matches(final AbstractInvokeExpr expr) {
        if (expr == null) {
            return false;
        }
        switch (mKind) {
        case STATIC:
            if (!(expr instanceof JStaticInvokeExpr)) {
                return false;
            }
            break;
        case INSTANCE:
            if (!(expr instanceof AbstractInstanceInvokeExpr)) {
                return false;
            }
            break;
        case ANY:
        default:
            break;
        }
        return matchesSignature(expr.getMethodSignature());
    }

    // Matches by declared signature only (dispatch kind isn't part of a MethodSignature).
    public boolean matchesSignature(final MethodSignature sig) {
        if (sig == null) {
            return false;
        }
        if (!mDeclaringClassFqn.equals(sig.getDeclClassType().getFullyQualifiedName())) {
            return false;
        }
        if (!mMethodName.equals(sig.getName())) {
            return false;
        }
        if (sig.getParameterCount() != mParameterTypes.size()) {
            return false;
        }
        for (int i = 0; i < sig.getParameterCount(); i++) {
            if (!sig.getParameterType(i).equals(mParameterTypes.get(i))) {
                return false;
            }
        }
        if (mReturnClassFqn == null) {
            return true;
        }
        final Type ret = sig.getType();
        if (!(ret instanceof ClassType)) {
            return false;
        }
        return mReturnClassFqn.equals(((ClassType) ret).getFullyQualifiedName());
    }

    public static boolean anyMatches(final Stmt stmt, final List<InvokeSignaturePattern> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        for (final InvokeSignaturePattern p : patterns) {
            if (p.matches(stmt)) {
                return true;
            }
        }
        return false;
    }

    public static boolean anyMatchesSignature(final MethodSignature sig,
                                              final List<InvokeSignaturePattern> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        for (final InvokeSignaturePattern p : patterns) {
            if (p.matchesSignature(sig)) {
                return true;
            }
        }
        return false;
    }
}
