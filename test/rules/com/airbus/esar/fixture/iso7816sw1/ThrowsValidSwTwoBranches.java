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

import javacard.framework.ISO7816;
import javacard.framework.ISOException;

/** Branch assigns one of two valid SWs to local, then throwIt after merge. */
public final class ThrowsValidSwTwoBranches {

    private static volatile short sSink;

    private ThrowsValidSwTwoBranches() {
    }

    public static void entry(final boolean flag) {
        final short sw;
        if (flag) {
            sw = ISO7816.SW_WRONG_DATA;
        } else {
            sw = ISO7816.SW_FILE_NOT_FOUND;
        }
        ISOException.throwIt(sw);
        sSink = sw;
    }
}
