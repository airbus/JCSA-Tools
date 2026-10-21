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

import java.util.function.IntPredicate;

import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.expr.JGeExpr;
import sootup.core.jimple.common.expr.JGtExpr;
import sootup.core.jimple.common.expr.JLeExpr;
import sootup.core.jimple.common.expr.JLtExpr;
import sootup.core.jimple.common.expr.JNeExpr;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

/** Whether sign-extended vs zero-extended narrow semantics change a comparison outcome. */
public final class ComparisonOutcome {

    private ComparisonOutcome() {
    }

    public static boolean pollutionMatters(final AbstractConditionExpr condition,
                                           final Type opType,
                                           final int literal) {
        if (opType.equals(PrimitiveType.getByte())) {
            return outcomesDiffer(condition, literal, 8);
        }
        if (opType.equals(PrimitiveType.getShort()) || opType.equals(PrimitiveType.getChar())) {
            return outcomesDiffer(condition, literal, 16);
        }
        return true;
    }

    private static boolean outcomesDiffer(final AbstractConditionExpr condition,
                                          final int literal,
                                          final int bitWidth) {
        if (condition instanceof JEqExpr) {
            return eqNeDiffers(literal, true, bitWidth);
        }
        if (condition instanceof JNeExpr) {
            return eqNeDiffers(literal, false, bitWidth);
        }
        if (condition instanceof JLtExpr) {
            return orderedDiffers(l -> l < literal, bitWidth);
        }
        if (condition instanceof JLeExpr) {
            return orderedDiffers(l -> l <= literal, bitWidth);
        }
        if (condition instanceof JGtExpr) {
            return orderedDiffers(l -> l > literal, bitWidth);
        }
        if (condition instanceof JGeExpr) {
            return orderedDiffers(l -> l >= literal, bitWidth);
        }
        return true;
    }

    private static boolean eqNeDiffers(final int literal, final boolean eq, final int bitWidth) {
        if (bitWidth == 8 && inSignedByteRange(literal)) {
            return false;
        }
        if (bitWidth == 16 && inSignedShortRange(literal)) {
            return false;
        }
        final int maxUnsigned = (1 << bitWidth) - 1;
        for (int unsigned = 0; unsigned <= maxUnsigned; unsigned++) {
            final int signed = signExtend(unsigned, bitWidth);
            if (eqNeResult(eq, signed, literal) != eqNeResult(eq, unsigned, literal)) {
                return true;
            }
        }
        return false;
    }

    private static boolean inSignedByteRange(final int literal) {
        return literal >= Byte.MIN_VALUE && literal <= Byte.MAX_VALUE;
    }

    private static boolean inSignedShortRange(final int literal) {
        return literal >= Short.MIN_VALUE && literal <= Short.MAX_VALUE;
    }

    private static boolean orderedDiffers(final IntPredicate signedVsLiteral, final int bitWidth) {
        final int maxUnsigned = (1 << bitWidth) - 1;
        for (int unsigned = 0; unsigned <= maxUnsigned; unsigned++) {
            final int signed = signExtend(unsigned, bitWidth);
            if (signedVsLiteral.test(signed) != signedVsLiteral.test(unsigned)) {
                return true;
            }
        }
        return false;
    }

    private static int signExtend(final int unsigned, final int bitWidth) {
        if (bitWidth == 8) {
            return (byte) unsigned;
        }
        return (short) unsigned;
    }

    private static boolean eqNeResult(final boolean eq, final int left, final int literal) {
        return eq ? left == literal : left != literal;
    }
}
