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
 * Mask constant 255L (long) on the LEFT of the {@code &} operator suppresses the initial widening
 * to {@code long}: {@code long w = b; return 255L & w;}. This covers both the mask-on-left branch
 * of {@code andMasksWithLocal} (L186) and the {@code LongConstant} branch of
 * {@code isByteLaneMaskConstant} (L325). Should be silent.
 */
public final class IbwLongMaskSuppressesWidening {

    private IbwLongMaskSuppressesWidening() {
    }

    public static long entry(final byte b) {
        final long w = b;
        return 255L & w;
    }
}
