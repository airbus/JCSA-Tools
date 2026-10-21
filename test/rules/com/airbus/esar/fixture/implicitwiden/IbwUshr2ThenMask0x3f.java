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

package com.airbus.esar.fixture.implicitwiden;

/**
 * Unsigned right shift 2, then 0x3f mask. Intended silent: value range already constrained after shift;
 * 0x3f is sufficient to clear any remaining high bits for the 6 bit lane.
 */
public final class IbwUshr2ThenMask0x3f {
    private static volatile int sSink;

    private IbwUshr2ThenMask0x3f() {
    }

    public static void entry(final byte b) {
        final int w = (b >>> 2) & 0x3f;
        sSink = w;
    }
}
