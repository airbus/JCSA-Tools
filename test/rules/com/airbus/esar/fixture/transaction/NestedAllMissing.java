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
 * Two begins, no commits at all. The outer begin must be reported with a residual of two (the
 * inner begin is absorbed once the outer is reported; from the inner's perspective the same
 * residual is seen, so two warnings is also acceptable). Whatever the attribution, at least one
 * warning is expected.
 */
public final class NestedAllMissing {
    private short mValue;

    public void work(final short v) {
        JCSystem.beginTransaction();
        mValue = v;
        JCSystem.beginTransaction();
        mValue = (short) (v + 1);
    }
}
