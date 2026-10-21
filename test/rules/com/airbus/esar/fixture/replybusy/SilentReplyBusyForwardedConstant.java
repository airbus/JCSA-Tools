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

package com.airbus.esar.fixture.replybusy;

import javacard.framework.ISOException;

/** Compile-time constant call argument; param not tracked interprocedurally — silent. */
public final class SilentReplyBusyForwardedConstant {

    private static volatile short sSink;

    private SilentReplyBusyForwardedConstant() {
    }

    public static void entry() {
        throwWithSw((short) 0x9300);
        sSink = 0;
    }

    private static void throwWithSw(final short sw) {
        ISOException.throwIt(sw);
    }
}
