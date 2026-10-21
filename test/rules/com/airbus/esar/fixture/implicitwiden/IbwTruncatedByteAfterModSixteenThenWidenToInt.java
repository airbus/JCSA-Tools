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
 * {@code byte b = (byte)(x % 16)} then {@code int y = b}. Semantically bounded like a low mask, but
 * {@code byteLocalHasMaskedDef} only recognises {@code (byte)(JAnd)} with a non-negating constant mask;
 * {@code %} is not {@code JAnd}, so {@code b} is not marked safe and widening reports.
 */
public final class IbwTruncatedByteAfterModSixteenThenWidenToInt {
    private static volatile int sSink;

    private IbwTruncatedByteAfterModSixteenThenWidenToInt() {
    }

    public static void entry(final int x) {
        final byte b = (byte) (x % 16);
        final int y = b;
        sSink = y;
    }
}
