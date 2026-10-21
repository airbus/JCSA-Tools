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
import sootup.core.jimple.common.expr.JAndExpr;
import sootup.core.jimple.common.expr.JRemExpr;

/** Effective unsigned low-bit width after mask operations. */
public final class UnsignedBitWidth {

    public static final int UNKNOWN = -1;

    private UnsignedBitWidth() {
    }

    /**
     * Contiguous low-bit width of {@code mask} (bits {@code 0..width-1}).
     *
     * @param mask constant AND mask
     * @return bit width, or {@link #UNKNOWN} when not contiguous from bit zero
     */
    public static int fromMask(final int mask) {
        if (mask <= 0) {
            return UNKNOWN;
        }
        final int width = 32 - Integer.numberOfLeadingZeros(mask);
        if (width >= 32) {
            return 32;
        }
        if (mask == (1 << width) - 1) {
            return width;
        }
        return UNKNOWN;
    }

    public static int merge(final int left, final int right) {
        if (left < 0) {
            return right;
        }
        if (right < 0) {
            return left;
        }
        return Math.min(left, right);
    }

    public static int afterAnd(final int operandWidth, final int mask) {
        final int maskWidth = fromMask(mask);
        if (maskWidth < 0) {
            return operandWidth;
        }
        if (operandWidth < 0) {
            return maskWidth;
        }
        return Math.min(operandWidth, maskWidth);
    }

    public static IntTempKind intTempKind(final int width) {
        if (width <= 0) {
            return IntTempKind.BOTTOM;
        }
        if (width <= 4) {
            return IntTempKind.NARROW;
        }
        if (width <= 8) {
            return IntTempKind.RE_WIDEN_UNSAFE;
        }
        return IntTempKind.BOTTOM;
    }

    public static boolean byteCastOperandUnsafe(final Value op) {
        if (op instanceof JRemExpr) {
            return true;
        }
        return maxContiguousMaskWidthInAndTree(op) > 4;
    }

    public static int maxContiguousMaskWidthInAndTree(final Value v) {
        if (v instanceof final JAndExpr a) {
            int max = 0;
            final int k1 = SignExtensionMask.andLiteralMask(a.getOp1(), a.getOp1(), a.getOp2());
            final int k2 = SignExtensionMask.andLiteralMask(a.getOp2(), a.getOp1(), a.getOp2());
            if (k1 > 0) {
                max = Math.max(max, fromMask(k1));
            }
            if (k2 > 0) {
                max = Math.max(max, fromMask(k2));
            }
            max = Math.max(max, maxContiguousMaskWidthInAndTree(a.getOp1()));
            max = Math.max(max, maxContiguousMaskWidthInAndTree(a.getOp2()));
            return max;
        }
        final int k = SignExtensionMask.andLiteralMask(v);
        return k > 0 ? fromMask(k) : 0;
    }
}
