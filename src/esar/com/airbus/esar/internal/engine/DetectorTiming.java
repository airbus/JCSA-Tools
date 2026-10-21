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

package com.airbus.esar.internal.engine;

import java.util.Locale;
import java.util.Objects;

/**
 * CPU time spent in one detector during a traverse (initAnalysis + visitation).
 */
public final class DetectorTiming {

    private final String mRuleId;
    private final long mInitCpuNanos;
    private final long mTraverseCpuNanos;

    private DetectorTiming(final String ruleId, final long initCpuNanos, final long traverseCpuNanos) {
        mRuleId = Objects.requireNonNull(ruleId, "ruleId");
        mInitCpuNanos = initCpuNanos;
        mTraverseCpuNanos = traverseCpuNanos;
    }

    /**
     * @param ruleId detector rule id
     * @param initCpuNanos CPU nanoseconds in initAnalysis
     * @param traverseCpuNanos CPU nanoseconds in start/visit/end callbacks
     * @return timing record
     */
    public static DetectorTiming of(final String ruleId,
                                    final long initCpuNanos,
                                    final long traverseCpuNanos) {
        return new DetectorTiming(ruleId, initCpuNanos, traverseCpuNanos);
    }

    public String getRuleId() {
        return mRuleId;
    }

    public long getInitCpuNanos() {
        return mInitCpuNanos;
    }

    public long getTraverseCpuNanos() {
        return mTraverseCpuNanos;
    }

    public long getTotalCpuNanos() {
        return mInitCpuNanos + mTraverseCpuNanos;
    }

    /**
     * @return one-line summary for verbose diagnostics
     */
    public String formatLine() {
        return String.format(
                Locale.ROOT,
                "%-40s init=%8.1f ms  traverse=%8.1f ms  total=%8.1f ms",
                mRuleId,
                mInitCpuNanos / 1_000_000.0,
                mTraverseCpuNanos / 1_000_000.0,
                getTotalCpuNanos() / 1_000_000.0);
    }
}
