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

package com.airbus.esar.fixture.comptime;

import javacard.security.RandomData;

/**
 * Stress: the algorithm byte is always {@link RandomData#ALG_TRNG} at runtime (constant return), but the argument at
 * {@code getInstance} is typically a {@code Local} filled by an invoke — the detector does not interpret callees. It is
 * expected to <em>report</em> here (false positive vs. “always the same constant in this program”).
 */
public final class ComptimeConstantThroughCallee {

    private ComptimeConstantThroughCallee() {
    }

    public static void entryConstantBehindCallee() {
        RandomData.getInstance(alwaysTrng());
    }

    private static byte alwaysTrng() {
        return RandomData.ALG_TRNG;
    }
}
