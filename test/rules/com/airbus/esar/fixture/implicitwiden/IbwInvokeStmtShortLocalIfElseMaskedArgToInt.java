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

/** {@code short} local assigned in if-else, then {@code test & 0xffff} passed to an {@code int} parameter. */
public final class IbwInvokeStmtShortLocalIfElseMaskedArgToInt {
    private static volatile int sSink;

    private IbwInvokeStmtShortLocalIfElseMaskedArgToInt() {
    }

    private static void sink(final int x) {
        sSink = x;
    }

    private static short test() {
        return 1;
    }

    public static void entry(final boolean cond) {
        short test = 1;
        if (cond) {
            test = test();
        } else {
            test = 3;
        }
        sink(test & 0xffff);
    }
}
