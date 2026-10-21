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

import javacard.framework.JCSystem;

/**
 * Same leak as HelperCommitsConditionally, but the conditional close is two hops away. The bad
 * summary propagates through the pass-through middle method, so the caller still looks balanced.
 */
public final class TransitiveConditionalCommit {
    private short mValue;

    public void work(final short v, final boolean cond) {
        JCSystem.beginTransaction();
        mValue = v;
        middle(cond);
    }

    private void middle(final boolean cond) {
        finish(cond);
    }

    private void finish(final boolean cond) {
        if (cond) {
            JCSystem.commitTransaction();
        }
    }
}
