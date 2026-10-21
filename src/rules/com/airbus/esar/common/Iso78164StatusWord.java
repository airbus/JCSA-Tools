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

package com.airbus.esar.common;

import java.util.Arrays;
import java.util.Optional;

/**
 * ISO/IEC 7816-4 validation for {@code ISOException.throwIt(short)} status words.
 */
public final class Iso78164StatusWord {

    public enum ViolationKind {
        INVALID_SW1,
        INVALID_SW2
    }

    public record Violation(int statusWord, ViolationKind kind) {
    }

    private record AllowedSw2(int[] singles, int[][] ranges) {

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof final AllowedSw2 other)) {
                return false;
            }
            return Arrays.equals(singles, other.singles) && Arrays.deepEquals(ranges, other.ranges);
        }

        @Override
        public int hashCode() {
            return 31 * Arrays.hashCode(singles) + Arrays.deepHashCode(ranges);
        }

        @Override
        public String toString() {
            return "AllowedSw2[singles=" + Arrays.toString(singles)
                    + ", ranges=" + Arrays.deepToString(ranges) + "]";
        }

        private static final AllowedSw2 ANY = new AllowedSw2(null, null);

        private static AllowedSw2 any() {
            return ANY;
        }

        private static AllowedSw2 of(final int[] singles, final int[][] ranges) {
            return new AllowedSw2(singles, ranges);
        }

        private boolean allows(final int sw2) {
            if (singles == null && ranges == null) {
                return true;
            }
            if (singles != null) {
                for (final int single : singles) {
                    if (sw2 == single) {
                        return true;
                    }
                }
            }
            if (ranges != null) {
                for (final int[] range : ranges) {
                    if (sw2 >= range[0] && sw2 <= range[1]) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private record StatusWordSpec(int sw1Min, int sw1Max, AllowedSw2 sw2) {

        private static StatusWordSpec sw1(final int sw1, final AllowedSw2 sw2) {
            return new StatusWordSpec(sw1, sw1, sw2);
        }

        private static StatusWordSpec sw1Range(final int sw1Min, final int sw1Max, final AllowedSw2 sw2) {
            return new StatusWordSpec(sw1Min, sw1Max, sw2);
        }

        private boolean matches(final int sw1) {
            return sw1 >= sw1Min && sw1 <= sw1Max;
        }
    }

    private static final StatusWordSpec[] VALID_COMBINATIONS = {
            StatusWordSpec.sw1(0x61, AllowedSw2.any()),
            StatusWordSpec.sw1(0x62, AllowedSw2.of(new int[] {0x00}, new int[][] {{0x02, 0x80}, {0x81, 0x86}})),
            StatusWordSpec.sw1(0x63, AllowedSw2.of(new int[] {0x00, 0x81}, new int[][] {{0xC0, 0xCF}})),
            StatusWordSpec.sw1(0x64, AllowedSw2.of(new int[] {0x00, 0x01}, new int[][] {{0x02, 0x80}})),
            StatusWordSpec.sw1(0x65, AllowedSw2.of(new int[] {0x00, 0x81}, new int[][] {})),
            StatusWordSpec.sw1(0x66, AllowedSw2.any()),
            StatusWordSpec.sw1(0x67, AllowedSw2.of(new int[] {0x00}, new int[][] {})),
            StatusWordSpec.sw1(0x68, AllowedSw2.of(new int[] {0x00}, new int[][] {{0x81, 0x84}})),
            StatusWordSpec.sw1(0x69, AllowedSw2.of(new int[] {0x00, 0x99}, new int[][] {{0x81, 0x88}})),
            StatusWordSpec.sw1(0x6A, AllowedSw2.of(new int[] {0x00}, new int[][] {{0x80, 0x8A}})),
            StatusWordSpec.sw1(0x6B, AllowedSw2.of(new int[] {0x00}, new int[][] {})),
            StatusWordSpec.sw1(0x6C, AllowedSw2.any()),
            StatusWordSpec.sw1(0x6D, AllowedSw2.of(new int[] {0x00}, new int[][] {})),
            StatusWordSpec.sw1(0x6E, AllowedSw2.of(new int[] {0x00}, new int[][] {})),
            StatusWordSpec.sw1(0x6F, AllowedSw2.of(new int[] {0x00}, new int[][] {})),
            StatusWordSpec.sw1(0x90, AllowedSw2.of(new int[] {0x00}, new int[][] {})),
            StatusWordSpec.sw1Range(0x91, 0x9F, AllowedSw2.any()),
    };

    private Iso78164StatusWord() {
    }

    public static Optional<Violation> findViolation(final int statusWord) {
        final int sw1 = sw1Of(statusWord);
        final int sw2 = sw2Of(statusWord);
        final Optional<AllowedSw2> sw2Rule = sw2RuleFor(sw1);
        if (sw2Rule.isEmpty()) {
            return Optional.of(new Violation(statusWord, ViolationKind.INVALID_SW1));
        }
        if (!sw2Rule.get().allows(sw2)) {
            return Optional.of(new Violation(statusWord, ViolationKind.INVALID_SW2));
        }
        return Optional.empty();
    }

    public static int sw1Of(final int statusWord) {
        return (statusWord >> 8) & 0xFF;
    }

    public static int sw2Of(final int statusWord) {
        return statusWord & 0xFF;
    }

    public static String formatStatusWord(final int statusWord) {
        return String.format("0x%04X", statusWord & 0xFFFF);
    }

    private static Optional<AllowedSw2> sw2RuleFor(final int sw1) {
        for (final StatusWordSpec spec : VALID_COMBINATIONS) {
            if (spec.matches(sw1)) {
                return Optional.of(spec.sw2());
            }
        }
        return Optional.empty();
    }
}
