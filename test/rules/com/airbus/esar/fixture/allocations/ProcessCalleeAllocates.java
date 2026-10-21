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

/** Applet whose process() itself is clean but delegates to a helper that allocates. */
public final class ProcessCalleeAllocates extends Applet {

    private static final class Buffer { }

    private ProcessCalleeAllocates() {
    }

    public static void install(final byte[] buf, final short off, final byte len) {
        new ProcessCalleeAllocates().register();
    }

    @Override
    public void process(final APDU apdu) {
        handleCommand(apdu);
    }

    private void handleCommand(final APDU apdu) {
        new Buffer();
    }
}
