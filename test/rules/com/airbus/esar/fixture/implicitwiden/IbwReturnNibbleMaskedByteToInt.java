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
 * {@code byte b = (byte)(x & 0x0f)} then {@code return b}. The nibble mask
 * guarantees {@code b} is in [-8, 7], so widening to {@code int} is
 * semantically safe and should produce no warning.
 *
 * <p>Currently produces a false-positive warning because {@code handleReturn}
 * does not call {@code computeSafeByteLocals} (it does not receive {@code body}
 * and performs no backward analysis). Even {@code handleAssignment} would
 * produce a false positive here because {@code byteLocalHasMaskedDef} looks
 * only at the direct RHS of {@code b}'s definition; in Jimple that RHS is
 * {@code (byte) $tmp} (a {@code JCastExpr} whose operand is a temp local, not
 * a {@code JAndExpr}), so the nibble mask is invisible. Both bugs must be fixed
 * for this and the analogous assignment case to become silent.
 */
public final class IbwReturnNibbleMaskedByteToInt {

    private IbwReturnNibbleMaskedByteToInt() {
    }

    public static int entry(final byte x) {
        final byte b = (byte) (x & 0x0f);
        return b;
    }
}
