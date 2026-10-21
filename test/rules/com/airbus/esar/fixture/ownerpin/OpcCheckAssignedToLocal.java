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

package com.airbus.esar.fixture.ownerpin;

import javacard.framework.OwnerPIN;

/** {@code OwnerPIN.check} result assigned to a local. */
public final class OpcCheckAssignedToLocal {
    private final OwnerPIN mPin = new OwnerPIN((byte) 3, (byte) 5);

    public void entry(final byte[] buf) {
        final boolean ok = mPin.check(buf, (short) 0, (byte) 8);
        if (ok) {
            mPin.reset();
        }
    }
}
