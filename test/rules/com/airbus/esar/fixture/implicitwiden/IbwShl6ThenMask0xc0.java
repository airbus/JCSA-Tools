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

/** {@code (b << 6) & 0xc0}; mask low six bits clear. */
public final class IbwShl6ThenMask0xc0 {
    private static volatile int sSink;

    private IbwShl6ThenMask0xc0() {
    }

    public static void entry(final byte b) {
        final int w = (b << 6) & 0xc0;
        sSink = w;
    }
}
