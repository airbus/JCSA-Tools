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

/**
 * Applet whose {@code process} method is directly recursive. The BFS in
 * {@code JcProcessAllocation} must detect the cycle (visit-guard at L83) and stop re-enqueueing
 * the same method signature.
 */
public final class ProcessAllocatesObjectRecursive extends Applet {

    private static final class Token { }

    private ProcessAllocatesObjectRecursive() {
    }

    public static void install(final byte[] bArray, final short bOffset, final byte bLength) {
        new ProcessAllocatesObjectRecursive().register();
    }

    @Override
    public void process(final APDU apdu) {
        new Token();
        process(apdu);
    }
}
