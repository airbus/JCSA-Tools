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

package com.airbus.esar.internal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record EntryPointSpec(String declaringClass,
                             String methodName,
                             String returnType,
                             List<String> parameterTypes) {

    public static EntryPointSpec parse(final String line) {
        final int openParen = line.indexOf('(');
        final int closeParen = line.lastIndexOf(')');

        if (openParen < 0 || closeParen < openParen) {
            throw new IllegalArgumentException("Entry method must contain '(' and ')' " + line);
        }
        final String beforeParen = line.substring(0, openParen).trim();
        final int lastDot = beforeParen.lastIndexOf('.');
        if (lastDot < 0 || lastDot == beforeParen.length() - 1) {
            throw new IllegalArgumentException(
                "Entry method needs declaringClass.method before '(': " + line);
        }
        final String methodName = beforeParen.substring(lastDot + 1);
        final String classAndMaybeReturn = beforeParen.substring(0, lastDot);
        final String returnType;
        final String declaringClass;
        final int firstSpace = classAndMaybeReturn.indexOf(' ');
        if (firstSpace >= 0) {
            returnType = classAndMaybeReturn.substring(0, firstSpace).trim();
            declaringClass = classAndMaybeReturn.substring(firstSpace + 1).trim();
            if (declaringClass.isEmpty()) {
                throw new IllegalArgumentException("Missing declaring class after return type: " + line);
            }
        } else {
            returnType = "void";
            declaringClass = classAndMaybeReturn;
        }
        final String inside = line.substring(openParen + 1, closeParen).trim();
        final List<String> parameterTypes;
        if (inside.isEmpty()) {
            parameterTypes = List.of();
        } else {
            final List<String> params = new ArrayList<>();
            for (final String part : inside.split(",")) {
                final String t = part.trim();
                if (!t.isEmpty()) {
                    params.add(t);
                }
            }
            parameterTypes = Collections.unmodifiableList(params);
        }
        return new EntryPointSpec(declaringClass, methodName, returnType, parameterTypes);
    }
}
