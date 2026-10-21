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
 * Big-endian {@code short} from two array elements: high byte {@code (buf[0] << 8) & 0xFF00}, low {@code buf[1] &
 * 0xFF}; one implicit widening is still reported for this shape.
 */
public final class IbwShortFromByteArrayShlThenMask {
    private static volatile short sSink;

    private IbwShortFromByteArrayShlThenMask() {
    }

    public static void entry(final byte[] buf) {
        final short w = (short) (((buf[0] << 8) & 0xFF00) | (buf[1] & 0xFF));
        sSink = w;
    }
}
