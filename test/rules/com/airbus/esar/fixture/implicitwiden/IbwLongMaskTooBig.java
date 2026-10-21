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
 * {@code long w = b; return 256L & w;} — long mask constant larger than 255.
 * In {@code localIsMaskedLater}, {@code andMasksWithLocal} checks op1=LongConstant(256L).
 * {@code isByteLaneMaskConstant(LongConstant(256L))}: {@code x == 255L} = false; then
 * {@code x >= 0L} = true but {@code x < 255L} = false → {@code x >= 0L && x < 255L} = false
 * → returns false.
 * The mask is too wide; the widening {@code long w = b} produces one warning.
 * Exercises the {@code x < 255L} false branch of {@code isByteLaneMaskConstant} for
 * {@code LongConstant}.
 */
public final class IbwLongMaskTooBig {

    private IbwLongMaskTooBig() {
    }

    public static long entry(final byte b) {
        final long w = b;
        return 256L & w;
    }
}
