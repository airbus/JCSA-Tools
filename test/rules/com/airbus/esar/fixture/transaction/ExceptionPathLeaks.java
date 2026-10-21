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

package com.airbus.esar.fixture.transaction;

import javacard.framework.ISOException;
import javacard.framework.JCSystem;

/**
 * begin(), then a try/catch where the catch handler does NOT close the transaction. The rule
 * must follow the exceptional edge to the catch handler and see residual depth 1 at the method's
 * exit, flagging the begin.
 */
public final class ExceptionPathLeaks {
    private short mValue;

    public void work(final short v) {
        JCSystem.beginTransaction();
        try {
            riskyUpdate(v);
            JCSystem.commitTransaction();
        } catch (final ISOException ex) {
            // Forgotten abort/commit: transaction remains open on the exception edge.
            mValue = (short) -1;
        }
    }

    private void riskyUpdate(final short v) {
        if (v < 0) {
            ISOException.throwIt((short) 0x6F00);
        }
        mValue = v;
    }
}
