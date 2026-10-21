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

/**
 * Path-correlation FP. Both branches guarded by the same condition, so at runtime the APDU is
 * either fully used (DONE) or untouched (NONE). The may-semantics join merges states to
 * {NONE, OUTGOING_LENGTH}, and setOutgoingLength from NONE looks like an out-of-order trigger.
 */
public final class ApduPathCorrelatedBranches {
    public void work(final APDU apdu, final boolean cond) {
        short le = 0;
        if (cond) {
            le = apdu.setOutgoing();
        }
        if (cond) {
            apdu.setOutgoingLength(le);
            apdu.sendBytes((short) 0, le);
        }
    }
}
