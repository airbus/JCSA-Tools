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
 * {@code if (b1 == b2)} — both operands are byte locals, no constant.
 * In Jimple: {@code JEqExpr(Local(b1), Local(b2))} so op1=Local(b1), op2=Local(b2).
 * In {@code handleConditional}:
 * <ul>
 *   <li>{@code byteOnLeft}: {@code hasUnmasked(Local(b1))} = true, then
 *       {@code isOutOfSignedByteRange(Local(b2))}: Local is neither IntConstant nor LongConstant
 *       → falls through to {@code return false} (L155) → byteOnLeft = false.</li>
 *   <li>{@code byteOnRight}: same reasoning → false.</li>
 * </ul>
 * The conditional produces no warning. The widening {@code int w = b1} produces one.
 * Exercises the fall-through {@code return false} path of {@code isOutOfSignedByteRange} (L155).
 */
public final class IbwTwoByteComparison {
    private static volatile int sSink;

    private IbwTwoByteComparison() {
    }

    public static void entry(final byte b1, final byte b2) {
        if (b1 == b2) {
            sSink = 1;
        }
        final int w = b1;
        sSink = w;
    }
}
