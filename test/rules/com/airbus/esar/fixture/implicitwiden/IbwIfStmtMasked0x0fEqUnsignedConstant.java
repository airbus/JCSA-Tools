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
 * {@code (b & 0x0f) == 0xff}: Jimple keeps the masked value in a {@code byte} temp, so the {@code if}
 * condition compares a {@code byte} local to {@code 255}. The widening rule does not trace the prior
 * {@code &} on {@code b}. For {@code b & 0xff}, Jimple uses a {@code short} temp instead, so that case
 * is not flagged in the condition.
 */
public final class IbwIfStmtMasked0x0fEqUnsignedConstant {
    private static volatile int sSink;

    private IbwIfStmtMasked0x0fEqUnsignedConstant() {
    }

    public static void entry(final byte b) {
        if ((b & 0x0f) == 0xff) {
            sSink = 1;
        }
    }
}
