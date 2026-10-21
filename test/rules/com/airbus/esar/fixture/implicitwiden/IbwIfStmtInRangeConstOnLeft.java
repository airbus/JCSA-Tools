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
 * {@code if (5 == b)} — constant 5 on the LEFT, byte local on the RIGHT.
 * In Jimple: {@code JNeExpr(IntConstant(5), Local(b))} so op1=IntConstant(5), op2=Local(b).
 * In {@code handleConditional}:
 * <ul>
 *   <li>{@code byteOnLeft}: {@code hasUnmasked(IntConstant(5))} = false → false (short-circuit)</li>
 *   <li>{@code byteOnRight}: {@code hasUnmasked(Local(b))} = true AND
 *       {@code isOutOfSignedByteRange(IntConstant(5))} = false (5 in range) → false</li>
 * </ul>
 * The conditional produces no warning; the widening {@code int w = b} produces one.
 * Exercises the branch where {@code byteOnRight} evaluates both sub-conditions and
 * returns false (L139, 2nd condition short-circuit false).
 */
public final class IbwIfStmtInRangeConstOnLeft {
    private static volatile int sSink;

    private IbwIfStmtInRangeConstOnLeft() {
    }

    public static void entry(final byte b) {
        if (5 == b) {
            sSink = 1;
        }
        final int w = b;
        sSink = w;
    }
}
