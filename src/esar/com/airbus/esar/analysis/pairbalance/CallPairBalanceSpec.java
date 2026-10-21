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

package com.airbus.esar.analysis.pairbalance;

import java.util.List;
import java.util.Objects;

import com.airbus.esar.analysis.callflow.InvokeSignaturePattern;

/**
 * Config for an open/close pair-balance rule. "Open" calls bump an abstract depth counter, "close" calls decrement
 * it. The rule fires when an open is not matched by a close on every reachable path.
 *
 * All opens are treated interchangeably and so are all closes. Nested opens are counted, so depth goes to 2, 3,
 * ... and each close walks it back down. The message receives one argument, {0}, the residual depth at the leaking
 * tail.
 */
public final class CallPairBalanceSpec {

    // Anything deeper than this is still a leak; we stop counting to help termination.
    public static final int DEFAULT_DEPTH_CAP = 64;

    private final String mRuleId;
    private final String mMessageId;
    private final List<InvokeSignaturePattern> mOpenCalls;
    private final List<InvokeSignaturePattern> mCloseCalls;
    private final int mDepthCap;

    private CallPairBalanceSpec(final String ruleId,
                                final String messageId,
                                final List<InvokeSignaturePattern> openCalls,
                                final List<InvokeSignaturePattern> closeCalls,
                                final int depthCap) {
        mRuleId = Objects.requireNonNull(ruleId, "ruleId");
        mMessageId = Objects.requireNonNull(messageId, "messageId");
        mOpenCalls = List.copyOf(openCalls);
        mCloseCalls = List.copyOf(closeCalls);
        if (mOpenCalls.isEmpty()) {
            throw new IllegalArgumentException("at least one open pattern is required");
        }
        if (mCloseCalls.isEmpty()) {
            throw new IllegalArgumentException("at least one close pattern is required");
        }
        if (depthCap <= 0) {
            throw new IllegalArgumentException("depthCap must be positive");
        }
        mDepthCap = depthCap;
    }

    public static CallPairBalanceSpec of(final String ruleId,
                                         final String messageId,
                                         final List<InvokeSignaturePattern> openCalls,
                                         final List<InvokeSignaturePattern> closeCalls) {
        return new CallPairBalanceSpec(ruleId, messageId, openCalls, closeCalls, DEFAULT_DEPTH_CAP);
    }

    public static CallPairBalanceSpec of(final String ruleId,
                                         final String messageId,
                                         final List<InvokeSignaturePattern> openCalls,
                                         final List<InvokeSignaturePattern> closeCalls,
                                         final int depthCap) {
        return new CallPairBalanceSpec(ruleId, messageId, openCalls, closeCalls, depthCap);
    }

    public String getRuleId() {
        return mRuleId;
    }

    public String getMessageId() {
        return mMessageId;
    }

    public List<InvokeSignaturePattern> getOpenCalls() {
        return mOpenCalls;
    }

    public List<InvokeSignaturePattern> getCloseCalls() {
        return mCloseCalls;
    }

    public int getDepthCap() {
        return mDepthCap;
    }
}
