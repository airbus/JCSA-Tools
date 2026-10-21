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
 * {@code byte b = (byte)(p1 & p2)} — neither operand of the AND is a constant, so
 * {@code byteLocalHasMaskedDef} finds k1=false and k2=false → reaches the
 * {@code return false} at the "neither operand is a constant" branch → b is not
 * considered safe → the subsequent widening {@code int w = b} should warn.
 */
public final class IbwCastNonConstantMask {
    private static volatile int sSink;

    private IbwCastNonConstantMask() {
    }

    public static void entry(final byte p1, final byte p2) {
        final byte b = (byte) (p1 & p2);
        final int w = b;
        sSink = w;
    }
}
