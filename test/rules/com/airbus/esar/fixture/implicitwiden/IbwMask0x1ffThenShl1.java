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
 * Mask 0x1ff (511) then left shift. Not a 0..255 lane mask; rule should report.
 */
public final class IbwMask0x1ffThenShl1 {
    private static volatile int sSink;

    private IbwMask0x1ffThenShl1() {
    }

    public static void entry(final byte b) {
        final int w = (b & 0x1ff) << 1;
        sSink = w;
    }
}
