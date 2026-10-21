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

public class StackDepthInfo {
    private final List<MethodStackInfo> mMethodStacks;
    private final int mTotalDepth;

    public StackDepthInfo(final List<MethodStackInfo> methodStacks) {
        mMethodStacks = methodStacks;

        int totalDepth = 0;
        for (final MethodStackInfo info : mMethodStacks) {
            // Parameter size can be subtracted as the caller and callee
            // both have bytes for the parameters but only one frame has the parameters
            totalDepth += info.getFrameSize() - info.getParametersSize();
        }
        mTotalDepth = totalDepth;
    }

    public List<MethodStackInfo> getMethodStacks() {
        return mMethodStacks;
    }

    public int getTotalDepth() {
        return mTotalDepth;
    }

    public int getTotalDepthBytes() {
        return mTotalDepth * JCVMStackAnalyser.WORD_SIZE;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(1000);

        for (int i = 0; i < mMethodStacks.size(); i++) {
            sb.append(mMethodStacks.get(i).getMethod().toString());

            if (i != mMethodStacks.size() - 1) {
                sb.append(" -> ");
            }
        }

        final String stackDepth = String.format("\npredicted total stack depth: %s bytes", getTotalDepthBytes());
        sb.append(stackDepth);

        return sb.toString();
    }
}
