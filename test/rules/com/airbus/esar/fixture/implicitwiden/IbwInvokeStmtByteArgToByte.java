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

/** Stand-alone invoke: {@code byte} argument to a {@code byte} parameter. No widening. */
public final class IbwInvokeStmtByteArgToByte {
    private static volatile byte sSink;
    private static volatile byte[] sSink2;

    private IbwInvokeStmtByteArgToByte() {
    }

    private static short sink(final byte x, final byte[] y) {
        sSink = x;
        sSink2 = y;
        return 12;
    }

    public static void entry(final byte b) {
        final byte[] bytes = new byte[10];
        // CHECKSTYLE.SUPPRESS: UnusedLocalVariable for +1 lines
        final short test = sink(b, bytes);
    }
}
