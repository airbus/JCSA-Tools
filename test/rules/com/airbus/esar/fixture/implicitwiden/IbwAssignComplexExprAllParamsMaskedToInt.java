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
 * Assignment (not return) of a nontrivial int expression over {@code k}, {@code a}, {@code b}.
 * Every {@code byte} is combined with a lane constant in the same assigned value.
 */
public final class IbwAssignComplexExprAllParamsMaskedToInt {
    private static volatile int sSink;

    private IbwAssignComplexExprAllParamsMaskedToInt() {
    }

    public static void entry(final int k, final byte a, final byte b) {
        sSink = k * 3 - ((a & 0xff) | (b & 0xff)) + ((a & 0x0f) * (b & 0xf0) >>> 1);
    }
}
