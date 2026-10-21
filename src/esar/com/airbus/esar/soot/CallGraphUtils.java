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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import sootup.callgraph.CallGraph;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

public final class CallGraphUtils {
    /**
     * Visit callees of a method and return true or false based on the predicate
     *
     * @param cg call graph
     * @param entry entry method signature
     * @param visitor predicate method
     *
     * @return true when predicate returns true
     */
    public static boolean visitCalleesOfMethod(final CallGraph cg,
                                               final MethodSignature entry,
                                               final Predicate<MethodSignature> visitor) {
        final Deque<MethodSignature> q = new ArrayDeque<>();
        final Set<MethodSignature> seen = new HashSet<>();
        q.add(entry);

        while (!q.isEmpty()) {
            final MethodSignature m = q.removeFirst();
            if (!seen.add(m)) {
                continue;
            }
            if (visitor.test(m)) {
                return true;
            }
            if (!cg.containsMethod(m)) {
                continue;
            }
            for (MethodSignature t : cg.callTargetsFrom(m)) {
                q.addLast(t);
            }
        }
        return false;
    }

    /**
     * Visits {@code entry} and methods reachable by following explicit invoke targets in Jimple bodies.
     * Invoke targets that name a subclass but are implemented on a superclass are resolved to the
     * declaring class that has a body (bytecode often keeps the receiver's compile-time type).
     *
     * @param view view that resolves {@link JavaSootMethod} bodies
     * @param entry root signature to start from
     * @param visitor invoked for each dequeued signature; if it returns true, traversal stops
     * @return true if {@code visitor} returned true for some visited signature
     */
    public static boolean visitReachableInvokes(final JavaView view,
                                                final MethodSignature entry,
                                                final Predicate<MethodSignature> visitor) {
        final Deque<MethodSignature> q = new ArrayDeque<>();
        final Set<MethodSignature> seen = new HashSet<>();
        q.add(entry);
        while (!q.isEmpty()) {
            final MethodSignature m = q.removeFirst();
            final MethodSignature resolved = resolveMethodWithBody(view, m).orElse(m);
            if (!seen.add(resolved)) {
                continue;
            }
            if (visitor.test(resolved)) {
                return true;
            }
            final Optional<JavaSootMethod> mo = view.getMethod(resolved);
            if (mo.isEmpty() || !mo.get().hasBody()) {
                continue;
            }
            enqueueInvokeTargets(mo.get(), q);
        }
        return false;
    }

    /**
     * Finds a method with a Jimple body for {@code sig}, walking superclasses when the named
     * declaring class does not define the method (inherited methodrefs).
     *
     * @param view java view
     * @param sig invoke target signature
     * @return signature of a method with a body, or empty
     */
    private static Optional<MethodSignature> resolveMethodWithBody(final JavaView view,
                                                                   final MethodSignature sig) {
        final Optional<JavaSootMethod> direct = view.getMethod(sig);
        if (direct.isPresent() && direct.get().hasBody()) {
            return Optional.of(direct.get().getSignature());
        }
        final Optional<JavaSootClass> start = view.getClass(sig.getDeclClassType());
        if (start.isEmpty()) {
            return Optional.empty();
        }
        Optional<JavaClassType> current = start.get().getSuperclass();
        while (current.isPresent()) {
            final ClassType superType = current.get();
            final MethodSignature candidate = view.getIdentifierFactory().getMethodSignature(
                    superType,
                    sig.getName(),
                    sig.getType(),
                    sig.getParameterTypes());
            final Optional<JavaSootMethod> mo = view.getMethod(candidate);
            if (mo.isPresent() && mo.get().hasBody()) {
                return Optional.of(mo.get().getSignature());
            }
            final Optional<JavaSootClass> superClass = view.getClass(superType);
            if (superClass.isEmpty()) {
                return Optional.empty();
            }
            current = superClass.get().getSuperclass();
        }
        return Optional.empty();
    }

    private static void enqueueInvokeTargets(final JavaSootMethod m, final Deque<MethodSignature> q) {
        for (final Stmt stmt : m.getBody().getStmtGraph()) {
            if (!(stmt instanceof InvokableStmt)) {
                continue;
            }
            final Optional<AbstractInvokeExpr> inv = ((InvokableStmt) stmt).getInvokeExpr();
            if (inv.isEmpty()) {
                continue;
            }
            q.addLast(inv.get().getMethodSignature());
        }
    }
}
