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

package com.airbus.esar.fixture.implicitwiden;

/** Stand-alone invoke: {@code short} argument to a {@code short} parameter. No widening. */
public final class IbwInvokeStmtShortArgToShort {
    private static volatile short sSink;
    private static volatile int sSinkInt;

    private IbwInvokeStmtShortArgToShort() {
    }

    private static void sink(final int test, final short x) {
        sSinkInt = test;
        sSink = x;
    }

    public static void entry(final short s) {
        final int test = 534053490;
        sink(test, s);
    }
}
