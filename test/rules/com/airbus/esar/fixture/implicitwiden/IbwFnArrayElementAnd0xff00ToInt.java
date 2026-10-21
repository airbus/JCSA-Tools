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
 * False negative: array element widens like a local; {@code buf[0] & 0xFF00} is still the wrong lane
 * for an unsigned octet (same situation as {@code IbwFnByteAnd0xff00ToInt} with a local).
 */
public final class IbwFnArrayElementAnd0xff00ToInt {
    private static volatile int sSink;

    private IbwFnArrayElementAnd0xff00ToInt() {
    }

    public static void entry(final byte[] buf) {
        final int value = buf[0] & 0xFF00;
        sSink = value;
    }
}
