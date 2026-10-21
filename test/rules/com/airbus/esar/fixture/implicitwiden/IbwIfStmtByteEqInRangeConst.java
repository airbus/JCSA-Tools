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
 * Byte local compared against an in-range constant (5, which fits in a signed byte).
 * {@code isOutOfSignedByteRange(IntConstant(5))} returns false, so neither
 * {@code byteOnLeft} nor {@code byteOnRight} triggers a warning from the conditional check.
 * The widening assignment {@code int w = b} in the same method ensures the analysis runs.
 * The conditional itself should NOT produce an extra report; the widening should produce one.
 */
public final class IbwIfStmtByteEqInRangeConst {
    private static volatile int sSink;

    private IbwIfStmtByteEqInRangeConst() {
    }

    public static void entry(final byte b) {
        if (b == 5) {
            sSink = 1;
        }
        final int w = b;
        sSink = w;
    }
}
