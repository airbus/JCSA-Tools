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
 * begin and commit in try, abort in catch; finally only cleans up.
 */
public final class TryFinallyCommitAbortCleanup {
    private short mValue;
    private short mScratch;

    public void work(final short v) {
        try {
            JCSystem.beginTransaction();
            if (v < 0) {
                ISOException.throwIt((short) 0x6F00);
            }
            mValue = v;
            JCSystem.commitTransaction();
        } catch (final ISOException ex) {
            JCSystem.abortTransaction();
            throw ex;
        } finally {
            mScratch = 0;
        }
    }
}
