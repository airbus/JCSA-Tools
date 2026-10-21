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

/** Field store whose RHS is {@code id(b) + 0}, not a bare invoke expression. */
public final class IbwAssignFieldInvokeArgInAddRhs {
    private static volatile int sSink;

    private IbwAssignFieldInvokeArgInAddRhs() {
    }

    private static int id(final int x) {
        return x;
    }

    public static void entry(final byte b) {
        sSink = id(b) + 0;
    }
}
