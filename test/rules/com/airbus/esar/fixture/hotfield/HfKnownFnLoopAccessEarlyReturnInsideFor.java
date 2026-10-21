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
 * Known FN: sIndex is read only on the early-return arm of for(;;), which is inside the loop
 * in source but outside the natural loop of the continue latch, so no loop-access warning.
 */
public final class HfKnownFnLoopAccessEarlyReturnInsideFor {
    private static volatile int sSink;
    private static short sIndex;

    public void entry(final boolean getMode, final short n) {
        for (;;) {
            if (getMode) {
                sSink = sIndex;
                return;
            }
            short acc = 0;
            for (short i = 0; i < n; i++) {
                acc = (short) (acc + i);
            }
            sSink = acc;
        }
    }
}
