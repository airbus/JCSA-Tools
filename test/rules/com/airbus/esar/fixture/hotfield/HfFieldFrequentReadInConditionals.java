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
 * Same field read in five separate {@code if} conditions (no loop); should trigger frequent-access warning
 * at registry threshold 5.
 */
public final class HfFieldFrequentReadInConditionals {
    private static volatile int sSink;
    private int mX;

    public void entry() {
        int sum = 0;
        if (mX > -100) {
            sum++;
        }
        if (mX > -99) {
            sum++;
        }
        if (mX > -98) {
            sum++;
        }
        if (mX > -97) {
            sum++;
        }
        if (mX > -96) {
            sum++;
        }
        sSink = sum;
    }
}
