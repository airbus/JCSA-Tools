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

package com.airbus.esar.fixture.iso7816sw1;

import javacard.framework.ISOException;

/**
 * Nine branch assigns distinct literal SWs (one invalid) then throwIt after merge.
 * Exceeds powerset bound — should warn (unknown SW).
 */
public final class ThrowsInvalidSwNineBranchesOneInvalid {

    private static volatile short sSink;

    private ThrowsInvalidSwNineBranchesOneInvalid() {
    }

    public static void entry(final byte which) {
        final short sw;
        if (which == (byte) 0) {
            sw = (short) 0x1200;
        } else if (which == (byte) 1) {
            sw = (short) 0x6A80;
        } else if (which == (byte) 2) {
            sw = (short) 0x6A82;
        } else if (which == (byte) 3) {
            sw = (short) 0x6700;
        } else if (which == (byte) 4) {
            sw = (short) 0x6D00;
        } else if (which == (byte) 5) {
            sw = (short) 0x6E00;
        } else if (which == (byte) 6) {
            sw = (short) 0x9000;
        } else if (which == (byte) 7) {
            sw = (short) 0x6B00;
        } else {
            sw = (short) 0x6982;
        }
        ISOException.throwIt(sw);
        sSink = sw;
    }
}
