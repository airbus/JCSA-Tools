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

package com.airbus.esar.fixture.apdu;

import javacard.framework.APDU;
import javacard.framework.ISO7816;

/** Incoming then outgoing protocol. Must not warn. */
public final class ApduIncomingBalancedFull {
    public void work(final APDU apdu) {
        short bytesLeft = apdu.setIncomingAndReceive();
        while (bytesLeft > 0) {
            bytesLeft -= apdu.receiveBytes(ISO7816.OFFSET_CDATA);
        }
        final short le = apdu.setOutgoing();
        apdu.setOutgoingLength(le);
        apdu.sendBytes((short) 0, le);
    }
}
