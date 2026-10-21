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
 * Big-endian {@code int} from four {@code byte[]} elements, high byte first: each lane is shift then
 * {@code 0xFF} width mask (or low byte {@code & 0xFF}).
 */
public final class IbwIntFromByteArrayBigEndian {
    private static volatile int sSink;

    private IbwIntFromByteArrayBigEndian() {
    }

    public static void entry(final byte[] buf) {
        final int w = (((buf[0] << 24) & 0xFF000000)
                | ((buf[1] << 16) & 0xFF0000)
                | ((buf[2] << 8) & 0xFF00)
                | (buf[3] & 0xFF));
        sSink = w;
    }
}
