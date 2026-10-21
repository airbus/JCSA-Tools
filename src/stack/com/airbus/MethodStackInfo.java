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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class MethodStackInfo {
    private final Method mMethod;
    private final int mMaxStack;
    private final int mMaxLocals;
    private final int mFrameSize;
    private final List<LocalInfo> mLocals;
    private final int mParametersSize;
    private final Map<String, Integer> mCallSiteStackDepths;

    public MethodStackInfo(final Method method,
                           final int maxStack,
                           final int maxLocals,
                           final List<LocalInfo> locals,
                           final int parametersSize) {
        this(method, maxStack, maxLocals, locals, parametersSize, new HashMap<>());
    }

    public MethodStackInfo(final Method method,
                           final int maxStack,
                           final int maxLocals,
                           final List<LocalInfo> locals,
                           final int parametersSize,
                           final Map<String, Integer> callSiteStackDepths) {
        mMethod = method;
        mMaxStack = maxStack;
        mMaxLocals = maxLocals;
        mFrameSize = maxStack + maxLocals + JCVMStackAnalyser.METHOD_OVERHEAD_COSMO;
        mLocals = locals;
        mParametersSize = parametersSize;
        mCallSiteStackDepths = callSiteStackDepths;
    }

    public static int getOverhead() {
        return JCVMStackAnalyser.METHOD_OVERHEAD_COSMO;
    }

    public String getName() {
        return mMethod.getName();
    }

    public int getMaxStack() {
        return mMaxStack;
    }

    public int getMaxLocals() {
        return mMaxLocals;
    }

    public int getFrameSize() {
        return mFrameSize;
    }

    public static int getOverheadBytes() {
        return JCVMStackAnalyser.METHOD_OVERHEAD_COSMO * JCVMStackAnalyser.WORD_SIZE;
    }

    public int getMaxStackBytes() {
        return mMaxStack * JCVMStackAnalyser.WORD_SIZE;
    }

    public int getMaxLocalsBytes() {
        return mMaxLocals * JCVMStackAnalyser.WORD_SIZE;
    }

    public int getFrameSizeBytes() {
        return mFrameSize * JCVMStackAnalyser.WORD_SIZE;
    }

    public String toString() {
        final String info = """
            Stack frame
            ------------
            Method                 : %s
            Stack                  : %d bytes
            Locals                 : %d bytes
            Predicted stack frame  : %s bytes"""
            .formatted(mMethod.toString(), getMaxStackBytes(), getMaxLocalsBytes(), getFrameSizeBytes());

        return info;
    }

    public String localsToString() {
        final String header = "Locals\n------\n";

        final StringJoiner sj = new StringJoiner("\n");
        for (Object obj : mLocals) {
            sj.add(String.valueOf(obj));
        }

        return header + sj.toString();
    }

    public Method getMethod() {
        return mMethod;
    }

    public int getParametersSize() {
        return mParametersSize;
    }

    public int getParametersSizeBytes() {
        return mParametersSize * JCVMStackAnalyser.WORD_SIZE;
    }

    public List<LocalInfo> getLocals() {
        return mLocals;
    }

    public int getStackDepthAtCallSite(final String calleeKey) {
        return mCallSiteStackDepths.getOrDefault(calleeKey, mMaxStack);
    }

    public Map<String, Integer> getCallSiteStackDepths() {
        return mCallSiteStackDepths;
    }

    public MethodStackInfo withMaxStack(final int newMaxStack) {
        return new MethodStackInfo(
            mMethod,
            newMaxStack,
            mMaxLocals,
            mLocals,
            mParametersSize,
            mCallSiteStackDepths
        );
    }
}
