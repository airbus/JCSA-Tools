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

/**
 * AND mask literals decoded by the narrowest integral operand lane.
 *
 * After binary numeric promotion both operands become {@code int}. A narrow operand defines the lane
 * for decoding negative literals only ({@code (byte)0x80} → {@code -128} → mask {@code 0x80}).
 * Non-negative literals are the promoted {@code int} mask as-is ({@code 0x1ff} stays {@code 511}).
 */
public final class SignExtensionMask {

    private SignExtensionMask() {
    }

    /**
     * Mask bits contributed by literal {@code v} in {@code andLeft & andRight}.
     *
     * @param v literal AND operand
     * @param andLeft left {@code &} operand
     * @param andRight right {@code &} operand
     * @return mask for {@link SignExtensionPollution#afterAnd}, or {@code 0} when {@code v} is not a literal
     */
    public static int andLiteralMask(final Value v, final Value andLeft, final Value andRight) {
        if (!(v instanceof IntConstant) && !(v instanceof LongConstant)) {
            return 0;
        }
        return literalInLane(SignExtensionPollution.intLiteral(v), narrowestIntegralLane(andLeft, andRight));
    }

    /**
     * Mask bits when {@code &} operands are unknown; only non-negative literals are decoded.
     *
     * @param v literal AND operand
     * @return mask for {@link SignExtensionPollution#afterAnd}, or {@code 0} when not a non-negative literal
     */
    public static int andLiteralMask(final Value v) {
        if (!(v instanceof IntConstant) && !(v instanceof LongConstant)) {
            return 0;
        }
        final int literal = SignExtensionPollution.intLiteral(v);
        return literal >= 0 ? literal : 0;
    }

    static Type narrowestIntegralLane(final Value andLeft, final Value andRight) {
        return narrowestIntegralType(
                SignExtensionEval.primitiveTypeOf(andLeft),
                SignExtensionEval.primitiveTypeOf(andRight));
    }

    static Type narrowestIntegralType(final Type left, final Type right) {
        if (left != null && SignExtensionEval.isNarrowIntegral(left)) {
            if (right != null && SignExtensionEval.isNarrowIntegral(right)) {
                return narrowerNarrowType(left, right);
            }
            return left;
        }
        if (right != null && SignExtensionEval.isNarrowIntegral(right)) {
            return right;
        }
        return PrimitiveType.getInt();
    }

    static int literalInLane(final int literal, final Type lane) {
        if (literal >= 0) {
            return literal;
        }
        if (lane.equals(PrimitiveType.getByte())) {
            return literal & 0xFF;
        }
        if (lane.equals(PrimitiveType.getShort()) || lane.equals(PrimitiveType.getChar())) {
            return literal & 0xFFFF;
        }
        return literal;
    }

    private static Type narrowerNarrowType(final Type left, final Type right) {
        if (left.equals(PrimitiveType.getByte()) || right.equals(PrimitiveType.getByte())) {
            return PrimitiveType.getByte();
        }
        return left;
    }
}
