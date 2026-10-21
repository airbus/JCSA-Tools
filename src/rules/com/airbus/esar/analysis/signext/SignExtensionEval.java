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

import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.expr.AbstractBinopExpr;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.AbstractUnopExpr;
import sootup.core.jimple.common.expr.JAndExpr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.expr.JNeExpr;
import sootup.core.jimple.common.expr.JPhiExpr;
import sootup.core.jimple.common.expr.JRemExpr;
import sootup.core.jimple.common.expr.JShrExpr;
import sootup.core.jimple.common.expr.JShlExpr;
import sootup.core.jimple.common.expr.JUshrExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

/** Compositional pollution evaluation for Jimple values. */
public final class SignExtensionEval {

    private SignExtensionEval() {
    }

    public static SignExtensionValueInfo eval(final Value v, final SignExtensionFact in) {
        return switch (v) {
            case final Local l -> localInfo(l, in);
            case final JArrayRef r -> narrowLoadInfo(r.getType());
            case final JFieldRef f -> narrowLoadInfo(f.getType());
            case final JAndExpr a -> andInfo(a, in);
            case final JShlExpr shl -> shlInfo(shl, in);
            case final JUshrExpr ushr -> ushrInfo(ushr, in);
            case final JShrExpr shr -> shrInfo(shr, in);
            case final JRemExpr rem -> remInfo(rem, in);
            case final JCastExpr c -> castInfo(c, in);
            case final AbstractBinopExpr b -> binopInfo(eval(b.getOp1(), in), eval(b.getOp2(), in));
            case final AbstractUnopExpr u -> eval(u.getOp(), in);
            case final AbstractInvokeExpr i -> invokeInfo(i, in);
            case final JPhiExpr p -> phiInfo(p, in);
            default -> SignExtensionValueInfo.BOTTOM;
        };
    }

    public static boolean expressesByteDerivedPollution(final Value v, final SignExtensionFact in) {
        return expressesNarrowSource(v, in, NarrowSourceKind.BYTE_DERIVED);
    }

    public static boolean expressesNarrowSource(final Value v,
                                                final SignExtensionFact in,
                                                final NarrowSourceKind kind) {
        return switch (v) {
            case final Local l -> localIsNarrowSource(l, in, kind);
            case final JArrayRef r -> narrowLoadIsSource(r.getType(), kind);
            case final JFieldRef f -> narrowLoadIsSource(f.getType(), kind);
            case final JCastExpr c -> expressesNarrowSource(c.getOp(), in, kind);
            case final JAndExpr a ->
                    expressesNarrowSource(a.getOp1(), in, kind) || expressesNarrowSource(a.getOp2(), in, kind);
            case final JShlExpr shl -> expressesNarrowSource(shl.getOp1(), in, kind);
            case final JUshrExpr ushr -> expressesNarrowSource(ushr.getOp1(), in, kind);
            case final JShrExpr shr -> expressesNarrowSource(shr.getOp1(), in, kind);
            case final AbstractBinopExpr b ->
                    expressesNarrowSource(b.getOp1(), in, kind) || expressesNarrowSource(b.getOp2(), in, kind);
            case final AbstractUnopExpr u -> expressesNarrowSource(u.getOp(), in, kind);
            case final AbstractInvokeExpr i -> invokeIsNarrowSource(i, in, kind);
            case final JPhiExpr p -> {
                boolean any = false;
                for (int k = 0; k < p.getArgsSize(); k++) {
                    any |= expressesNarrowSource(p.getArg(k), in, kind);
                }
                yield any;
            }
            default -> false;
        };
    }

    public static SignExtensionValueInfo assignLocalInfo(final LValue lhs,
                                                         final Value rhs,
                                                         final SignExtensionFact in) {
        if (lhs instanceof final Local local && isIntrinsicNarrowLoadRhs(local, rhs)) {
            return SignExtensionValueInfo.ofTypedNarrowLoad();
        }
        if (lhs instanceof final Local local && isShortCharWidenRhs(local, rhs)) {
            return eval(rhs, in);
        }
        final SignExtensionValueInfo evaluated = eval(rhs, in);
        if (evaluated.signExtPollution() != 0) {
            return evaluated;
        }
        if (evaluated.typedNarrowLoad()) {
            return evaluated;
        }
        final IntTempKind kind = classifyIntTempRhs(rhs);
        if (kind != IntTempKind.BOTTOM) {
            return SignExtensionValueInfo.ofIntTemp(kind);
        }
        return SignExtensionValueInfo.BOTTOM;
    }

    /**
     * Type at which {@code op} is consumed in a binary comparison, or {@code null} when Jimple keeps
     * both operands at the same narrow type (e.g. {@code byte == byte}) or {@code op} is not narrowed.
     *
     * @param op narrow operand under test
     * @param other other comparison operand
     * @return promoted primitive type at the compare, or {@code null} when no widen applies
     */
    public static Type comparisonWidenDest(final Value op, final Value other) {
        final Type opType = primitiveTypeOf(op);
        final Type otherType = primitiveTypeOf(other);
        if (opType == null || otherType == null) {
            return null;
        }
        if (opType.equals(otherType) && isNarrowIntegral(opType)) {
            return null;
        }
        if (!isNarrowIntegral(opType)) {
            return null;
        }
        final Type promoted = binaryNumericPromotion(opType, otherType);
        return promoted.equals(opType) ? null : promoted;
    }

    /**
     * Whether sign-extension pollution on {@code op} can change the comparison outcome.
     *
     * @param condition comparison expression
     * @param op narrow operand under test
     * @param other other comparison operand
     * @return {@code false} when sign-ext and unsigned byte semantics agree on the outcome
     */
    public static boolean comparisonPollutionMatters(final AbstractConditionExpr condition,
                                                     final Value op,
                                                     final Value other) {
        final Type opType = primitiveTypeOf(op);
        if (opType == null || !isNarrowIntegral(opType)) {
            return true;
        }
        final Integer literal = intConstantValue(other);
        if (literal == null) {
            return true;
        }
        return ComparisonOutcome.pollutionMatters(condition, opType, literal);
    }

    /**
     * {@code true} when {@code local} is compared to another narrow primitive of a different type
     * (e.g. {@code short != byte}), where sign-extension semantics can still change the outcome.
     *
     * @param local narrow local under test
     * @param other other comparison operand
     * @return {@code true} for cross-narrow-type compares
     */
    static boolean crossNarrowIntegralCompare(final Local local, final Value other) {
        if (other == null) {
            return false;
        }
        final Type sourceType = local.getType();
        if (!isNarrowIntegral(sourceType)) {
            return false;
        }
        final Type otherType = primitiveTypeOf(other);
        return otherType != null && isNarrowIntegral(otherType) && !sourceType.equals(otherType);
    }

    /**
     * {@code true} when {@code op} is a narrow local filled by a same-type load.
     *
     * @param op value under test
     * @param in incoming pollution facts
     * @return {@code true} for invoke/field/array narrow loads assigned to a typed local
     */
    public static boolean isTypedNarrowLoadLocal(final Value op, final SignExtensionFact in) {
        if (!(op instanceof final Local local)) {
            return false;
        }
        return in.isTracked(local) && in.typedNarrowLoad(local);
    }

    /**
     * Ordered {@code byte}/{@code short} vs {@code int} literal in that type's signed range.
     *
     * @param condition comparison expression
     * @param op narrow operand under test
     * @param other other comparison operand
     * @return {@code true} for ordered compares against a signed-range literal
     */
    public static boolean isOrderedInRangeLiteralCompare(final AbstractConditionExpr condition,
                                                         final Value op,
                                                         final Value other) {
        if (condition instanceof JEqExpr || condition instanceof JNeExpr) {
            return false;
        }
        final Type opType = primitiveTypeOf(op);
        final Integer literal = intConstantValue(other);
        if (opType == null || literal == null) {
            return false;
        }
        if (opType.equals(PrimitiveType.getByte())) {
            return literal >= Byte.MIN_VALUE && literal <= Byte.MAX_VALUE;
        }
        if (opType.equals(PrimitiveType.getShort())) {
            return literal >= Short.MIN_VALUE && literal <= Short.MAX_VALUE;
        }
        return false;
    }

    /**
     * {@code byte} compared to an {@code int} literal outside the signed {@code byte} range
     * ({@code == 0xff}, etc.); distinct from sign-ext pollution on an {@code int} temp.
     *
     * @param condition comparison expression
     * @param op narrow operand under test
     * @param other other comparison operand
     * @return {@code true} for eq/ne against an out-of-range literal
     */
    public static boolean isUnsignedByteLiteralCompareTrap(final AbstractConditionExpr condition,
                                                           final Value op,
                                                           final Value other) {
        if (!(condition instanceof JEqExpr) && !(condition instanceof JNeExpr)) {
            return false;
        }
        if (!(op instanceof final Local local) || !local.getType().equals(PrimitiveType.getByte())) {
            return false;
        }
        final Integer literal = intConstantValue(other);
        return literal != null && (literal > 127 || literal < -128);
    }

    public static boolean pollutesAtInvokeArg(final Value arg,
                                              final SignExtensionFact in,
                                              final Type paramType) {
        if (!requiresImplicitWidenAtArg(arg, paramType)) {
            return false;
        }
        return pollutesAtConsumer(arg, in, paramType);
    }

    private static boolean requiresImplicitWidenAtArg(final Value arg, final Type paramType) {
        if (!paramType.equals(PrimitiveType.getShort()) && !paramType.equals(PrimitiveType.getChar())) {
            return true;
        }
        if (paramType.equals(arg.getType())) {
            return false;
        }
        return !(arg instanceof final JCastExpr cast && cast.getType().equals(paramType));
    }

    public static boolean pollutesAtConsumer(final Value source,
                                             final SignExtensionFact in,
                                             final Type dest) {
        return pollutesAtConsumer(source, in, dest, false, null);
    }

    public static boolean pollutesAtConsumer(final Value source,
                                             final SignExtensionFact in,
                                             final Type dest,
                                             final boolean comparePromotion) {
        return pollutesAtConsumer(source, in, dest, comparePromotion, null);
    }

    public static boolean pollutesAtConsumer(final Value source,
                                             final SignExtensionFact in,
                                             final Type dest,
                                             final boolean comparePromotion,
                                             final Value compareOther) {
        if (source instanceof final Local local && in.isTracked(local)) {
            if (comparePromotion && in.typedNarrowLoad(local)
                    && !crossNarrowIntegralCompare(local, compareOther)) {
                return false;
            }
            if (in.typedNarrowLoad(local)) {
                final Type sourceType = local.getType();
                if (isNarrowIntegral(sourceType) && SignExtensionPollution.widensFromNarrow(dest)
                        && !dest.equals(sourceType)) {
                    return true;
                }
            }
        }
        if (!eval(source, in).affectsDestination(SignExtensionPollution.significantMask(dest))) {
            return false;
        }
        if (source instanceof final Local local && in.isTracked(local)) {
            if (in.pollution(local) == 0) {
                return false;
            }
            if (SignExtensionLocals.isStagingAtConsumer(local)) {
                return true;
            }
            final Type sourceType = local.getType();
            if (sourceType.equals(PrimitiveType.getByte())
                    || sourceType.equals(PrimitiveType.getShort())
                    || sourceType.equals(PrimitiveType.getChar())) {
                if (dest.equals(sourceType)) {
                    return true;
                }
                return SignExtensionPollution.widensFromNarrow(dest) && !dest.equals(sourceType);
            }
            return false;
        }
        return expressesByteDerivedPollution(source, in);
    }

    public static boolean isShortCharWidenRhs(final Local local, final Value rhs) {
        final Type t = local.getType();
        if (!t.equals(PrimitiveType.getShort()) && !t.equals(PrimitiveType.getChar())) {
            return false;
        }
        if (rhs instanceof final JCastExpr cast) {
            return cast.getType().equals(PrimitiveType.getShort()) || cast.getType().equals(PrimitiveType.getChar());
        }
        return switch (rhs) {
            case final Local l -> l.getType().equals(PrimitiveType.getByte());
            case final JArrayRef r -> r.getType().equals(PrimitiveType.getByte());
            case final JFieldRef f -> f.getType().equals(PrimitiveType.getByte());
            case final AbstractInvokeExpr i -> i.getType().equals(PrimitiveType.getByte());
            default -> false;
        };
    }

    static boolean isIntrinsicNarrowLoadRhs(final Local lhs, final Value rhs) {
        final Type lhsType = lhs.getType();
        if (!isNarrowIntegral(lhsType)) {
            return false;
        }
        if (!lhsType.equals(primitiveTypeOf(rhs))) {
            return false;
        }
        return rhs instanceof AbstractInvokeExpr
                || rhs instanceof JFieldRef
                || rhs instanceof JArrayRef;
    }

    public static IntTempKind classifyIntTempRhs(final Value rhs) {
        if (rhs instanceof JRemExpr) {
            return IntTempKind.MOD_UNSAFE;
        }
        if (UnsignedBitWidth.maxContiguousMaskWidthInAndTree(rhs) > 4) {
            return IntTempKind.RE_WIDEN_UNSAFE;
        }
        return UnsignedBitWidth.intTempKind(andEffectiveWidth(rhs));
    }

    private static int andEffectiveWidth(final Value v) {
        if (v instanceof final JAndExpr a) {
            final int k1 = SignExtensionMask.andLiteralMask(a.getOp1(), a.getOp1(), a.getOp2());
            final int k2 = SignExtensionMask.andLiteralMask(a.getOp2(), a.getOp1(), a.getOp2());
            if (k1 != 0) {
                return UnsignedBitWidth.afterAnd(andEffectiveWidth(a.getOp2()), k1);
            }
            if (k2 != 0) {
                return UnsignedBitWidth.afterAnd(andEffectiveWidth(a.getOp1()), k2);
            }
            return UnsignedBitWidth.merge(andEffectiveWidth(a.getOp1()), andEffectiveWidth(a.getOp2()));
        }
        if (v instanceof final Local l && l.getType().equals(PrimitiveType.getByte())) {
            return 8;
        }
        if (v instanceof final JArrayRef r && r.getType().equals(PrimitiveType.getByte())) {
            return 8;
        }
        if (v instanceof final JFieldRef f && f.getType().equals(PrimitiveType.getByte())) {
            return 8;
        }
        return UnsignedBitWidth.UNKNOWN;
    }

    private static boolean localIsNarrowSource(final Local l,
                                               final SignExtensionFact in,
                                               final NarrowSourceKind kind) {
        if (!l.getType().equals(PrimitiveType.getByte())) {
            return false;
        }
        if (!in.isTracked(l)) {
            return true;
        }
        return in.pollution(l) != 0;
    }

    private static boolean narrowLoadIsSource(final Type type, final NarrowSourceKind kind) {
        if (type.equals(PrimitiveType.getByte())) {
            return true;
        }
        return kind == NarrowSourceKind.IMPLICIT_NARROW
                && (type.equals(PrimitiveType.getShort()) || type.equals(PrimitiveType.getChar()));
    }

    private static boolean invokeIsNarrowSource(final AbstractInvokeExpr i,
                                                final SignExtensionFact in,
                                                final NarrowSourceKind kind) {
        if (kind == NarrowSourceKind.BYTE_DERIVED) {
            return invokeHasByteDerivedWidenArg(i, in) || i.getType().equals(PrimitiveType.getByte());
        }
        return i.getType().equals(PrimitiveType.getByte()) || i.getType().equals(PrimitiveType.getShort());
    }

    private static SignExtensionValueInfo localInfo(final Local l, final SignExtensionFact in) {
        if (in.isTracked(l)) {
            return in.localInfo(l);
        }
        if (l.getType().equals(PrimitiveType.getByte())) {
            return SignExtensionValueInfo.ofSignExt(SignExtensionPollution.BYTE_WIDEN);
        }
        return SignExtensionValueInfo.BOTTOM;
    }

    private static SignExtensionValueInfo invokeInfo(final AbstractInvokeExpr i, final SignExtensionFact in) {
        SignExtensionValueInfo info = narrowLoadInfo(i.getType());
        final var params = i.getMethodSignature().getParameterTypes();
        for (int k = 0; k < i.getArgCount(); k++) {
            if (SignExtensionPollution.widensFromNarrow(params.get(k))
                    && expressesNarrowSource(i.getArg(k), in, NarrowSourceKind.BYTE_DERIVED)) {
                info = SignExtensionValueInfo.join(info, eval(i.getArg(k), in));
            }
        }
        return info;
    }

    private static boolean invokeHasByteDerivedWidenArg(final AbstractInvokeExpr i, final SignExtensionFact in) {
        final var params = i.getMethodSignature().getParameterTypes();
        for (int k = 0; k < i.getArgCount(); k++) {
            if (SignExtensionPollution.widensFromNarrow(params.get(k))
                    && expressesNarrowSource(i.getArg(k), in, NarrowSourceKind.BYTE_DERIVED)) {
                return true;
            }
        }
        return false;
    }

    private static SignExtensionValueInfo narrowLoadInfo(final Type t) {
        if (t.equals(PrimitiveType.getByte())) {
            return SignExtensionValueInfo.ofSignExt(SignExtensionPollution.BYTE_WIDEN);
        }
        if (t.equals(PrimitiveType.getShort()) || t.equals(PrimitiveType.getChar())) {
            return SignExtensionValueInfo.ofSignExt(SignExtensionPollution.SHORT_WIDEN);
        }
        return SignExtensionValueInfo.BOTTOM;
    }

    private static SignExtensionValueInfo andInfo(final JAndExpr a, final SignExtensionFact in) {
        final SignExtensionValueInfo left = eval(a.getOp1(), in);
        final SignExtensionValueInfo right = eval(a.getOp2(), in);
        SignExtensionValueInfo info = SignExtensionValueInfo.ofSignExt(SignExtensionPollution.afterAndBinary(
                left.signExtPollution(), right.signExtPollution(), a.getOp1(), a.getOp2()));
        if (a.getType().equals(PrimitiveType.getInt())) {
            info = SignExtensionValueInfo.join(info,
                    typedNarrowLoadWidenForAndOperand(a, a.getOp1(), in));
            info = SignExtensionValueInfo.join(info,
                    typedNarrowLoadWidenForAndOperand(a, a.getOp2(), in));
        }
        return info;
    }

    private static SignExtensionValueInfo remInfo(final JRemExpr rem, final SignExtensionFact in) {
        return eval(rem.getOp1(), in);
    }

    private static SignExtensionValueInfo shlInfo(final JShlExpr shl, final SignExtensionFact in) {
        final SignExtensionValueInfo base = eval(shl.getOp1(), in);
        final int n = SignExtensionPollution.intLiteral(shl.getOp2());
        SignExtensionValueInfo info;
        if (n <= 0) {
            info = base;
        } else {
            info = SignExtensionValueInfo.ofSignExt(SignExtensionPollution.afterShl(base.signExtPollution(), n));
        }
        if (shl.getType().equals(PrimitiveType.getInt()) && n > 0) {
            final SignExtensionValueInfo widen = typedNarrowLoadWidenInfo(shl.getOp1(), in);
            if (widen.signExtPollution() != 0) {
                final int shifted = SignExtensionPollution.afterShl(widen.signExtPollution(), n);
                if (shifted != 0) {
                    info = SignExtensionValueInfo.join(info,
                            SignExtensionValueInfo.ofSignExt(shifted));
                }
            }
        }
        return info;
    }

    private static SignExtensionValueInfo ushrInfo(final JUshrExpr ushr, final SignExtensionFact in) {
        final SignExtensionValueInfo base = eval(ushr.getOp1(), in);
        final int n = SignExtensionPollution.intLiteral(ushr.getOp2());
        if (n <= 0) {
            return base;
        }
        return SignExtensionValueInfo.ofSignExt(SignExtensionPollution.afterUshr(base.signExtPollution(), n));
    }

    private static SignExtensionValueInfo shrInfo(final JShrExpr shr, final SignExtensionFact in) {
        final SignExtensionValueInfo base = eval(shr.getOp1(), in);
        final int n = SignExtensionPollution.intLiteral(shr.getOp2());
        if (n <= 0) {
            return base;
        }
        return SignExtensionValueInfo.ofSignExt(SignExtensionPollution.afterShr(base.signExtPollution(), n));
    }

    private static SignExtensionValueInfo castInfo(final JCastExpr c, final SignExtensionFact in) {
        final SignExtensionValueInfo inner = eval(c.getOp(), in);
        final Type t = c.getType();
        if (t.equals(PrimitiveType.getByte())) {
            return byteCastInfo(c.getOp(), inner, in);
        }
        if (t.equals(PrimitiveType.getShort()) || t.equals(PrimitiveType.getChar())) {
            return SignExtensionValueInfo.ofSignExt(
                    SignExtensionPollution.afterShortCast(inner.signExtPollution()));
        }
        return inner;
    }

    private static SignExtensionValueInfo byteCastInfo(final Value op,
                                                       final SignExtensionValueInfo inner,
                                                       final SignExtensionFact in) {
        if (op instanceof final Local l && l.getType().equals(PrimitiveType.getByte())) {
            return inner;
        }
        if (op instanceof final Local il && il.getType().equals(PrimitiveType.getInt())) {
            if (in.reWidenInt(il) || in.unsafeByteCastInt(il)) {
                return SignExtensionValueInfo.ofSignExt(SignExtensionPollution.BYTE_WIDEN);
            }
            if (in.narrowInt(il)) {
                return SignExtensionValueInfo.BOTTOM;
            }
            if (inner.signExtPollution() != 0) {
                return SignExtensionValueInfo.ofSignExt(
                        SignExtensionPollution.afterByteCast(inner.signExtPollution()));
            }
            return SignExtensionValueInfo.BOTTOM;
        }
        return byteCastFromExpr(op, inner);
    }

    private static SignExtensionValueInfo byteCastFromExpr(final Value op, final SignExtensionValueInfo inner) {
        if (op instanceof JRemExpr) {
            return SignExtensionValueInfo.ofSignExt(SignExtensionPollution.BYTE_WIDEN);
        }
        if (UnsignedBitWidth.byteCastOperandUnsafe(op)) {
            return SignExtensionValueInfo.ofSignExt(SignExtensionPollution.BYTE_WIDEN);
        }
        if (UnsignedBitWidth.intTempKind(andEffectiveWidth(op)) == IntTempKind.NARROW) {
            return SignExtensionValueInfo.BOTTOM;
        }
        if (inner.signExtPollution() != 0) {
            return SignExtensionValueInfo.ofSignExt(
                    SignExtensionPollution.afterByteCast(inner.signExtPollution()));
        }
        return SignExtensionValueInfo.BOTTOM;
    }

    private static SignExtensionValueInfo typedNarrowLoadWidenForAndOperand(final JAndExpr a,
                                                                          final Value op,
                                                                          final SignExtensionFact in) {
        if (!(op instanceof final Local local) || !in.typedNarrowLoad(local)) {
            return SignExtensionValueInfo.BOTTOM;
        }
        final int widenMask = narrowWidenMask(local.getType());
        if (widenMask == 0) {
            return SignExtensionValueInfo.BOTTOM;
        }
        final int otherMask = SignExtensionMask.andLiteralMask(
                op == a.getOp1() ? a.getOp2() : a.getOp1(), a.getOp1(), a.getOp2());
        if (otherMask != 0 && SignExtensionPollution.afterAnd(widenMask, otherMask) == 0) {
            return SignExtensionValueInfo.BOTTOM;
        }
        return typedNarrowLoadWidenInfo(op, in);
    }

    private static int narrowWidenMask(final Type type) {
        if (type.equals(PrimitiveType.getByte())) {
            return SignExtensionPollution.BYTE_WIDEN;
        }
        if (type.equals(PrimitiveType.getShort()) || type.equals(PrimitiveType.getChar())) {
            return SignExtensionPollution.SHORT_WIDEN;
        }
        return 0;
    }

    private static SignExtensionValueInfo typedNarrowLoadWidenInfo(final Value op, final SignExtensionFact in) {
        if (!(op instanceof final Local local) || !in.typedNarrowLoad(local)) {
            return SignExtensionValueInfo.BOTTOM;
        }
        if (local.getType().equals(PrimitiveType.getByte())) {
            return SignExtensionValueInfo.ofSignExt(SignExtensionPollution.BYTE_WIDEN);
        }
        if (local.getType().equals(PrimitiveType.getShort()) || local.getType().equals(PrimitiveType.getChar())) {
            return SignExtensionValueInfo.ofSignExt(SignExtensionPollution.SHORT_WIDEN);
        }
        return SignExtensionValueInfo.BOTTOM;
    }

    private static SignExtensionValueInfo binopInfo(final SignExtensionValueInfo left,
                                                    final SignExtensionValueInfo right) {
        return SignExtensionValueInfo.ofSignExt(SignExtensionPollution.afterBinop(
                left.signExtPollution(), right.signExtPollution()));
    }

    private static SignExtensionValueInfo phiInfo(final JPhiExpr p, final SignExtensionFact in) {
        SignExtensionValueInfo info = SignExtensionValueInfo.BOTTOM;
        for (int k = 0; k < p.getArgsSize(); k++) {
            info = SignExtensionValueInfo.join(info, eval(p.getArg(k), in));
        }
        return info;
    }

    static Type primitiveTypeOf(final Value v) {
        return switch (v) {
            case final Local l -> l.getType();
            case final JArrayRef r -> r.getType();
            case final JFieldRef f -> f.getType();
            case final JCastExpr c -> c.getType();
            case final IntConstant ic -> PrimitiveType.getInt();
            case final LongConstant lc -> PrimitiveType.getLong();
            case final AbstractBinopExpr b -> PrimitiveType.getInt();
            case final AbstractInvokeExpr i -> i.getType();
            default -> null;
        };
    }

    static boolean isNarrowIntegral(final Type t) {
        return t.equals(PrimitiveType.getByte())
                || t.equals(PrimitiveType.getShort())
                || t.equals(PrimitiveType.getChar());
    }

    private static Type binaryNumericPromotion(final Type left, final Type right) {
        if (isCategory(left, PrimitiveType.getDouble()) || isCategory(right, PrimitiveType.getDouble())) {
            return PrimitiveType.getDouble();
        }
        if (isCategory(left, PrimitiveType.getFloat()) || isCategory(right, PrimitiveType.getFloat())) {
            return PrimitiveType.getFloat();
        }
        if (isCategory(left, PrimitiveType.getLong()) || isCategory(right, PrimitiveType.getLong())) {
            return PrimitiveType.getLong();
        }
        return PrimitiveType.getInt();
    }

    private static boolean isCategory(final Type t, final Type category) {
        return t != null && t.equals(category);
    }

    static Integer intConstantValue(final Value v) {
        if (v instanceof final IntConstant ic) {
            return ic.getValue();
        }
        if (v instanceof final LongConstant lc) {
            final long x = lc.getValue();
            if (x >= Integer.MIN_VALUE && x <= Integer.MAX_VALUE) {
                return (int) x;
            }
        }
        return null;
    }

    /** Byte-derived sources vs broader implicit narrow loads (short/char fields, etc.). */
    enum NarrowSourceKind {
        BYTE_DERIVED,
        IMPLICIT_NARROW
    }
}
