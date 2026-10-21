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

import java.util.Arrays;
import java.util.List;

import sootup.core.types.Type;

/**
 * Static method shape for InvokeMatchers (no string parsing of signatures).
 */
public final class StaticInvokeSignature {

    private final String mDeclaringClassFqn;
    private final String mMethodName;
    private final List<Type> mParameterTypes;
    private final String mReturnClassFqn;

    private StaticInvokeSignature(final String declaringClassFqn,
                                  final String methodName,
                                  final List<Type> parameterTypes,
                                  final String returnClassFqn) {
        mDeclaringClassFqn = declaringClassFqn;
        mMethodName = methodName;
        mParameterTypes = List.copyOf(parameterTypes);
        mReturnClassFqn = returnClassFqn;
    }

    /**
     * @param declaringClassFqn fully qualified name of the declaring class
     * @param methodName simple method name
     * @param returnClassFqn if non-null, return type must be that class; if null, return type is not checked
     * @param parameterTypes formal parameter types in order
     * @return pattern for matching MethodSignature / static invokes
     */
    public static StaticInvokeSignature of(
            final String declaringClassFqn,
            final String methodName,
            final String returnClassFqn,
            final Type... parameterTypes) {
        return new StaticInvokeSignature(declaringClassFqn, methodName, Arrays.asList(parameterTypes), returnClassFqn);
    }

    /**
     * Like of(...) but return type is not checked.
     *
     * @param declaringClassFqn fully qualified name of the declaring class
     * @param methodName simple method name
     * @param parameterTypes formal parameter types in order
     * @return pattern that ignores the return type
     */
    public static StaticInvokeSignature ignoringReturn(
            final String declaringClassFqn,
            final String methodName,
            final Type... parameterTypes) {
        return new StaticInvokeSignature(declaringClassFqn, methodName, Arrays.asList(parameterTypes), null);
    }

    /**
     * @return declaring class fully qualified name
     */
    public String getDeclaringClassFqn() {
        return mDeclaringClassFqn;
    }

    /**
     * @return method name
     */
    public String getMethodName() {
        return mMethodName;
    }

    /**
     * @return unmodifiable parameter type list
     */
    public List<Type> getParameterTypes() {
        return mParameterTypes;
    }

    /**
     * @return expected return class FQN, or null when return type is not part of the match
     */
    public String getReturnClassFqn() {
        return mReturnClassFqn;
    }
}
