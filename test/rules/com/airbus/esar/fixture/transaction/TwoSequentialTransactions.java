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
 * Two independent begin/commit pairs (NOT nested): each begin has its own matching close. Must
 * not warn. Exercises the absorbing-zero behaviour of the per-begin analysis.
 */
public final class TwoSequentialTransactions {
    private short mValue;

    public void work(final short v) {
        JCSystem.beginTransaction();
        mValue = v;
        JCSystem.commitTransaction();

        JCSystem.beginTransaction();
        mValue = (short) (v + 1);
        JCSystem.commitTransaction();
    }
}
