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

package com.airbus.esar.fixture.hotfield;

/**
 * Known FP: early-return arm inside for(;;) is outside the natural loop of the continue latch
 * (inner for back-edge), so those field reads still count as frequent access.
 */
public final class HfKnownFpEarlyReturnInsideFor {
    private static volatile int sSink;
    private static short sIndex;
    private static final byte[] BUF = new byte[8];

    public void entry(final boolean getMode, final short n) {
        if ((BUF[sIndex] == (byte) -1) && (sIndex == 0)) {
            return;
        }
        for (;;) {
            if (BUF[sIndex] == (byte) -1) {
                sIndex = 0;
                return;
            }
            if (getMode) {
                sSink = sIndex;
                sSink += sIndex;
                sSink += sIndex;
                sSink += sIndex;
                sSink += sIndex;
                return;
            }
            for (short i = 0; i < n; i++) {
                sIndex++;
            }
        }
    }
}
