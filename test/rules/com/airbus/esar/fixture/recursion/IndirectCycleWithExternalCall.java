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

package com.airbus.esar.fixture.recursion;

import javacard.framework.Util;

/**
 * Indirect cycle A↔B where one method also calls an external JavaCard API method.
 * In {@code JcCallGraphCycle}, the adjacency-building loop iterates call targets and
 * skips targets not in the reachable-method set ({@code nodes}). The external call to
 * {@code Util.arrayFillNonAtomic} is not in {@code nodes}, so the
 * {@code if (!nodes.contains(t)) { continue; }} branch (L46) fires.
 * Exercises the {@code !nodes.contains(t)} true branch in the cycle-detection loop.
 */
public final class IndirectCycleWithExternalCall {

    private IndirectCycleWithExternalCall() {
    }

    public static void entryA(final byte[] buf) {
        Util.arrayFillNonAtomic(buf, (short) 0, (short) 1, (byte) 0);
        entryB(buf);
    }

    public static void entryB(final byte[] buf) {
        entryA(buf);
    }
}
