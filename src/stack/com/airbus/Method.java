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

package com.airbus;

import java.util.List;
import java.util.stream.Collectors;

import sootup.core.signatures.MethodSignature;

public class Method {
    private final String mReturnType;
    private final String mName;
    private final List<String> mParameters;
    private final String mClass;

    public Method(final MethodSignature methodSignature) {
        mReturnType = methodSignature.getType().toString();
        mName = methodSignature.getName();
        mParameters = methodSignature.getParameterTypes()
            .stream()
            .map(item -> item.toString())
            .collect(Collectors.toList());
        mClass = methodSignature.getDeclClassType().toString();

    }

    public Method(final String returnType, final String name, final List<String> parameters) {
        mReturnType = returnType;
        mName = name;
        mParameters = parameters;
        mClass = null;
    }

    public String getReturnType() {
        return mReturnType;
    }

    public String getName() {
        return mName;
    }

    public List<String> getParameters() {
        return mParameters;
    }

    public String getDeclaringClass() {
        return mClass;
    }

    public String toString() {
        if (mParameters.isEmpty()) {
            return mReturnType + " " + mName + "()";
        }
        return mReturnType + " " + mName + "(" + String.join(", ", mParameters) + ")";
    }

    public String getClassPath() {
        if (mClass == null)  {
            return null;
        }
        return mClass.replace(".", "/") + JCVMStackAnalyser.CLASS_EXTENSION;
    }

    public boolean matches(final Method method) {
        if (mClass == null || method.getDeclaringClass() == null) {
            return mReturnType.equals(method.getReturnType())
                && mName.equals(method.getName())
                && mParameters.equals(method.getParameters());

        }
        return mReturnType.equals(method.getReturnType())
               && mName.equals(method.getName())
               && mClass.equals(method.getDeclaringClass())
               && mParameters.equals(method.getParameters());
    }
}
