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

/** {@code testMethod((short) 0, (byte) 0)}; explicit casts, no implicit widen at the invoke. */
public final class IbwInvokeShortByteExplicitCast {
    private static volatile short sSinkShort;
    private static volatile byte sSinkByte;

    private IbwInvokeShortByteExplicitCast() {
    }

    private static void testMethod(final short s, final byte b) {
        sSinkShort = s;
        sSinkByte = b;
    }

    public static void entry() {
        testMethod((short) 0, (byte) 0);
    }
}
