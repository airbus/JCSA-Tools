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

package com.airbus.esar.fixture.bitshift;

/**
 * Signed right shifts on byte-array elements used as S-box nibble indices
 * ({@code (b[i] >> 4) & 0xf} / {@code (b[i] >> 0) & 0xf}).
 */
public final class SignedRightShiftByteArrayNibble {

    private static final byte[] S_BOX = new byte[16];

    private SignedRightShiftByteArrayNibble() {
    }

    public static void entry(final byte[] b, final short offset, final byte[] temp) {
        temp[0] = S_BOX[(b[offset] >> 4) & 0xf];
        temp[1] = S_BOX[(b[offset] >> 0) & 0xf];
        temp[2] = S_BOX[(b[(short) (offset + 1)] >> 4) & 0xf];
        temp[3] = S_BOX[(b[(short) (offset + 1)] >> 0) & 0xf];
    }
}
