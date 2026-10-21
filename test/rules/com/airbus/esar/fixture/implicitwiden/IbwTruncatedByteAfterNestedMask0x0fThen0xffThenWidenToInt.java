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
 * {@code byte b = (byte)((x & 0x0f) & 0xff)} then {@code int y = b}. The operand of {@code (byte)} is a
 * {@code JAnd} whose masked side includes {@code 0xff}; {@code laneMaskConstantAtMostNibble} rejects that
 * literal for cast-def safety, so {@code byteLocalHasMaskedDef} does not mark {@code b} safe—same rationale as
 * {@link IbwTruncatedByteAfterMask0xffThenWidenToInt}. Widening {@code b} to {@code int} yields one report.
 */
public final class IbwTruncatedByteAfterNestedMask0x0fThen0xffThenWidenToInt {
    private static volatile int sSink;

    private IbwTruncatedByteAfterNestedMask0x0fThen0xffThenWidenToInt() {
    }

    public static void entry(final int x) {
        final byte b = (byte) ((x & 0x0f) & 0xff);
        final int y = b;
        sSink = y;
    }
}
