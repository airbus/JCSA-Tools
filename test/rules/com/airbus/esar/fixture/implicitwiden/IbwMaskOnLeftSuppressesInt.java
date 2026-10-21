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
 * Mask constant 255 on the LEFT of the {@code &} operator in a later statement suppresses the initial
 * widening: {@code int t = b; int s = 255 & t;}. This exercises the mask-on-left branch of
 * {@code andMasksWithLocal} (op1 is the constant). Should be silent.
 */
public final class IbwMaskOnLeftSuppressesInt {
    private static volatile int sSink;

    private IbwMaskOnLeftSuppressesInt() {
    }

    public static void entry(final byte b) {
        final int t = b;
        final int s = 255 & t;
        sSink = s;
    }
}
