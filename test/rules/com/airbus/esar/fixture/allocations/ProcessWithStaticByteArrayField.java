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

package com.airbus.esar.fixture.allocations;

import javacard.framework.APDU;
import javacard.framework.Applet;

/** Applet with a static byte[] field; process() uses it without allocating. */
public final class ProcessWithStaticByteArrayField extends Applet {

    private static volatile byte[] sBuffer = {0x01, 0x02, 0x03};

    private ProcessWithStaticByteArrayField() {
    }

    public static void install(final byte[] buf, final short off, final byte len) {
        new ProcessWithStaticByteArrayField().register();
    }

    @Override
    public void process(final APDU apdu) {
        final byte[] buffer = apdu.getBuffer();
        buffer[0] = sBuffer[0];
        buffer[1] = sBuffer[1];
        buffer[2] = sBuffer[2];
    }
}
