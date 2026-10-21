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

package com.airbus.esar.analysis.signext;

import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

/** Bit-level sign-extension pollution: set bits may carry spurious sign copies from a narrow load. */
public final class SignExtensionPollution {

    /** Spurious bits after an implicit {@code byte} widen to {@code int}. */
    public static final int BYTE_WIDEN = 0xFFFFFF00;

    /** Spurious bits after an implicit {@code short}/{@code char} widen to {@code int}. */
    public static final int SHORT_WIDEN = 0xFFFF0000;

    private static final int WORD_MASK = 0xFFFFFFFF;

    private SignExtensionPollution() {
    }

    public static int afterAnd(final int pollution, final int constantMask) {
        return pollution & constantMask;
    }

    public static int afterAndBinary(final int leftPollution,
                                     final int rightPollution,
                                     final Value andLeft,
                                     final Value andRight) {
        final int k1 = SignExtensionMask.andLiteralMask(andLeft, andLeft, andRight);
        if (k1 != 0) {
            return afterAndMaskedOperand(rightPollution, k1);
        }
        final int k2 = SignExtensionMask.andLiteralMask(andRight, andLeft, andRight);
        if (k2 != 0) {
            return afterAndMaskedOperand(leftPollution, k2);
        }
        return afterBinop(leftPollution, rightPollution);
    }

    /**
     * Clears sign-ext pollution when {@code mask} covers the shifted byte lane entirely.
     *
     * @param operandSignExt sign-extension pollution on the shifted operand
     * @param mask constant AND mask
     * @return {@code true} when all polluted bits lie in the lane cleared by {@code mask}
     */
    public static boolean maskClearsShiftedLane(final int operandSignExt, final int mask) {
        if (operandSignExt == 0) {
            return true;
        }
        final int shift = Integer.numberOfTrailingZeros(mask);
        if (shift <= 0) {
            return false;
        }
        final int lanePollution = afterShl(BYTE_WIDEN, shift);
        return (operandSignExt & lanePollution) == operandSignExt;
    }

    public static boolean widensFromNarrow(final Type dest) {
        return dest.equals(PrimitiveType.getShort()) || dest.equals(PrimitiveType.getChar())
                || dest.equals(PrimitiveType.getInt()) || dest.equals(PrimitiveType.getLong())
                || dest.equals(PrimitiveType.getFloat()) || dest.equals(PrimitiveType.getDouble());
    }

    public static int afterShl(final int pollution, final int shift) {
        if (shift <= 0) {
            return pollution;
        }
        if (shift >= 32) {
            return 0;
        }
        return (pollution << shift) & WORD_MASK;
    }

    public static int afterUshr(final int pollution, final int shift) {
        if (shift <= 0) {
            return pollution;
        }
        if (shift >= 32) {
            return 0;
        }
        return pollution >>> shift;
    }

    public static int afterShr(final int pollution, final int shift) {
        if (shift <= 0) {
            return pollution;
        }
        if (shift >= 32) {
            return pollution & (1 << 31);
        }
        return pollution >> shift;
    }

    public static int afterBinop(final int left, final int right) {
        return left | right;
    }

    public static int afterByteCast(final int inner) {
        return inner & BYTE_WIDEN;
    }

    public static int afterShortCast(final int inner) {
        return inner & 0xFF00;
    }

    public static int significantMask(final Type dest) {
        if (dest.equals(PrimitiveType.getShort()) || dest.equals(PrimitiveType.getChar())) {
            return 0xFFFF;
        }
        return WORD_MASK;
    }

    public static int intLiteral(final Value v) {
        return switch (v) {
            case final IntConstant ic -> ic.getValue();
            case final LongConstant lc -> {
                final long x = lc.getValue();
                if (x >= 0L && x <= Integer.MAX_VALUE) {
                    yield (int) x;
                }
                yield -1;
            }
            default -> -1;
        };
    }

    public static int constantMask(final Value v, final Value andLeft, final Value andRight) {
        return SignExtensionMask.andLiteralMask(v, andLeft, andRight);
    }

    public static int constantMask(final Value v) {
        return SignExtensionMask.andLiteralMask(v);
    }

    private static int afterAndMaskedOperand(final int operandPollution, final int mask) {
        final int signExt = operandPollution & (BYTE_WIDEN | SHORT_WIDEN);
        if (signExt != 0 && maskClearsShiftedLane(signExt, mask)) {
            return 0;
        }
        return afterAnd(operandPollution, mask);
    }
}
