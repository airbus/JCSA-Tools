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

/** Polluted {@code short} {@code >= 32768} — literal just outside signed {@code short} range. */
public final class IbwIfStmtShortGeOutOfRange {
    private static volatile int sSink;

    private IbwIfStmtShortGeOutOfRange() {
    }

    public static void entry(final short sharesLen, final byte t) {
        final short secretLen = (short) (sharesLen / t);
        if (secretLen >= 32768) {
            sSink = 1;
        }
    }
}
