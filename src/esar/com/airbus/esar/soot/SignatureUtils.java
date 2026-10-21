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

import java.util.ArrayList;
import java.util.List;

import sootup.core.signatures.MethodSignature;
import sootup.java.core.views.JavaView;
import sootup.core.types.ClassType;

public final class SignatureUtils {
    private SignatureUtils() {
        // NOP
    }
    /**
     * @param view java view where method exists
     * @param declaringClass class that declares the method
     * @param methodName simple name
     * @param returnType JVM-style type name as SootUp expects (e.g. {@code void}, {@code byte})
     * @param parameterTypes fully qualified parameter types in declaration order (e.g.
     *     {@code javacard.framework.APDU})
     * @return MethodSignature resolved against harness classpath
     */
    public static MethodSignature methodSignature(final JavaView view,
                                                  final Class<?> declaringClass,
                                                  final String methodName,
                                                  final String returnType,
                                                  final List<String> parameterTypes) {
        final ClassType classType = view.getIdentifierFactory().getClassType(declaringClass.getName());
        return view.getIdentifierFactory().getMethodSignature(classType, methodName, returnType, parameterTypes);
    }

    /**
     * Entry signatures for {@code void} methods with no parameters on {@code fixtureClass} (common for static test
     * helpers that should be CHA roots).
     *
     * @param view java view where method exists
     * @param declaringClass declaring class
     * @param voidNoArgMethodNames e.g. {@code "dataflowPseudo"}
     * @return list of method signatures, same order as {@code voidNoArgMethodNames}
     */
    public static List<MethodSignature> sigVoidNoArg(final JavaView view,
                                                     final Class<?> declaringClass,
                                                     final String... voidNoArgMethodNames) {
        if (voidNoArgMethodNames.length == 0) {
            throw new IllegalArgumentException("At least one method name is required");
        }
        final List<MethodSignature> out = new ArrayList<>(voidNoArgMethodNames.length);
        for (final String name : voidNoArgMethodNames) {
            out.add(methodSignature(view, declaringClass, name, "void", List.of()));
        }
        return List.copyOf(out);
    }
}
