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
 * Big-endian {@code int} from four {@code byte[]}: mask each byte with {@code 0xFF} before left shift
 * (contrast {@link IbwIntFromByteArrayBigEndian}).
 */
public final class IbwIntFromByteArrayBigEndianMaskBeforeShl {
    private static volatile int sSink;

    private IbwIntFromByteArrayBigEndianMaskBeforeShl() {
    }

    public static void entry(final byte[] buf) {
        final int w = (((buf[0] & 0xFF) << 24)
                | ((buf[1] & 0xFF) << 16)
                | ((buf[2] & 0xFF) << 8)
                | (buf[3] & 0xFF));
        sSink = w;
    }
}
