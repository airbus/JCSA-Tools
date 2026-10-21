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

/** {@code testMethod(short, byte)} with implicit {@code byte} widen to {@code short} on the first argument. */
public final class IbwInvokeShortByteImplicitWiden {
    private static volatile short sSinkShort;
    private static volatile byte sSinkByte;

    private IbwInvokeShortByteImplicitWiden() {
    }

    private static void testMethod(final short s, final byte b) {
        sSinkShort = s;
        sSinkByte = b;
    }

    public static void entry(final byte b) {
        testMethod(b, b);
    }
}
