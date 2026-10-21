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

/** Chunked response: sendBytes allowed multiple times after the first successful send. Must not warn. */
public final class ApduBalancedChunked {
    public void work(final APDU apdu) {
        final short le = apdu.setOutgoing();
        apdu.setOutgoingLength(le);
        apdu.sendBytes((short) 0, (short) 2);
        apdu.sendBytes((short) 2, (short) 2);
    }
}
