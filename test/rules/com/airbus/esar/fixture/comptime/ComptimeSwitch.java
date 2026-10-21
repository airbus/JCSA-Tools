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

package com.airbus.esar.fixture.comptime;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.security.RandomData;

public final class ComptimeSwitch {

    private ComptimeSwitch() {
    }

    public static byte getAlgorithm(final byte[] buffer) {
        switch (buffer[0]) {
        case 1:
            return 1;
        default:
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
            return -1;
        }
    }

    public static void entry(final byte[] buffer) {
        RandomData.getInstance(getAlgorithm(buffer));
    }
}
