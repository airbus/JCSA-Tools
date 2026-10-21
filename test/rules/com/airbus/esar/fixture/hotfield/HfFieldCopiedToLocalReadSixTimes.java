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

/** Field read once into a local; the local is used six times. */
public final class HfFieldCopiedToLocalReadSixTimes {
    private static volatile int sSink;
    private int mX;

    public void entry() {
        final int local = mX;
        // CHECKSTYLE.SUPPRESS: FinalLocalVariable for +6 lines
        int a = local;
        int b = local;
        int c = local;
        int d = local;
        int e = local;
        int f = local;
        sSink = a + b + c + d + e + f;
    }
}
