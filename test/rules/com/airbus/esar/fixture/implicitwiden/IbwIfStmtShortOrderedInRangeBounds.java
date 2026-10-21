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

/** Polluted {@code short} ordered-compared to {@code 0}, {@code -32768}, and {@code 32767}. */
public final class IbwIfStmtShortOrderedInRangeBounds {
    private static volatile int sSink;

    private IbwIfStmtShortOrderedInRangeBounds() {
    }

    public static void entry(final short sharesLen, final byte t) {
        final short secretLen = (short) (sharesLen / t);
        if (secretLen <= 0) {
            sSink = 1;
        }
        if (secretLen >= 0) {
            sSink = 2;
        }
        if (secretLen <= 32767) {
            sSink = 3;
        }
        if (secretLen < 32767) {
            sSink = 4;
        }
        if (secretLen >= 32767) {
            sSink = 5;
        }
        if (secretLen > 32767) {
            sSink = 6;
        }
        if (secretLen >= -32768) {
            sSink = 7;
        }
        if (secretLen > -32768) {
            sSink = 8;
        }
        if (secretLen <= -32768) {
            sSink = 9;
        }
        if (secretLen < -32768) {
            sSink = 10;
        }
    }
}
