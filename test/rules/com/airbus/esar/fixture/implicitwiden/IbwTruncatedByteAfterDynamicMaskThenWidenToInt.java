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
 * {@code byte b = (byte)(x & mask)} with {@code mask} a non-constant {@code byte}; the AND is not
 * foldable to a lane literal, so {@code b} is not tracked as a safe byte local when widening to
 * {@code int}. {@code mask} is also widened to {@code int} for {@code x & mask}, so this method
 * yields two reports.
 */
public final class IbwTruncatedByteAfterDynamicMaskThenWidenToInt {
    private static volatile int sSink;

    private IbwTruncatedByteAfterDynamicMaskThenWidenToInt() {
    }

    public static void entry(final int x, final byte mask) {
        final byte b = (byte) (x & mask);
        final int w = b;
        sSink = w;
    }
}
