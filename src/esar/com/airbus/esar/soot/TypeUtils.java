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

import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

public final class TypeUtils {
    private TypeUtils() {
    }

    /**
     * Walks the superclass chain of {@code clazz} looking for {@code target}.
     * Uses only {@link JavaSootClass#getSuperclass()} (class-file based), so it
     * works reliably on stripped API JARs where {@code TypeHierarchy.isSubtype}
     * may not.
     *
     * @param view  view to resolve intermediate classes
     * @param clazz starting class (not checked itself)
     * @param target superclass to look for
     * @return true if {@code target} appears anywhere in the superclass chain
     */
    public static boolean extendsClass(final JavaView view,
                                       final JavaSootClass clazz,
                                       final ClassType target) {
        Optional<JavaClassType> current = clazz.getSuperclass();
        while (current.isPresent()) {
            final JavaClassType superType = current.get();
            if (superType.equals(target)) {
                return true;
            }
            final Optional<JavaSootClass> superClass = view.getClass(superType);
            if (superClass.isEmpty()) {
                return false;
            }
            current = superClass.get().getSuperclass();
        }
        return false;
    }

    /**
     * @param view view to resolve {@code type}
     * @param type candidate subtype
     * @param ancestor candidate superclass
     * @return true when {@code type} is {@code ancestor} or extends it
     */
    public static boolean isSubtypeOrSame(final JavaView view,
                                          final ClassType type,
                                          final ClassType ancestor) {
        if (type.equals(ancestor)) {
            return true;
        }
        final Optional<JavaSootClass> clazz = view.getClass(type);
        if (clazz.isEmpty()) {
            return false;
        }
        return extendsClass(view, clazz.get(), ancestor);
    }

    /**
     * Checks whether {@code clazz} implements {@code target} anywhere in its
     * hierarchy (directly declared interfaces, interfaces of superclasses, and
     * super-interfaces).
     *
     * @param view   view to resolve intermediate classes/interfaces
     * @param clazz  starting class
     * @param target interface to look for
     * @return true if {@code target} is found in the interface hierarchy
     */
    public static boolean implementsInterface(final JavaView view,
                                              final JavaSootClass clazz,
                                              final ClassType target) {
        final Set<ClassType> visited = new HashSet<>();
        final Deque<ClassType> queue = new ArrayDeque<>();

        collectInterfaces(clazz, queue);
        addSuperclassInterfaces(view, clazz, queue);

        while (!queue.isEmpty()) {
            final ClassType iface = queue.poll();
            if (iface.equals(target)) {
                return true;
            }
            if (!visited.add(iface)) {
                continue;
            }
            final Optional<JavaSootClass> resolved = view.getClass(iface);
            if (resolved.isPresent()) {
                collectInterfaces(resolved.get(), queue);
            }
        }
        return false;
    }

    private static void collectInterfaces(final JavaSootClass clazz,
                                          final Deque<ClassType> queue) {
        for (final ClassType iface : clazz.getInterfaces()) {
            queue.add(iface);
        }
    }

    private static void addSuperclassInterfaces(final JavaView view,
                                                final JavaSootClass clazz,
                                                final Deque<ClassType> queue) {
        Optional<JavaClassType> current = clazz.getSuperclass();
        while (current.isPresent()) {
            final Optional<JavaSootClass> superClass = view.getClass(current.get());
            if (superClass.isEmpty()) {
                return;
            }
            collectInterfaces(superClass.get(), queue);
            current = superClass.get().getSuperclass();
        }
    }
}
