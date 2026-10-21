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
 * Constant on the left side of a comparison: {@code if (200 == b)} compiles to
 * {@code sipush 200; iload b; if_icmpne} → Jimple {@code JNeExpr(IntConstant(200), Local(b))}.
 * The byte local lands on the right operand of the Jimple expression, exercising the
 * {@code byteOnRight} path in {@code handleConditional}. 200 is outside the signed-byte range
 * so {@code isOutOfSignedByteRange(IntConstant(200))} returns true → should warn.
 */
public final class IbwIfStmtConstOnLeftByteOnRight {
    private static volatile int sSink;

    private IbwIfStmtConstOnLeftByteOnRight() {
    }

    public static void entry(final byte b) {
        if (200 == b) {
            sSink = 1;
        }
    }
}
