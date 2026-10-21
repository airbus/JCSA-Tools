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
 * {@code byte b = (byte)(0x3f & x)} then {@code int w = b}. Even though 0x3f (63) guarantees
 * a non-negative byte after truncation, {@code byteLocalHasMaskedDef} does not trace through
 * the Jimple decomposition of {@code (byte)(expr & K)} (two statements: {@code $tmp = expr & K;
 * b = (byte) $tmp}); {@code b}'s direct definition is a {@code JCastExpr}, not a
 * {@code JAndExpr}, so {@code b} is not marked safe → widening warns.
 */
public final class IbwCastWideNibbleMask {
    private static volatile int sSink;

    private IbwCastWideNibbleMask() {
    }

    public static void entry(final byte x) {
        final byte b = (byte) (0x3f & x);
        final int w = b;
        sSink = w;
    }
}
