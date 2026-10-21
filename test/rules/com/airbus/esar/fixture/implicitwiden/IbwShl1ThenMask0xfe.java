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
 * Byte widens, left shift 1, then 0xfe (no 0xff). Intended silent: LSB handled by the partial mask
 * after shift.
 */
public final class IbwShl1ThenMask0xfe {
    private static volatile int sSink;

    private IbwShl1ThenMask0xfe() {
    }

    public static void entry(final byte b) {
        final int w = (b << 1) & 0xfe;
        sSink = w;
    }
}
