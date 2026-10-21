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
 * False negative: {@code 0xF00} passes shifted-mask check ({@code m == 0xF}) but
 * {@code b & 0xF00} does not yield a correct unsigned byte value.
 */
public final class IbwFnByteAnd0xf00ToInt {
    private static volatile int sSink;

    private IbwFnByteAnd0xf00ToInt() {
    }

    public static void entry(final byte b) {
        final int value = b & 0xF00;
        sSink = value;
    }
}
