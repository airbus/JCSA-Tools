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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JThrowStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;
import com.airbus.esar.soot.TypeUtils;

/**
 * Exceptional successors limited to types the statement can throw, and to the most specific
 * matching handler (JVM first-match), so a {@code catch} is not also taken as {@code finally}.
 */
public final class TypedExceptionalSuccessors {

    private static final String THROW_IT = "throwIt";

    private TypedExceptionalSuccessors() {
    }

    /**
     * @param graph CFG
     * @param stmt statement whose exceptional edges are filtered
     * @param view view for method bodies and subtype checks
     * @param thrownCache memo of thrown types per callee
     * @return trap type to handler, possibly empty
     */
    public static Map<ClassType, Stmt> filter(final StmtGraph<?> graph,
                                              final Stmt stmt,
                                              final JavaView view,
                                              final Map<MethodSignature, Set<ClassType>> thrownCache) {
        final Map<ClassType, Stmt> traps = graph.exceptionalSuccessors(stmt);
        if (traps.isEmpty()) {
            return traps;
        }
        final Set<ClassType> thrown = thrownTypes(stmt, view, new HashSet<>(), thrownCache);
        if (thrown.isEmpty()) {
            return Map.of();
        }
        final Map<ClassType, Stmt> out = new LinkedHashMap<>();
        for (final ClassType type : thrown) {
            final Map.Entry<ClassType, Stmt> best = mostSpecificHandler(view, type, traps);
            if (best != null) {
                out.put(best.getKey(), best.getValue());
            }
        }
        return out;
    }

    private static Map.Entry<ClassType, Stmt> mostSpecificHandler(final JavaView view,
                                                                  final ClassType thrown,
                                                                  final Map<ClassType, Stmt> traps) {
        ClassType bestType = null;
        Stmt bestHandler = null;
        for (final Map.Entry<ClassType, Stmt> entry : traps.entrySet()) {
            if (!TypeUtils.isSubtypeOrSame(view, thrown, entry.getKey())) {
                continue;
            }
            if (bestType == null || TypeUtils.isSubtypeOrSame(view, entry.getKey(), bestType)) {
                bestType = entry.getKey();
                bestHandler = entry.getValue();
            }
        }
        if (bestType == null) {
            return null;
        }
        return Map.entry(bestType, bestHandler);
    }

    private static Set<ClassType> thrownTypes(final Stmt stmt,
                                              final JavaView view,
                                              final Set<MethodSignature> onStack,
                                              final Map<MethodSignature, Set<ClassType>> thrownCache) {
        if (stmt instanceof JThrowStmt throwStmt) {
            return throwTypesOf(throwStmt.getOp().getType());
        }
        if (!(stmt instanceof InvokableStmt invokable) || invokable.getInvokeExpr().isEmpty()) {
            return Set.of();
        }
        return invokeThrownTypes(invokable.getInvokeExpr().get(), view, onStack, thrownCache);
    }

    private static Set<ClassType> invokeThrownTypes(final AbstractInvokeExpr invoke,
                                                    final JavaView view,
                                                    final Set<MethodSignature> onStack,
                                                    final Map<MethodSignature, Set<ClassType>> thrownCache) {
        final MethodSignature sig = invoke.getMethodSignature();
        if (THROW_IT.equals(sig.getName())) {
            return Set.of(sig.getDeclClassType());
        }
        return methodThrownTypes(sig, view, onStack, thrownCache);
    }

    private static Set<ClassType> methodThrownTypes(final MethodSignature sig,
                                                    final JavaView view,
                                                    final Set<MethodSignature> onStack,
                                                    final Map<MethodSignature, Set<ClassType>> thrownCache) {
        final Set<ClassType> cached = thrownCache.get(sig);
        if (cached != null) {
            return cached;
        }
        if (!onStack.add(sig)) {
            return Set.of();
        }
        final Set<ClassType> types = new HashSet<>();
        try {
            final Optional<JavaSootMethod> method = view.getMethod(sig);
            if (method.isPresent() && method.get().hasBody()) {
                final Body body = method.get().getBody();
                for (final Stmt stmt : body.getStmts()) {
                    types.addAll(thrownTypes(stmt, view, onStack, thrownCache));
                }
            }
        } finally {
            onStack.remove(sig);
        }
        final Set<ClassType> frozen = Set.copyOf(types);
        thrownCache.put(sig, frozen);
        return frozen;
    }

    private static Set<ClassType> throwTypesOf(final Type type) {
        if (type instanceof ClassType classType) {
            return Set.of(classType);
        }
        return Set.of();
    }
}
