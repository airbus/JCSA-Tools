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
 * Three reads per {@code if}-{@code else} branch plus one in the condition; max per path is 4, static total 7.
 */
public final class HfFieldIfElseThreeReadsPerBranch {
    private static volatile int sSink;
    private int mX;

    public void entry() {
        final int sum;
        if (mX > 0) {
            sum = mX + mX + mX;
        } else {
            sum = mX + mX + mX;
        }
        sSink = sum;
    }
}
