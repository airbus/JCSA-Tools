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
 * Harder IFDS scenarios: multi-hop parameter threading, callee-wrapped literals, and branch merge (may pseudo).
 * Callee chains are not shared across these entries so summaries are not merged with unrelated TRNG flows.
 */
public final class DeprecatedRandomDataIfdsChallenging {

    private DeprecatedRandomDataIfdsChallenging() {
    }

    /**
     * Deprecated pseudo algorithm flows through three dedicated callees before {@code getInstance}.
     * A callee supplies the algorithm byte so the first hop receives an {@code iload} argument: passing
     * {@code RandomData.ALG_PSEUDO_RANDOM} directly often compiles to {@code iconst_1}, and the analysis
     * then fails to attach a finding to the {@code getInstance} argument local in this shape.
     */
    public static void entryDeepPseudoChain() {
        final byte seed = pseudoAlgConstant();
        RandomData.getInstance(deepPseudoA(seed));
    }

    private static byte pseudoAlgConstant() {
        return RandomData.ALG_PSEUDO_RANDOM;
    }

    private static byte deepPseudoA(final byte x) {
        return deepPseudoB(x);
    }

    private static byte deepPseudoB(final byte y) {
        return deepPseudoC(y);
    }

    private static byte deepPseudoC(final byte z) {
        return z;
    }

    /**
     * Numeric {@code 2} (secure deprecated) through its own identity callee chain.
     * The literal must not be fed straight into the hops: javac usually passes {@code iconst_2} into
     * {@code numericHopA}, while IFDS ties facts to the {@code getInstance} argument local — so we obtain
     * {@code 2} via callee return (same idea as DeprecatedRandomDataDataFlow.secure).
     */
    public static void entryNumericThroughCallee() {
        final byte two = numericLiteralTwo();
        RandomData.getInstance(numericHopB(numericHopA(two)));
    }

    private static byte numericLiteralTwo() {
        return 2;
    }

    private static byte numericHopA(final byte v) {
        return v;
    }

    private static byte numericHopB(final byte w) {
        return w;
    }

    /**
     * Join point after a branch: one arm uses TRNG, the other deprecated pseudo — may analysis should still flag
     * {@code getInstance}.
     *
     * @param which zero selects {@code ALG_PSEUDO_RANDOM}, non-zero selects {@code ALG_TRNG}
     */
    public static void entryMayPseudoDependingOnParam(final byte which) {
        final byte b;
        if (which != 0) {
            b = RandomData.ALG_TRNG;
        } else {
            b = RandomData.ALG_PSEUDO_RANDOM;
        }
        RandomData.getInstance(b);
    }

    /**
     * Diamond CFG where one arm produces {@code ALG_PSEUDO_RANDOM} and the other {@code ALG_SECURE_RANDOM}.
     * Both deprecated facts converge at {@code getInstance}; the accumulate function is called twice,
     * exercising either the "current is PSEUDO, return current" branch or the "candidate is PSEUDO,
     * return candidate" branch depending on HashSet iteration order. Should warn (PSEUDO is found).
     *
     * @param which zero selects {@code ALG_PSEUDO_RANDOM}, non-zero selects {@code ALG_SECURE_RANDOM}
     */
    public static void entryBranchPseudoAndSecure(final byte which) {
        final byte alg;
        if (which != 0) {
            alg = RandomData.ALG_SECURE_RANDOM;
        } else {
            alg = RandomData.ALG_PSEUDO_RANDOM;
        }
        RandomData.getInstance(alg);
    }

    /**
     * Same depth as entryDeepPseudoChain but only TRNG; sole CHA root for negative tests.
     */
    public static void entryTrngDeepOnly() {
        final byte ok = RandomData.ALG_TRNG;
        RandomData.getInstance(trngDeepA(ok));
    }

    private static byte trngDeepA(final byte x) {
        return trngDeepB(x);
    }

    private static byte trngDeepB(final byte y) {
        return trngDeepC(y);
    }

    private static byte trngDeepC(final byte z) {
        return z;
    }
}
