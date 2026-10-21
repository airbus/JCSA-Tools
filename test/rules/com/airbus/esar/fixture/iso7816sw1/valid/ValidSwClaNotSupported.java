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

package com.airbus.esar.fixture.iso7816sw1.valid;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;

/** Valid interindustry status word ClaNotSupported. */
public final class ValidSwClaNotSupported {

    private static volatile short sSink;

    private ValidSwClaNotSupported() {
    }

    public static void entry() {
        ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        sSink = 0;
    }
}
