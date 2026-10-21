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

import javacard.framework.ISOException;

/** Invalid SW2 for interindustry status word InvalidSw29001. */
public final class InvalidSw29001 {

    private static volatile short sSink;

    private InvalidSw29001() {
    }

    public static void entry() {
        ISOException.throwIt((short) 0x9001);
        sSink = 0;
    }
}
