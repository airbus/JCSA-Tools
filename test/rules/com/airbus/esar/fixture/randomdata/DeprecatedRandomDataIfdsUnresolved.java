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

package com.airbus.esar.fixture.randomdata;

import javacard.security.RandomData;

/**
 * Regression: final local for {@code ALG_PSEUDO_RANDOM} plus {@code iconst_1} at callee call (javac); IFDS must still
 * reach {@code getInstance}.
 */
public final class DeprecatedRandomDataIfdsUnresolved {

    private DeprecatedRandomDataIfdsUnresolved() {
    }

    /** {@code seed} local plus identity callee; callee receives folded literal, not {@code iload seed}. */
    public static void entryLocalConstantMismatch() {
        final byte seed = RandomData.ALG_PSEUDO_RANDOM;
        RandomData.getInstance(deepPseudoA(seed));
    }

    private static byte deepPseudoA(final byte x) {
        return x;
    }
}
