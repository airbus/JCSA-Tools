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

package com.airbus.esar.fixture.comptime;

import javacard.security.RandomData;

/**
 * Stress: after the branch, {@code b} is either a fixed API constant or a parameter — so the value at
 * {@code getInstance} is not fixed in the CAP. The detector uses a linear backward scan for {@code Local} definitions,
 * not a CFG merge, so whichever {@code b = …} appears last in the Jimple listing can “win”, producing either a
 * spurious all-clear or a spurious finding depending on compiler output. Treat this fixture as
 * <em>order-dependent</em> stress, not a stable oracle.
 */
public final class ComptimeUnsoundBackwardScanBranchMerge {

    private ComptimeUnsoundBackwardScanBranchMerge() {
    }

    public static void entryBranchMerge(final byte fromApdu, final boolean useTrng) {
        final byte b;
        if (useTrng) {
            b = RandomData.ALG_TRNG;
        } else {
            b = fromApdu;
        }
        RandomData.getInstance(b);
    }
}
