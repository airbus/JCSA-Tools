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
 * {@code long w = b; return 63L & w;} — long lane mask {@code 63L} (positive, less than 255).
 * In {@code localIsMaskedLater}, {@code andMasksWithLocal} finds the AND with op1=LongConstant(63L).
 * {@code isByteLaneMaskConstant(LongConstant(63L))}: {@code x == 255L} = false; then
 * {@code x >= 0L && x < 255L} = true → returns true → widening is suppressed → silent.
 * Exercises the {@code x >= 0L && x < 255L} true branch of {@code isByteLaneMaskConstant} for
 * {@code LongConstant} (L325 of the rule).
 */
public final class IbwLongMaskSmall {

    private IbwLongMaskSmall() {
    }

    public static long entry(final byte b) {
        final long w = b;
        return 63L & w;
    }
}
