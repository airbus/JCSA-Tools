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

/** Field read under {@code if} inside a {@code for} loop; should trigger the loop-access warning. */
public final class HfFieldReadInsideIfInForLoop {
    private static volatile int sSink;
    private int mX;

    public void entry() {
        int sum = 0;
        for (int i = 0; i < 3; i++) {
            if (i >= 0) {
                sum += mX;
            }
        }
        sSink = sum;
    }
}
