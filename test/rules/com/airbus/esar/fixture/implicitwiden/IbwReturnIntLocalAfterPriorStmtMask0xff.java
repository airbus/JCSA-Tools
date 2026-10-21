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

/**
 * Lane mask is on the previous statement; the {@code return} is a bare {@code int} local. In Jimple
 * the return operand is only that local, not the {@code & 0xff} tree (unlike {@code return b & 0xff}).
 * Should be silent: wide enough mask on the assign RHS.
 */
public final class IbwReturnIntLocalAfterPriorStmtMask0xff {
    private IbwReturnIntLocalAfterPriorStmtMask0xff() {
    }

    public static int entry(final byte b) {
        final int t = b & 0xff;
        return t;
    }
}
