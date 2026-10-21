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

package com.airbus.esar.detect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import sootup.core.signatures.MethodSignature;
import com.airbus.esar.fixture.implicitwiden.*;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

/** Unit tests for JcImplicitByteWidening. */
@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcImplicitIntegralWidening {

    protected List<ProgramVisitorIf> ruleVisitors() {
        return List.of(new JcImplicitIntegralWidening());
    }

    protected String ruleId() {
        return JcImplicitIntegralWidening.RULE_ID;
    }

    private static final List<MethodSignature> IBW_LOCAL_BYTE_TO_INT = List.of(
            JcTestRunner.sig(IbwLocalByteToInt.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_BYTE_EXPRESSION_TO_INT = List.of(
            JcTestRunner.sig(IbwByteExpressionToInt.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_ARRAY_ELEMENT_TO_INT = List.of(
            JcTestRunner.sig(IbwArrayElementToInt.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> IBW_RETURN_INT_LOCAL_FROM_BYTE_ARRAY_ELEMENT_IN_LARGE_METHOD =
            List.of(JcTestRunner.sig(
                    IbwReturnIntLocalFromByteArrayElementInLargeMethod.class,
                    "entry",
                    "void",
                    List.of("byte[]")));
    private static final List<MethodSignature> IBW_IF_STMT_SHORT_NE_BYTE_ARRAY_ELEMENT = List.of(
            JcTestRunner.sig(IbwIfStmtShortNeByteArrayElement.class, "entry", "void", List.of("byte[]", "short")));
    private static final List<MethodSignature> IBW_BYTE_TO_LONG = List.of(
            JcTestRunner.sig(IbwByteToLong.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_BYTE_TO_SHORT = List.of(
            JcTestRunner.sig(IbwByteToShort.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHORT_ARRAY_INITIALIZER_FROM_BYTE = List.of(
            JcTestRunner.sig(IbwShortArrayInitializerFromByte.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_INT_ARRAY_INITIALIZER_FROM_BYTE = List.of(
            JcTestRunner.sig(IbwIntArrayInitializerFromByte.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHORT_ARRAY_STORE_FROM_BYTE = List.of(
            JcTestRunner.sig(IbwShortArrayStoreFromByte.class, "entry", "void", List.of("short[]", "byte")));
    private static final List<MethodSignature> IBW_INT_ARRAY_STORE_FROM_BYTE = List.of(
            JcTestRunner.sig(IbwIntArrayStoreFromByte.class, "entry", "void", List.of("int[]", "byte")));
    private static final List<MethodSignature> IBW_SHORT_ARRAY_STORE_MASKED_BYTE = List.of(
            JcTestRunner.sig(IbwShortArrayStoreMaskedByte.class, "entry", "void", List.of("short[]", "byte")));
    private static final List<MethodSignature> IBW_SHORT_FROM_BYTE_ARRAY_SHL_THEN_MASK = List.of(
            JcTestRunner.sig(IbwShortFromByteArrayShlThenMask.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> IBW_SHORT_FROM_BYTE_ARRAY_SHL_OR_MASKED_LOW = List.of(
            JcTestRunner.sig(IbwShortFromByteArrayShlOrMaskedLow.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> IBW_INT_FROM_BYTE_ARRAY_SHL_OR_MASKED_LOW = List.of(
            JcTestRunner.sig(IbwIntFromByteArrayShlOrMaskedLow.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> IBW_SHORT_FROM_BYTE_ARRAY_SHL7_OR_MASKED_LOW = List.of(
            JcTestRunner.sig(IbwShortFromByteArrayShl7OrMaskedLow.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> IBW_INT_FROM_BYTE_ARRAY_SHL7_OR_MASKED_LOW = List.of(
            JcTestRunner.sig(IbwIntFromByteArrayShl7OrMaskedLow.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> IBW_SHORT_FROM_BYTE_ARRAY_SHL9_OR_MASKED_LOW = List.of(
            JcTestRunner.sig(IbwShortFromByteArrayShl9OrMaskedLow.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> IBW_INT_FROM_BYTE_ARRAY_SHL9_OR_MASKED_LOW = List.of(
            JcTestRunner.sig(IbwIntFromByteArrayShl9OrMaskedLow.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> IBW_SHORT_FROM_BYTE_ARRAY_SHL7_THEN_MASK = List.of(
            JcTestRunner.sig(IbwShortFromByteArrayShl7ThenMask.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> IBW_SHORT_FROM_BYTE_ARRAY_SHL9_THEN_MASK = List.of(
            JcTestRunner.sig(IbwShortFromByteArrayShl9ThenMask.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> IBW_INT_FROM_BYTE_ARRAY_BIG_ENDIAN = List.of(
            JcTestRunner.sig(IbwIntFromByteArrayBigEndian.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> IBW_INT_FROM_BYTE_ARRAY_BIG_ENDIAN_MASK_BEFORE_SHL = List.of(
            JcTestRunner.sig(IbwIntFromByteArrayBigEndianMaskBeforeShl.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> IBW_FN_ARRAY_ELEMENT_AND0XFF00_TO_INT = List.of(
            JcTestRunner.sig(IbwFnArrayElementAnd0xff00ToInt.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> IBW_FN_BYTE_AND0XF00_TO_INT = List.of(
            JcTestRunner.sig(IbwFnByteAnd0xf00ToInt.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_FN_BYTE_AND0XFF0000_TO_INT = List.of(
            JcTestRunner.sig(IbwFnByteAnd0xff0000ToInt.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_FN_BYTE_AND0XFF00_TO_INT = List.of(
            JcTestRunner.sig(IbwFnByteAnd0xff00ToInt.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_FP_MASKED_ARRAY_ELEMENT_SHL16_TO_INT = List.of(
            JcTestRunner.sig(IbwFpMaskedArrayElementShl16ToInt.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> IBW_FP_MASKED_BYTE_SHL8_TO_INT = List.of(
            JcTestRunner.sig(IbwFpMaskedByteShl8ToInt.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_FP_SWITCH_STMT_INT_KEY_MASKED_BYTE_EQ_UNSIGNED_CONSTANT = List.of(
            JcTestRunner.sig(IbwFpSwitchStmtIntKeyMaskedByteEqUnsignedConstant.class, "entry", "void",
                    List.of("byte")));
    private static final List<MethodSignature> IBW_FP_SWITCH_STMT_INT_KEY_FROM_BYTE_EQ_ONE = List.of(
            JcTestRunner.sig(IbwFpSwitchStmtIntKeyFromByteEqOne.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_FP_SWITCH_STMT_INT_KEY_FROM_BYTE_EQ_SIGNED_CASE = List.of(
            JcTestRunner.sig(IbwFpSwitchStmtIntKeyFromByteEqSignedCase.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_FP_ASSIGN_BOOLEAN_INVOKE_FROM_BYTE = List.of(
            JcTestRunner.sig(IbwFpAssignBooleanInvokeFromByte.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_FP_IF_STMT_BOOLEAN_INVOKE_FROM_WIDENED_BYTE = List.of(
            JcTestRunner.sig(IbwFpIfStmtBooleanInvokeFromWidenedByte.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_FP_RETURN_BYTE_EQ_ONE_COMPARE_ONLY = List.of(
            JcTestRunner.sig(IbwFpReturnByteEqOneCompareOnly.class, "entry", "boolean", List.of("byte")));
    private static final List<MethodSignature> IBW_MANY_IMPLICIT_IN_ONE_METHOD = List.of(
            JcTestRunner.sig(IbwManyImplicitInOneMethod.class, "entry", "void", List.of("byte", "byte[]")));
    private static final List<MethodSignature> IBW_MASK0XFF_TO_INT = List.of(
            JcTestRunner.sig(IbwMask0xffToInt.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_MASK0XFF_CASE_TO_INT = List.of(
            JcTestRunner.sig(IbwMask0xFFCaseToInt.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_MASK_THEN_ARITHMETIC_TO_INT = List.of(
            JcTestRunner.sig(IbwMaskThenArithmeticToInt.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_MASK0XFF_ON_LEFT_OF_BYTE = List.of(
            JcTestRunner.sig(IbwMask0xffOnLeftOfByte.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_MASK255_ON_LEFT_OF_BYTE = List.of(
            JcTestRunner.sig(IbwMask255OnLeftOfByte.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_MASK_PARENS0XFF_ON_LEFT_OF_BYTE = List.of(
            JcTestRunner.sig(IbwMaskParens0xffOnLeftOfByte.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_EXPLICIT_INT_CAST_FROM_BYTE = List.of(
            JcTestRunner.sig(IbwExplicitIntCastFromByte.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_TRUNCATED_BYTE_AFTER_MASK0XFF_THEN_WIDEN_TO_INT = List.of(
            JcTestRunner.sig(IbwTruncatedByteAfterMask0xffThenWidenToInt.class, "entry", "void", List.of("int")));
    private static final List<MethodSignature> IBW_TRUNCATED_BYTE_AFTER_DYNAMIC_MASK_THEN_WIDEN_TO_INT = List.of(
            JcTestRunner.sig(IbwTruncatedByteAfterDynamicMaskThenWidenToInt.class, "entry", "void",
                    List.of("int", "byte")));
    private static final List<MethodSignature> IBW_TRUNCATED_BYTE_AFTER_MOD_SIXTEEN_WIDEN_TO_INT = List.of(
            JcTestRunner.sig(IbwTruncatedByteAfterModSixteenThenWidenToInt.class, "entry", "void", List.of("int")));
    /**
     * Outer {@code & 0xff} under {@code (byte)} fails nibble gate; expect one widen report (same family as plain
     * {@code 0xff} mask).
     */
    private static final List<MethodSignature> IBW_TRUNCATED_BYTE_AFTER_NESTED_MASK0X0F_AND_MASK0XFF_THEN_WIDEN_TO_INT =
            List.of(JcTestRunner.sig(IbwTruncatedByteAfterNestedMask0x0fThen0xffThenWidenToInt.class, "entry", "void",
                    List.of("int")));
    private static final List<MethodSignature> IBW_INT_WIDEN_TO_LONG_NO_BYTE = List.of(
            JcTestRunner.sig(IbwIntWidenToLongNoByte.class, "entry", "void", List.of("int")));
    private static final List<MethodSignature> IBW_INVOKE_RETURNS_BYTE_TO_INT = List.of(
            JcTestRunner.sig(IbwInvokeReturnsByteToInt.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_ASSIGN_INVOKE_ARG_BYTE_WIDEN_TO_INT = List.of(
            JcTestRunner.sig(IbwAssignInvokeArgByteWidenToInt.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_ASSIGN_INVOKE_TWO_BYTE_ARGS_WIDEN_TO_INT = List.of(
            JcTestRunner.sig(IbwAssignInvokeTwoByteArgsWidenToInt.class, "entry", "void",
                    List.of("byte", "byte")));
    private static final List<MethodSignature> IBW_ASSIGN_INVOKE_ARG_MASKED_BYTE_TO_INT = List.of(
            JcTestRunner.sig(IbwAssignInvokeArgMaskedByteToInt.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_RETURN_INVOKE_ARG_BYTE_WIDEN_TO_INT = List.of(
            JcTestRunner.sig(IbwReturnInvokeArgByteWidenToInt.class, "entry", "int", List.of("byte")));
    private static final List<MethodSignature> IBW_INVOKE_STMT_BYTE_ARG_TO_BYTE = List.of(
            JcTestRunner.sig(IbwInvokeStmtByteArgToByte.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_INVOKE_STMT_SHORT_ARG_TO_SHORT = List.of(
            JcTestRunner.sig(IbwInvokeStmtShortArgToShort.class, "entry", "void", List.of("short")));
    private static final List<MethodSignature> IBW_INVOKE_STMT_SHORT_LOCAL_IF_ELSE_MASKED_ARG_TO_INT = List.of(
            JcTestRunner.sig(IbwInvokeStmtShortLocalIfElseMaskedArgToInt.class, "entry", "void", List.of("boolean")));
    private static final List<MethodSignature> IBW_IF_STMT_BOOLEAN_INVOKE_FROM_WIDENED_BYTE = List.of(
            JcTestRunner.sig(IbwIfStmtBooleanInvokeFromWidenedByte.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_INVOKE_STMT_BYTE_ARG_WIDEN_TO_INT = List.of(
            JcTestRunner.sig(IbwInvokeStmtByteArgWidenToInt.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_INVOKE_STMT_MASKED_BYTE_ARG_TO_INT = List.of(
            JcTestRunner.sig(IbwInvokeStmtMaskedByteArgToInt.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_INVOKE_STMT_TWO_BYTE_ARGS_TO_INT = List.of(
            JcTestRunner.sig(IbwInvokeStmtTwoByteArgsToInt.class, "entry", "void", List.of("byte", "byte")));
    private static final List<MethodSignature> IBW_INVOKE_STMT_BYTE_ARG_TO_LONG = List.of(
            JcTestRunner.sig(IbwInvokeStmtByteArgToLong.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_INVOKE_SHORT_BYTE_IMPLICIT_WIDEN = List.of(
            JcTestRunner.sig(IbwInvokeShortByteImplicitWiden.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_INVOKE_SHORT_BYTE_EXPLICIT_CAST = List.of(
            JcTestRunner.sig(IbwInvokeShortByteExplicitCast.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_INVOKE_INT_SHORT_IMPLICIT_WIDEN = List.of(
            JcTestRunner.sig(IbwInvokeIntShortImplicitWiden.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_INVOKE_INT_SHORT_EXPLICIT_CAST = List.of(
            JcTestRunner.sig(IbwInvokeIntShortExplicitCast.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_INVOKE_INT_SHORT_IMPLICIT_WIDEN_IN_LOOP = List.of(
            JcTestRunner.sig(IbwInvokeIntShortImplicitWidenInLoop.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_INVOKE_INT_SHORT_EXPLICIT_CAST_IN_LOOP = List.of(
            JcTestRunner.sig(IbwInvokeIntShortExplicitCastInLoop.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_EQ_UNSIGNED_CONSTANT = List.of(
            JcTestRunner.sig(IbwIfStmtByteEqUnsignedConstant.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_EQ_CAST0XFF = List.of(
            JcTestRunner.sig(IbwIfStmtByteEqCast0xff.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_IF_STMT_SHORT_EQ_CAST0XFFFF = List.of(
            JcTestRunner.sig(IbwIfStmtShortEqCast0xffff.class, "entry", "void", List.of("short")));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_NE_UNSIGNED_CONSTANT = List.of(
            JcTestRunner.sig(IbwIfStmtByteNeUnsignedConstant.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_GT_OUT_OF_RANGE_CONSTANT = List.of(
            JcTestRunner.sig(IbwIfStmtByteGtOutOfRangeConstant.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_LT_UNSIGNED_CONSTANT = List.of(
            JcTestRunner.sig(IbwIfStmtByteLtUnsignedConstant.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_GE_UNSIGNED_CONSTANT = List.of(
            JcTestRunner.sig(IbwIfStmtByteGeUnsignedConstant.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_LE_UNSIGNED_CONSTANT = List.of(
            JcTestRunner.sig(IbwIfStmtByteLeUnsignedConstant.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_IF_STMT_SHORT_ORDERED_IN_RANGE_BOUNDS = List.of(
            JcTestRunner.sig(IbwIfStmtShortOrderedInRangeBounds.class, "entry", "void",
                    List.of("short", "byte")));
    private static final List<MethodSignature> IBW_IF_STMT_SHORT_GE_OUT_OF_RANGE = List.of(
            JcTestRunner.sig(IbwIfStmtShortGeOutOfRange.class, "entry", "void", List.of("short", "byte")));
    private static final List<MethodSignature> IBW_SWITCH_STMT_BYTE_EQ_UNSIGNED_CONSTANT = List.of(
            JcTestRunner.sig(IbwSwitchStmtByteEqUnsignedConstant.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SWITCH_STMT_INT_KEY_FROM_BYTE_EQ_UNSIGNED_CONSTANT = List.of(
            JcTestRunner.sig(IbwSwitchStmtIntKeyFromByteEqUnsignedConstant.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SWITCH_STMT_SHORT_EQ_ONE = List.of(
            JcTestRunner.sig(IbwSwitchStmtShortEqOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_WHILE_STMT_BYTE_EQ_UNSIGNED_CONSTANT = List.of(
            JcTestRunner.sig(IbwWhileStmtByteEqUnsignedConstant.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_ASSIGN_FIELD_INVOKE_ARG_IN_ADD_RHS = List.of(
            JcTestRunner.sig(IbwAssignFieldInvokeArgInAddRhs.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SILENT_INT_LOCAL_FROM_BYTE_NO_SINK = List.of(
            JcTestRunner.sig(IbwSilentIntLocalFromByteNoSink.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_ASSIGN_BOOLEAN_INVOKE_FROM_BYTE = List.of(
            JcTestRunner.sig(IbwAssignBooleanInvokeFromByte.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_IF_STMT_MASKED_BYTE_EQ_UNSIGNED_CONSTANT = List.of(
            JcTestRunner.sig(IbwIfStmtMaskedByteEqUnsignedConstant.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_IF_STMT_MASKED0X0F_EQ_UNSIGNED_CONSTANT = List.of(
            JcTestRunner.sig(IbwIfStmtMasked0x0fEqUnsignedConstant.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_AND_CONSTANT_EQ_CONSTANT = List.of(
            JcTestRunner.sig(IbwIfStmtByteAndConstantEqConstant.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_USHR2_THEN_MASK0X3F = List.of(
            JcTestRunner.sig(IbwUshr2ThenMask0x3f.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_USHR1_THEN_MASK0X7F = List.of(
            JcTestRunner.sig(IbwUshr1ThenMask0x7f.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHL2_THEN_MASK0XFC = List.of(
            JcTestRunner.sig(IbwShl2ThenMask0xfc.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHL1_THEN_MASK0XFE = List.of(
            JcTestRunner.sig(IbwShl1ThenMask0xfe.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHL3_THEN_MASK0XF8 = List.of(
            JcTestRunner.sig(IbwShl3ThenMask0xf8.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHL4_THEN_MASK0XF0 = List.of(
            JcTestRunner.sig(IbwShl4ThenMask0xf0.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHL5_THEN_MASK0XE0 = List.of(
            JcTestRunner.sig(IbwShl5ThenMask0xe0.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHL6_THEN_MASK0XC0 = List.of(
            JcTestRunner.sig(IbwShl6ThenMask0xc0.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHL7_THEN_MASK0X80 = List.of(
            JcTestRunner.sig(IbwShl7ThenMask0x80.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHL1_THEN_MASK0X1E0 = List.of(
            JcTestRunner.sig(IbwShl1ThenMask0x1e0.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHL2_THEN_MASK0X3C0 = List.of(
            JcTestRunner.sig(IbwShl2ThenMask0x3c0.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHL3_THEN_MASK0X780 = List.of(
            JcTestRunner.sig(IbwShl3ThenMask0x780.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHL4_THEN_MASK0XF00 = List.of(
            JcTestRunner.sig(IbwShl4ThenMask0xf00.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHL5_THEN_MASK0X1E00 = List.of(
            JcTestRunner.sig(IbwShl5ThenMask0x1e00.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHL6_THEN_MASK0X3C00 = List.of(
            JcTestRunner.sig(IbwShl6ThenMask0x3c00.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHL7_THEN_MASK0X7800 = List.of(
            JcTestRunner.sig(IbwShl7ThenMask0x7800.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHL4_THEN_MASK0XFF = List.of(
            JcTestRunner.sig(IbwShl4ThenMask0xff.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHL3_THEN_MASK0XFE_MISALIGNED = List.of(
            JcTestRunner.sig(IbwShl3ThenMask0xfeMisaligned.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_USHR3_BYTE_MASKED_THEN0X1F = List.of(
            JcTestRunner.sig(IbwUshr3ByteMaskedThen0x1f.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_NARROW_MASK_THEN_SHL2 = List.of(
            JcTestRunner.sig(IbwNarrowMaskThenShl2.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_MASK0XFF_THEN_USHR2 = List.of(
            JcTestRunner.sig(IbwMask0xffThenUshr2.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_MASK0X100_THEN_SHL1 = List.of(
            JcTestRunner.sig(IbwMask0x100ThenShl1.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_MASK0X1FF_THEN_SHL1 = List.of(
            JcTestRunner.sig(IbwMask0x1ffThenShl1.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_RETURN_BYTE_TO_INT = List.of(
            JcTestRunner.sig(IbwReturnByteToInt.class, "entry", "int", List.of("byte")));
    private static final List<MethodSignature> IBW_RETURN_BYTE_MASKED_TO_INT = List.of(
            JcTestRunner.sig(IbwReturnByteMaskedToInt.class, "entry", "int", List.of("byte")));
    private static final List<MethodSignature> IBW_USHR2_THEN_RETURN_MASK0X3F = List.of(
            JcTestRunner.sig(IbwUshr2ThenReturnMask0x3f.class, "entry", "int", List.of("byte")));
    private static final List<MethodSignature> IBW_RETURN_MASKED_BYTE_SHL1_TO_INT = List.of(
            JcTestRunner.sig(IbwReturnMaskedByteShl1ToInt.class, "entry", "int", List.of("byte")));
    private static final List<MethodSignature> IBW_RETURN_MASKED0X0F_AND1_SHL6_TO_INT = List.of(
            JcTestRunner.sig(IbwReturnMasked0x0fAnd1Shl6ToInt.class, "entry", "int", List.of("byte")));
    private static final List<MethodSignature> IBW_RETURN_BYTE_USHR1_WIDEN_TO_INT = List.of(
            JcTestRunner.sig(IbwReturnByteUshr1WidenToInt.class, "entry", "int", List.of("byte")));
    private static final List<MethodSignature> IBW_RETURN_BYTE_WIDEN_TO_LONG = List.of(
            JcTestRunner.sig(IbwReturnByteWidenToLong.class, "entry", "long", List.of("byte")));
    private static final List<MethodSignature> IBW_RETURN_BYTE_WIDEN_TO_SHORT = List.of(
            JcTestRunner.sig(IbwReturnByteWidenToShort.class, "entry", "short", List.of("byte")));
    private static final List<MethodSignature> IBW_RETURN_ARRAY_ELEMENT_WIDEN_TO_INT = List.of(
            JcTestRunner.sig(IbwReturnArrayElementWidenToInt.class, "entry", "int", List.of("byte[]")));
    private static final List<MethodSignature> IBW_RETURN_MASK0XFF_CONJUNCT_LEFT_TO_INT = List.of(
            JcTestRunner.sig(IbwReturnMask0xffConjunctOnLeftToInt.class, "entry", "int", List.of("byte")));
    private static final List<MethodSignature> IBW_RETURN_BYTE_NO_WIDEN_TO_BYTE = List.of(
            JcTestRunner.sig(IbwReturnByteNoWidenToByte.class, "entry", "byte", List.of("byte")));
    private static final List<MethodSignature> IBW_USHR1_THEN_RETURN_MASK0X7F = List.of(
            JcTestRunner.sig(IbwUshr1ThenReturnMask0x7f.class, "entry", "int", List.of("byte")));
    private static final List<MethodSignature> IBW_RETURN_BYTE_ADD_BYTE_WIDEN_TO_INT = List.of(
            JcTestRunner.sig(IbwReturnByteAddByteWidenToInt.class, "entry", "int", List.of("byte")));
    private static final List<MethodSignature> IBW_RETURN_BYTE_WIDEN_TO_FLOAT = List.of(
            JcTestRunner.sig(IbwReturnByteWidenToFloat.class, "entry", "float", List.of("byte")));
    private static final List<MethodSignature> IBW_RETURN_INT_LOCAL_AFTER_PRIOR_MASK0XFF = List.of(
            JcTestRunner.sig(IbwReturnIntLocalAfterPriorStmtMask0xff.class, "entry", "int", List.of("byte")));
    private static final List<MethodSignature> IBW_RETURN_COMPLEX_EXPR_INT_PLUS_MASKED_AND_RAW = List.of(
            JcTestRunner.sig(
                    IbwReturnComplexExprIntPlusMaskedAndRawBytes.class,
                    "entry",
                    "int",
                    List.of("int", "byte", "byte")));
    private static final List<MethodSignature> IBW_ASSIGN_COMPLEX_EXPR_ALL_PARAMS_MASKED = List.of(
            JcTestRunner.sig(
                    IbwAssignComplexExprAllParamsMaskedToInt.class,
                    "entry",
                    "void",
                    List.of("int", "byte", "byte")));
    private static final List<MethodSignature> IBW_RETURN_COMPLEX_EXPR_ALL_PARAMS_MASKED = List.of(
            JcTestRunner.sig(
                    IbwReturnComplexExprAllParamsMaskedToInt.class,
                    "entry",
                    "int",
                    List.of("int", "byte", "byte")));
    private static final List<MethodSignature> IBW_ADD_TWO_UNMASKED_BYTES = List.of(
            JcTestRunner.sig(IbwAddTwoUnmaskedBytes.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SECOND_ASSIGN_INT_LOCAL_FROM_BYTE = List.of(
            JcTestRunner.sig(IbwSecondAssignIntLocalFromByte.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_TERNARY_WIDEN_TO_INT = List.of(
            JcTestRunner.sig(IbwTernaryWidenToInt.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_PARENS_AND_PLUS_MASKED_THEN_RAW = List.of(
            JcTestRunner.sig(IbwParensAndPlusMaskedThenRaw.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_AND_PRECEDENCE_PLUS_LITERAL = List.of(
            JcTestRunner.sig(IbwAndPrecedencePlusLiteral.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_SHL_BYTE = List.of(
            JcTestRunner.sig(IbwShlByte.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_MASK_ON_LEFT_SUPPRESSES_INT = List.of(
            JcTestRunner.sig(IbwMaskOnLeftSuppressesInt.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_LONG_MASK_SUPPRESSES_WIDENING = List.of(
            JcTestRunner.sig(IbwLongMaskSuppressesWidening.class, "entry", "long", List.of("byte")));
    private static final List<MethodSignature> IBW_DOUBLE_WIDEN = List.of(
            JcTestRunner.sig(IbwDoubleWiden.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_IF_STMT_CONST_ON_LEFT_BYTE_ON_RIGHT = List.of(
            JcTestRunner.sig(IbwIfStmtConstOnLeftByteOnRight.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_EQ_IN_RANGE_CONST = List.of(
            JcTestRunner.sig(IbwIfStmtByteEqInRangeConst.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_PARAM_EQ_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtByteParamEqOne.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_INVOKE_EQ_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtByteInvokeEqOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_SHORT_INVOKE_EQ_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtShortInvokeEqOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_SHORT_LOCAL_FROM_INVOKE_GT_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtShortLocalFromInvokeGtOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_GT_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtByteLocalFromInvokeGtOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_SHORT_LOCAL_FROM_FIELD_GT_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtShortLocalFromFieldGtOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_LOCAL_FROM_ARRAY_GT_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtByteLocalFromArrayGtOne.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> IBW_IF_STMT_CHAR_LOCAL_FROM_INVOKE_GT_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtCharLocalFromInvokeGtOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_SHORT_LOCAL_FROM_INVOKE_EQ_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtShortLocalFromInvokeEqOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_SHORT_LOCAL_FROM_INVOKE_NE_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtShortLocalFromInvokeNeOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_SHORT_LOCAL_FROM_INVOKE_LT_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtShortLocalFromInvokeLtOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_SHORT_LOCAL_FROM_INVOKE_GE_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtShortLocalFromInvokeGeOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_SHORT_LOCAL_FROM_INVOKE_LE_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtShortLocalFromInvokeLeOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_CHAR_LOCAL_FROM_INVOKE_EQ_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtCharLocalFromInvokeEqOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_CHAR_LOCAL_FROM_INVOKE_NE_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtCharLocalFromInvokeNeOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_CHAR_LOCAL_FROM_INVOKE_LT_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtCharLocalFromInvokeLtOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_CHAR_LOCAL_FROM_INVOKE_GE_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtCharLocalFromInvokeGeOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_CHAR_LOCAL_FROM_INVOKE_LE_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtCharLocalFromInvokeLeOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_EQ_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtByteLocalFromInvokeEqOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_NE_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtByteLocalFromInvokeNeOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_LT_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtByteLocalFromInvokeLtOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_GE_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtByteLocalFromInvokeGeOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_LE_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtByteLocalFromInvokeLeOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_EQ_UNSIGNED_CONSTANT = List.of(
            JcTestRunner.sig(IbwIfStmtByteLocalFromInvokeEqUnsignedConstant.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_NE_UNSIGNED_CONSTANT = List.of(
            JcTestRunner.sig(IbwIfStmtByteLocalFromInvokeNeUnsignedConstant.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_GT_OUT_OF_RANGE_CONSTANT = List.of(
            JcTestRunner.sig(IbwIfStmtByteLocalFromInvokeGtOutOfRangeConstant.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_LT_UNSIGNED_CONSTANT = List.of(
            JcTestRunner.sig(IbwIfStmtByteLocalFromInvokeLtUnsignedConstant.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_GE_UNSIGNED_CONSTANT = List.of(
            JcTestRunner.sig(IbwIfStmtByteLocalFromInvokeGeUnsignedConstant.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_LE_OUT_OF_RANGE_CONSTANT = List.of(
            JcTestRunner.sig(IbwIfStmtByteLocalFromInvokeLeOutOfRangeConstant.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_INVOKE_GT_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtByteInvokeGtOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_SHORT_INVOKE_GT_ONE = List.of(
            JcTestRunner.sig(IbwIfStmtShortInvokeGtOne.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_BYTE_INVOKE_EQ_UNSIGNED_CONSTANT = List.of(
            JcTestRunner.sig(IbwIfStmtByteInvokeEqUnsignedConstant.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_IF_STMT_SHORT_INVOKE_EQ_OUT_OF_RANGE_CONSTANT = List.of(
            JcTestRunner.sig(IbwIfStmtShortInvokeEqOutOfRangeConstant.class, "entry", "void", List.of()));
    private static final List<MethodSignature> IBW_CAST_NON_CONSTANT_MASK = List.of(
            JcTestRunner.sig(IbwCastNonConstantMask.class, "entry", "void", List.of("byte", "byte")));
    private static final List<MethodSignature> IBW_CAST_WIDE_NIBBLE_MASK = List.of(
            JcTestRunner.sig(IbwCastWideNibbleMask.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_AND_INT_AND_BYTE = List.of(
            JcTestRunner.sig(IbwAndIntAndByte.class, "entry", "void", List.of("int", "byte")));
    private static final List<MethodSignature> IBW_IF_STMT_IN_RANGE_CONST_ON_LEFT = List.of(
            JcTestRunner.sig(IbwIfStmtInRangeConstOnLeft.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_IF_STMT_NEGATIVE_CONST = List.of(
            JcTestRunner.sig(IbwIfStmtNegativeConst.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_TWO_BYTE_COMPARISON = List.of(
            JcTestRunner.sig(IbwTwoByteComparison.class, "entry", "void", List.of("byte", "byte")));
    private static final List<MethodSignature> IBW_LONG_MASK_SMALL = List.of(
            JcTestRunner.sig(IbwLongMaskSmall.class, "entry", "long", List.of("byte")));
    private static final List<MethodSignature> IBW_LONG_MASK_NEGATIVE = List.of(
            JcTestRunner.sig(IbwLongMaskNegative.class, "entry", "long", List.of("byte")));
    private static final List<MethodSignature> IBW_LONG_MASK_TOO_BIG = List.of(
            JcTestRunner.sig(IbwLongMaskTooBig.class, "entry", "long", List.of("byte")));
    private static final List<MethodSignature> IBW_RETURN_NIBBLE_MASKED_BYTE_TO_INT = List.of(
            JcTestRunner.sig(IbwReturnNibbleMaskedByteToInt.class, "entry", "int", List.of("byte")));
    private static final List<MethodSignature> IBW_SHORT_CAST_INT_DIV_EIGHT = List.of(
            JcTestRunner.sig(IbwShortCastIntDivEight.class, "entry", "void", List.of("int")));

    private static final List<MethodSignature> IBW_ASSIGN_BYTE =
            List.of(JcTestRunner.sig(IbwAssignByte.class, "entry", "void", List.of("byte")));
    private static final List<MethodSignature> IBW_BYTE_ARRAY_OR_CAST_STORE = List.of(
            JcTestRunner.sig(IbwByteArrayOrCastStore.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> IBW_BYTE_ARRAY_OR_EQ_MASKED_CAST = List.of(
            JcTestRunner.sig(IbwByteArrayOrEqMaskedCast.class, "entry", "void",
                    List.of("byte[]", "byte", "short")));
    private static final List<MethodSignature> IBW_RETURN_SHORT = List.of(
            JcTestRunner.sig(IbwReturnShort.class, "entry", "short", List.of("short")));

    @Test
    void testWarnsLocalByteToInt(final JcTestRunner analysis) {
        analysis.analyse(IbwLocalByteToInt.class, IBW_LOCAL_BYTE_TO_INT, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwLocalByteToInt.class));
    }

    @Test
    void testWarnsByteExpressionToInt(final JcTestRunner analysis) {
        analysis.analyse(IbwByteExpressionToInt.class, IBW_BYTE_EXPRESSION_TO_INT, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwByteExpressionToInt.class));
    }

    @Test
    void testWarnsArrayElementToInt(final JcTestRunner analysis) {
        analysis.analyse(IbwArrayElementToInt.class, IBW_ARRAY_ELEMENT_TO_INT, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwArrayElementToInt.class));
    }

    @Test
    void testWarnsReturnIntLocalFromByteArrayElementInLargeMethod(final JcTestRunner analysis) {
        analysis.analyse(
                IbwReturnIntLocalFromByteArrayElementInLargeMethod.class,
                IBW_RETURN_INT_LOCAL_FROM_BYTE_ARRAY_ELEMENT_IN_LARGE_METHOD,
                ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwReturnIntLocalFromByteArrayElementInLargeMethod.class));
    }

    @Test
    void testWarnsIfStmtShortNeByteArrayElement(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtShortNeByteArrayElement.class,
                IBW_IF_STMT_SHORT_NE_BYTE_ARRAY_ELEMENT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwIfStmtShortNeByteArrayElement.class));
    }

    @Test
    void testWarnsByteToLong(final JcTestRunner analysis) {
        analysis.analyse(IbwByteToLong.class, IBW_BYTE_TO_LONG, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwByteToLong.class));
    }

    @Test
    void testWarnsReturnByteToInt(final JcTestRunner analysis) {
        analysis.analyse(IbwReturnByteToInt.class, IBW_RETURN_BYTE_TO_INT, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwReturnByteToInt.class));
    }

    @Test
    void testSilentReturnByteMaskedToInt(final JcTestRunner analysis) {
        analysis.analyse(IbwReturnByteMaskedToInt.class, IBW_RETURN_BYTE_MASKED_TO_INT, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwReturnByteMaskedToInt.class));
    }

    @Test
    void testReturnWarnsComplexExpressionIntAndTwoBytes(final JcTestRunner analysis) {
        analysis.analyse(
                IbwReturnComplexExprIntPlusMaskedAndRawBytes.class,
                IBW_RETURN_COMPLEX_EXPR_INT_PLUS_MASKED_AND_RAW,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwReturnComplexExprIntPlusMaskedAndRawBytes.class));
    }

    @Test
    void testSilentComplexExpressionAllBytesMaskedInAssignment(final JcTestRunner analysis) {
        analysis.analyse(
                IbwAssignComplexExprAllParamsMaskedToInt.class,
                IBW_ASSIGN_COMPLEX_EXPR_ALL_PARAMS_MASKED,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwAssignComplexExprAllParamsMaskedToInt.class));
    }

    @Test
    void testReturnSilentComplexExpressionAllBytesMaskedInReturn(final JcTestRunner analysis) {
        analysis.analyse(
                IbwReturnComplexExprAllParamsMaskedToInt.class,
                IBW_RETURN_COMPLEX_EXPR_ALL_PARAMS_MASKED,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwReturnComplexExprAllParamsMaskedToInt.class));
    }

    @Test
    void testReturnSilentIntLocalMaskOnlyInPriorStatement(final JcTestRunner analysis) {
        analysis.analyse(
                IbwReturnIntLocalAfterPriorStmtMask0xff.class,
                IBW_RETURN_INT_LOCAL_AFTER_PRIOR_MASK0XFF,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwReturnIntLocalAfterPriorStmtMask0xff.class));
    }

    @Test
    void testWarnsTruncatedByteAfterMaskedInt(final JcTestRunner analysis) {
        analysis.analyse(
                IbwTruncatedByteAfterMask0xffThenWidenToInt.class,
                IBW_TRUNCATED_BYTE_AFTER_MASK0XFF_THEN_WIDEN_TO_INT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwTruncatedByteAfterMask0xffThenWidenToInt.class));
    }

    @Test
    void testWarnsTruncatedByteAfterDynamicMaskThenWiden(final JcTestRunner analysis) {
        analysis.analyse(
                IbwTruncatedByteAfterDynamicMaskThenWidenToInt.class,
                IBW_TRUNCATED_BYTE_AFTER_DYNAMIC_MASK_THEN_WIDEN_TO_INT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwTruncatedByteAfterDynamicMaskThenWidenToInt.class));
    }

    @Test
    void testWarnsTruncatedByteAfterModSixteenNonAndOperandUnderCast(final JcTestRunner analysis) {
        analysis.analyse(
                IbwTruncatedByteAfterModSixteenThenWidenToInt.class,
                IBW_TRUNCATED_BYTE_AFTER_MOD_SIXTEEN_WIDEN_TO_INT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwTruncatedByteAfterModSixteenThenWidenToInt.class));
    }

    @Test
    void testWarnsTruncatedByteAfterNestedMaskWhereOuterLiteralFailsNibbleGate(final JcTestRunner analysis) {
        analysis.analyse(
                IbwTruncatedByteAfterNestedMask0x0fThen0xffThenWidenToInt.class,
                IBW_TRUNCATED_BYTE_AFTER_NESTED_MASK0X0F_AND_MASK0XFF_THEN_WIDEN_TO_INT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(),
                IbwTruncatedByteAfterNestedMask0x0fThen0xffThenWidenToInt.class));
    }

    @Test
    void testWarnsByteToShort(final JcTestRunner analysis) {
        analysis.analyse(IbwByteToShort.class, IBW_BYTE_TO_SHORT, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwByteToShort.class));
    }

    @Test
    void testWarnsShortFromByteArrayShiftHighByteThenMask(final JcTestRunner analysis) {
        analysis.analyse(
                IbwShortFromByteArrayShlThenMask.class,
                IBW_SHORT_FROM_BYTE_ARRAY_SHL_THEN_MASK,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwShortFromByteArrayShlThenMask.class));
    }

    @Test
    void testSilentShortFromByteArrayShlOrMaskedLow(final JcTestRunner analysis) {
        analysis.analyse(
                IbwShortFromByteArrayShlOrMaskedLow.class,
                IBW_SHORT_FROM_BYTE_ARRAY_SHL_OR_MASKED_LOW,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwShortFromByteArrayShlOrMaskedLow.class));
    }

    @Test
    void testWarnsIntFromByteArrayShlOrMaskedLow(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIntFromByteArrayShlOrMaskedLow.class,
                IBW_INT_FROM_BYTE_ARRAY_SHL_OR_MASKED_LOW,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwIntFromByteArrayShlOrMaskedLow.class));
    }

    @Test
    void testWarnsShortFromByteArrayShl7OrMaskedLow(final JcTestRunner analysis) {
        analysis.analyse(
                IbwShortFromByteArrayShl7OrMaskedLow.class,
                IBW_SHORT_FROM_BYTE_ARRAY_SHL7_OR_MASKED_LOW,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwShortFromByteArrayShl7OrMaskedLow.class));
    }

    @Test
    void testWarnsIntFromByteArrayShl7OrMaskedLow(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIntFromByteArrayShl7OrMaskedLow.class,
                IBW_INT_FROM_BYTE_ARRAY_SHL7_OR_MASKED_LOW,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwIntFromByteArrayShl7OrMaskedLow.class));
    }

    @Test
    void testSilentShortFromByteArrayShl9OrMaskedLow(final JcTestRunner analysis) {
        analysis.analyse(
                IbwShortFromByteArrayShl9OrMaskedLow.class,
                IBW_SHORT_FROM_BYTE_ARRAY_SHL9_OR_MASKED_LOW,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwShortFromByteArrayShl9OrMaskedLow.class));
    }

    @Test
    void testWarnsIntFromByteArrayShl9OrMaskedLow(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIntFromByteArrayShl9OrMaskedLow.class,
                IBW_INT_FROM_BYTE_ARRAY_SHL9_OR_MASKED_LOW,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwIntFromByteArrayShl9OrMaskedLow.class));
    }

    @Test
    void testSilentShortFromByteArrayShl7ThenMask(final JcTestRunner analysis) {
        analysis.analyse(
                IbwShortFromByteArrayShl7ThenMask.class,
                IBW_SHORT_FROM_BYTE_ARRAY_SHL7_THEN_MASK,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwShortFromByteArrayShl7ThenMask.class));
    }

    @Test
    void testSilentShortFromByteArrayShl9ThenMask(final JcTestRunner analysis) {
        analysis.analyse(
                IbwShortFromByteArrayShl9ThenMask.class,
                IBW_SHORT_FROM_BYTE_ARRAY_SHL9_THEN_MASK,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwShortFromByteArrayShl9ThenMask.class));
    }

    @Test
    void testSilentIntFromByteArrayBigEndianShlThenMask(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIntFromByteArrayBigEndian.class,
                IBW_INT_FROM_BYTE_ARRAY_BIG_ENDIAN,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIntFromByteArrayBigEndian.class));
    }

    @Test
    void testSilentIntFromByteArrayBigEndianMaskBeforeShl(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIntFromByteArrayBigEndianMaskBeforeShl.class,
                IBW_INT_FROM_BYTE_ARRAY_BIG_ENDIAN_MASK_BEFORE_SHL,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIntFromByteArrayBigEndianMaskBeforeShl.class));
    }

    @Test
    void testWarnsShortArrayInitializerFromByte(final JcTestRunner analysis) {
        analysis.analyse(
                IbwShortArrayInitializerFromByte.class,
                IBW_SHORT_ARRAY_INITIALIZER_FROM_BYTE,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwShortArrayInitializerFromByte.class));
    }

    @Test
    void testWarnsIntArrayInitializerFromByte(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIntArrayInitializerFromByte.class,
                IBW_INT_ARRAY_INITIALIZER_FROM_BYTE,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwIntArrayInitializerFromByte.class));
    }

    @Test
    void testWarnsShortArrayStoreFromByte(final JcTestRunner analysis) {
        analysis.analyse(
                IbwShortArrayStoreFromByte.class,
                IBW_SHORT_ARRAY_STORE_FROM_BYTE,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwShortArrayStoreFromByte.class));
    }

    @Test
    void testWarnsIntArrayStoreFromByte(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIntArrayStoreFromByte.class,
                IBW_INT_ARRAY_STORE_FROM_BYTE,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwIntArrayStoreFromByte.class));
    }

    @Test
    void testSilentShortArrayStoreMaskedByte(final JcTestRunner analysis) {
        analysis.analyse(
                IbwShortArrayStoreMaskedByte.class,
                IBW_SHORT_ARRAY_STORE_MASKED_BYTE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwShortArrayStoreMaskedByte.class));
    }

    @Test
    void testWarnsInvokeReturnsByteToInt(final JcTestRunner analysis) {
        analysis.analyse(IbwInvokeReturnsByteToInt.class, IBW_INVOKE_RETURNS_BYTE_TO_INT, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwInvokeReturnsByteToInt.class));
    }

    @Test
    void testWarnsAssignInvokeArgByteWidenToInt(final JcTestRunner analysis) {
        analysis.analyse(
                IbwAssignInvokeArgByteWidenToInt.class,
                IBW_ASSIGN_INVOKE_ARG_BYTE_WIDEN_TO_INT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwAssignInvokeArgByteWidenToInt.class));
    }

    @Test
    void testWarnsAssignInvokeTwoByteArgsWidenToInt(final JcTestRunner analysis) {
        analysis.analyse(
                IbwAssignInvokeTwoByteArgsWidenToInt.class,
                IBW_ASSIGN_INVOKE_TWO_BYTE_ARGS_WIDEN_TO_INT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwAssignInvokeTwoByteArgsWidenToInt.class));
    }

    @Test
    void testSilentAssignInvokeArgMaskedByteToInt(final JcTestRunner analysis) {
        analysis.analyse(
                IbwAssignInvokeArgMaskedByteToInt.class,
                IBW_ASSIGN_INVOKE_ARG_MASKED_BYTE_TO_INT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwAssignInvokeArgMaskedByteToInt.class));
    }

    @Test
    void testWarnsReturnInvokeArgByteWidenToInt(final JcTestRunner analysis) {
        analysis.analyse(
                IbwReturnInvokeArgByteWidenToInt.class,
                IBW_RETURN_INVOKE_ARG_BYTE_WIDEN_TO_INT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwReturnInvokeArgByteWidenToInt.class));
    }

    @Test
    void testSilentInvokeStmtByteArgToByte(final JcTestRunner analysis) {
        analysis.analyse(
                IbwInvokeStmtByteArgToByte.class,
                IBW_INVOKE_STMT_BYTE_ARG_TO_BYTE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwInvokeStmtByteArgToByte.class));
    }

    @Test
    void testSilentInvokeStmtShortArgToShort(final JcTestRunner analysis) {
        analysis.analyse(
                IbwInvokeStmtShortArgToShort.class,
                IBW_INVOKE_STMT_SHORT_ARG_TO_SHORT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwInvokeStmtShortArgToShort.class));
    }

    @Test
    void testSilentInvokeStmtShortLocalIfElseMaskedArgToInt(final JcTestRunner analysis) {
        analysis.analyse(
                IbwInvokeStmtShortLocalIfElseMaskedArgToInt.class,
                IBW_INVOKE_STMT_SHORT_LOCAL_IF_ELSE_MASKED_ARG_TO_INT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwInvokeStmtShortLocalIfElseMaskedArgToInt.class));
    }

    @Test
    void testWarnsIfStmtBooleanInvokeFromWidenedByte(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtBooleanInvokeFromWidenedByte.class,
                IBW_IF_STMT_BOOLEAN_INVOKE_FROM_WIDENED_BYTE,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwIfStmtBooleanInvokeFromWidenedByte.class));
    }

    @Test
    void testWarnsInvokeStmtByteArgToInt(final JcTestRunner analysis) {
        analysis.analyse(
                IbwInvokeStmtByteArgWidenToInt.class,
                IBW_INVOKE_STMT_BYTE_ARG_WIDEN_TO_INT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwInvokeStmtByteArgWidenToInt.class));
    }

    @Test
    void testWarnsInvokeStmtTwoByteArgsToInt(final JcTestRunner analysis) {
        analysis.analyse(
                IbwInvokeStmtTwoByteArgsToInt.class,
                IBW_INVOKE_STMT_TWO_BYTE_ARGS_TO_INT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwInvokeStmtTwoByteArgsToInt.class));
    }

    @Test
    void testWarnsInvokeStmtByteArgToLong(final JcTestRunner analysis) {
        analysis.analyse(
                IbwInvokeStmtByteArgToLong.class,
                IBW_INVOKE_STMT_BYTE_ARG_TO_LONG,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwInvokeStmtByteArgToLong.class));
    }

    @Test
    void testSilentInvokeStmtMaskedByteArgToInt(final JcTestRunner analysis) {
        analysis.analyse(
                IbwInvokeStmtMaskedByteArgToInt.class,
                IBW_INVOKE_STMT_MASKED_BYTE_ARG_TO_INT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwInvokeStmtMaskedByteArgToInt.class));
    }

    @Test
    void testSilentInvokeShortByteImplicitWiden(final JcTestRunner analysis) {
        analysis.analyse(
                IbwInvokeShortByteImplicitWiden.class,
                IBW_INVOKE_SHORT_BYTE_IMPLICIT_WIDEN,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwInvokeShortByteImplicitWiden.class));
    }

    @Test
    void testSilentInvokeShortByteExplicitCast(final JcTestRunner analysis) {
        analysis.analyse(
                IbwInvokeShortByteExplicitCast.class,
                IBW_INVOKE_SHORT_BYTE_EXPLICIT_CAST,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwInvokeShortByteExplicitCast.class));
    }

    @Test
    void testWarnsInvokeIntShortImplicitWiden(final JcTestRunner analysis) {
        analysis.analyse(
                IbwInvokeIntShortImplicitWiden.class,
                IBW_INVOKE_INT_SHORT_IMPLICIT_WIDEN,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwInvokeIntShortImplicitWiden.class));
    }

    @Test
    void testSilentInvokeIntShortExplicitCast(final JcTestRunner analysis) {
        analysis.analyse(
                IbwInvokeIntShortExplicitCast.class,
                IBW_INVOKE_INT_SHORT_EXPLICIT_CAST,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwInvokeIntShortExplicitCast.class));
    }

    @Test
    void testWarnsInvokeIntShortImplicitWidenInLoop(final JcTestRunner analysis) {
        analysis.analyse(
                IbwInvokeIntShortImplicitWidenInLoop.class,
                IBW_INVOKE_INT_SHORT_IMPLICIT_WIDEN_IN_LOOP,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwInvokeIntShortImplicitWidenInLoop.class));
    }

    @Test
    void testSilentInvokeIntShortExplicitCastInLoop(final JcTestRunner analysis) {
        analysis.analyse(
                IbwInvokeIntShortExplicitCastInLoop.class,
                IBW_INVOKE_INT_SHORT_EXPLICIT_CAST_IN_LOOP,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwInvokeIntShortExplicitCastInLoop.class));
    }

    @Test
    void testSilentIfStmtShortEqCast0xffff(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtShortEqCast0xffff.class,
                IBW_IF_STMT_SHORT_EQ_CAST0XFFFF,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtShortEqCast0xffff.class));
    }

    @Test
    void testSilentIfStmtByteEqCast0xff(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteEqCast0xff.class,
                IBW_IF_STMT_BYTE_EQ_CAST0XFF,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtByteEqCast0xff.class));
    }

    @Test
    void testWarnsIfStmtByteEqUnsignedConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteEqUnsignedConstant.class,
                IBW_IF_STMT_BYTE_EQ_UNSIGNED_CONSTANT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwIfStmtByteEqUnsignedConstant.class));
    }

    @Test
    void testWarnsIfStmtByteNeUnsignedConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteNeUnsignedConstant.class,
                IBW_IF_STMT_BYTE_NE_UNSIGNED_CONSTANT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwIfStmtByteNeUnsignedConstant.class));
    }

    @Test
    void testWarnsIfStmtByteGtOutOfRangeConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteGtOutOfRangeConstant.class,
                IBW_IF_STMT_BYTE_GT_OUT_OF_RANGE_CONSTANT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwIfStmtByteGtOutOfRangeConstant.class));
    }

    @Test
    void testWarnsIfStmtByteLtUnsignedConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteLtUnsignedConstant.class,
                IBW_IF_STMT_BYTE_LT_UNSIGNED_CONSTANT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwIfStmtByteLtUnsignedConstant.class));
    }

    @Test
    void testWarnsIfStmtByteGeUnsignedConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteGeUnsignedConstant.class,
                IBW_IF_STMT_BYTE_GE_UNSIGNED_CONSTANT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwIfStmtByteGeUnsignedConstant.class));
    }

    @Test
    void testSilentIfStmtByteLeUnsignedConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteLeUnsignedConstant.class,
                IBW_IF_STMT_BYTE_LE_UNSIGNED_CONSTANT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtByteLeUnsignedConstant.class));
    }

    @Test
    void testSilentIfStmtShortOrderedInRangeBounds(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtShortOrderedInRangeBounds.class,
                IBW_IF_STMT_SHORT_ORDERED_IN_RANGE_BOUNDS,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtShortOrderedInRangeBounds.class));
    }

    @Test
    void testWarnsIfStmtShortGeOutOfRange(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtShortGeOutOfRange.class,
                IBW_IF_STMT_SHORT_GE_OUT_OF_RANGE,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwIfStmtShortGeOutOfRange.class));
    }

    @Test
    void testSilentSwitchStmtByteEqUnsignedConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwSwitchStmtByteEqUnsignedConstant.class,
                IBW_SWITCH_STMT_BYTE_EQ_UNSIGNED_CONSTANT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwSwitchStmtByteEqUnsignedConstant.class));
    }

    @Test
    void testWarnsSwitchStmtIntKeyFromByteEqUnsignedConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwSwitchStmtIntKeyFromByteEqUnsignedConstant.class,
                IBW_SWITCH_STMT_INT_KEY_FROM_BYTE_EQ_UNSIGNED_CONSTANT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwSwitchStmtIntKeyFromByteEqUnsignedConstant.class));
    }

    @Test
    void testSilentSwitchStmtShortEqOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwSwitchStmtShortEqOne.class,
                IBW_SWITCH_STMT_SHORT_EQ_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwSwitchStmtShortEqOne.class));
    }

    @Test
    void testWarnsWhileStmtByteEqUnsignedConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwWhileStmtByteEqUnsignedConstant.class,
                IBW_WHILE_STMT_BYTE_EQ_UNSIGNED_CONSTANT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwWhileStmtByteEqUnsignedConstant.class));
    }

    @Test
    void testWarnsAssignFieldInvokeArgInAddRhs(final JcTestRunner analysis) {
        analysis.analyse(
                IbwAssignFieldInvokeArgInAddRhs.class,
                IBW_ASSIGN_FIELD_INVOKE_ARG_IN_ADD_RHS,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwAssignFieldInvokeArgInAddRhs.class));
    }

    @Test
    void testSilentIntLocalFromByteNoSink(final JcTestRunner analysis) {
        analysis.analyse(
                IbwSilentIntLocalFromByteNoSink.class,
                IBW_SILENT_INT_LOCAL_FROM_BYTE_NO_SINK,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwSilentIntLocalFromByteNoSink.class));
    }

    @Test
    void testWarnsAssignBooleanInvokeFromByte(final JcTestRunner analysis) {
        analysis.analyse(
                IbwAssignBooleanInvokeFromByte.class,
                IBW_ASSIGN_BOOLEAN_INVOKE_FROM_BYTE,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwAssignBooleanInvokeFromByte.class));
    }

    @Test
    void testSilentIfStmtByteInvokeEqOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteInvokeEqOne.class,
                IBW_IF_STMT_BYTE_INVOKE_EQ_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtByteInvokeEqOne.class));
    }

    @Test
    void testSilentIfStmtShortInvokeEqOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtShortInvokeEqOne.class,
                IBW_IF_STMT_SHORT_INVOKE_EQ_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtShortInvokeEqOne.class));
    }

    @Test
    void testSilentIfStmtShortLocalFromInvokeGtOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtShortLocalFromInvokeGtOne.class,
                IBW_IF_STMT_SHORT_LOCAL_FROM_INVOKE_GT_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtShortLocalFromInvokeGtOne.class));
    }

    @Test
    void testSilentIfStmtByteLocalFromInvokeGtOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteLocalFromInvokeGtOne.class,
                IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_GT_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtByteLocalFromInvokeGtOne.class));
    }

    @Test
    void testSilentIfStmtShortLocalFromFieldGtOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtShortLocalFromFieldGtOne.class,
                IBW_IF_STMT_SHORT_LOCAL_FROM_FIELD_GT_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtShortLocalFromFieldGtOne.class));
    }

    @Test
    void testSilentIfStmtByteLocalFromArrayGtOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteLocalFromArrayGtOne.class,
                IBW_IF_STMT_BYTE_LOCAL_FROM_ARRAY_GT_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtByteLocalFromArrayGtOne.class));
    }

    @Test
    void testSilentIfStmtCharLocalFromInvokeGtOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtCharLocalFromInvokeGtOne.class,
                IBW_IF_STMT_CHAR_LOCAL_FROM_INVOKE_GT_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtCharLocalFromInvokeGtOne.class));
    }

    @Test
    void testSilentIfStmtShortLocalFromInvokeEqOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtShortLocalFromInvokeEqOne.class,
                IBW_IF_STMT_SHORT_LOCAL_FROM_INVOKE_EQ_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtShortLocalFromInvokeEqOne.class));
    }

    @Test
    void testSilentIfStmtShortLocalFromInvokeNeOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtShortLocalFromInvokeNeOne.class,
                IBW_IF_STMT_SHORT_LOCAL_FROM_INVOKE_NE_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtShortLocalFromInvokeNeOne.class));
    }

    @Test
    void testSilentIfStmtShortLocalFromInvokeLtOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtShortLocalFromInvokeLtOne.class,
                IBW_IF_STMT_SHORT_LOCAL_FROM_INVOKE_LT_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtShortLocalFromInvokeLtOne.class));
    }

    @Test
    void testSilentIfStmtShortLocalFromInvokeGeOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtShortLocalFromInvokeGeOne.class,
                IBW_IF_STMT_SHORT_LOCAL_FROM_INVOKE_GE_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtShortLocalFromInvokeGeOne.class));
    }

    @Test
    void testSilentIfStmtShortLocalFromInvokeLeOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtShortLocalFromInvokeLeOne.class,
                IBW_IF_STMT_SHORT_LOCAL_FROM_INVOKE_LE_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtShortLocalFromInvokeLeOne.class));
    }

    @Test
    void testSilentIfStmtCharLocalFromInvokeEqOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtCharLocalFromInvokeEqOne.class,
                IBW_IF_STMT_CHAR_LOCAL_FROM_INVOKE_EQ_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtCharLocalFromInvokeEqOne.class));
    }

    @Test
    void testSilentIfStmtCharLocalFromInvokeNeOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtCharLocalFromInvokeNeOne.class,
                IBW_IF_STMT_CHAR_LOCAL_FROM_INVOKE_NE_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtCharLocalFromInvokeNeOne.class));
    }

    @Test
    void testSilentIfStmtCharLocalFromInvokeLtOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtCharLocalFromInvokeLtOne.class,
                IBW_IF_STMT_CHAR_LOCAL_FROM_INVOKE_LT_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtCharLocalFromInvokeLtOne.class));
    }

    @Test
    void testSilentIfStmtCharLocalFromInvokeGeOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtCharLocalFromInvokeGeOne.class,
                IBW_IF_STMT_CHAR_LOCAL_FROM_INVOKE_GE_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtCharLocalFromInvokeGeOne.class));
    }

    @Test
    void testSilentIfStmtCharLocalFromInvokeLeOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtCharLocalFromInvokeLeOne.class,
                IBW_IF_STMT_CHAR_LOCAL_FROM_INVOKE_LE_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtCharLocalFromInvokeLeOne.class));
    }

    @Test
    void testSilentIfStmtByteLocalFromInvokeEqOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteLocalFromInvokeEqOne.class,
                IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_EQ_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtByteLocalFromInvokeEqOne.class));
    }

    @Test
    void testSilentIfStmtByteLocalFromInvokeNeOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteLocalFromInvokeNeOne.class,
                IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_NE_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtByteLocalFromInvokeNeOne.class));
    }

    @Test
    void testSilentIfStmtByteLocalFromInvokeLtOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteLocalFromInvokeLtOne.class,
                IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_LT_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtByteLocalFromInvokeLtOne.class));
    }

    @Test
    void testSilentIfStmtByteLocalFromInvokeGeOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteLocalFromInvokeGeOne.class,
                IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_GE_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtByteLocalFromInvokeGeOne.class));
    }

    @Test
    void testSilentIfStmtByteLocalFromInvokeLeOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteLocalFromInvokeLeOne.class,
                IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_LE_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtByteLocalFromInvokeLeOne.class));
    }

    @Test
    void testWarnsIfStmtByteLocalFromInvokeEqUnsignedConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteLocalFromInvokeEqUnsignedConstant.class,
                IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_EQ_UNSIGNED_CONSTANT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwIfStmtByteLocalFromInvokeEqUnsignedConstant.class));
    }

    @Test
    void testWarnsIfStmtByteLocalFromInvokeNeUnsignedConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteLocalFromInvokeNeUnsignedConstant.class,
                IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_NE_UNSIGNED_CONSTANT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwIfStmtByteLocalFromInvokeNeUnsignedConstant.class));
    }

    @Test
    void testSilentIfStmtByteLocalFromInvokeGtOutOfRangeConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteLocalFromInvokeGtOutOfRangeConstant.class,
                IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_GT_OUT_OF_RANGE_CONSTANT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtByteLocalFromInvokeGtOutOfRangeConstant.class));
    }

    @Test
    void testSilentIfStmtByteLocalFromInvokeLtUnsignedConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteLocalFromInvokeLtUnsignedConstant.class,
                IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_LT_UNSIGNED_CONSTANT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtByteLocalFromInvokeLtUnsignedConstant.class));
    }

    @Test
    void testSilentIfStmtByteLocalFromInvokeGeUnsignedConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteLocalFromInvokeGeUnsignedConstant.class,
                IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_GE_UNSIGNED_CONSTANT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtByteLocalFromInvokeGeUnsignedConstant.class));
    }

    @Test
    void testSilentIfStmtByteLocalFromInvokeLeOutOfRangeConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteLocalFromInvokeLeOutOfRangeConstant.class,
                IBW_IF_STMT_BYTE_LOCAL_FROM_INVOKE_LE_OUT_OF_RANGE_CONSTANT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtByteLocalFromInvokeLeOutOfRangeConstant.class));
    }

    @Test
    void testSilentIfStmtByteParamEqOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteParamEqOne.class,
                IBW_IF_STMT_BYTE_PARAM_EQ_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtByteParamEqOne.class));
    }

    @Test
    void testSilentIfStmtByteInvokeGtOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteInvokeGtOne.class,
                IBW_IF_STMT_BYTE_INVOKE_GT_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtByteInvokeGtOne.class));
    }

    @Test
    void testSilentIfStmtShortInvokeGtOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtShortInvokeGtOne.class,
                IBW_IF_STMT_SHORT_INVOKE_GT_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtShortInvokeGtOne.class));
    }

    @Test
    void testWarnsIfStmtByteInvokeEqUnsignedConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteInvokeEqUnsignedConstant.class,
                IBW_IF_STMT_BYTE_INVOKE_EQ_UNSIGNED_CONSTANT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwIfStmtByteInvokeEqUnsignedConstant.class));
    }

    @Test
    void testSilentIfStmtShortInvokeEqOutOfRangeConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtShortInvokeEqOutOfRangeConstant.class,
                IBW_IF_STMT_SHORT_INVOKE_EQ_OUT_OF_RANGE_CONSTANT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtShortInvokeEqOutOfRangeConstant.class));
    }

    @Test
    void testSilentIfStmtMaskedByteEqUnsignedConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtMaskedByteEqUnsignedConstant.class,
                IBW_IF_STMT_MASKED_BYTE_EQ_UNSIGNED_CONSTANT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtMaskedByteEqUnsignedConstant.class));
    }

    @Test
    void testWarnsIfStmtMasked0x0fEqUnsignedConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtMasked0x0fEqUnsignedConstant.class,
                IBW_IF_STMT_MASKED0X0F_EQ_UNSIGNED_CONSTANT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtMasked0x0fEqUnsignedConstant.class));
    }

    @Test
    void testSilentIfStmtByteAndConstantEqConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtByteAndConstantEqConstant.class,
                IBW_IF_STMT_BYTE_AND_CONSTANT_EQ_CONSTANT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwIfStmtByteAndConstantEqConstant.class));
    }

    @Test
    void testWarnsThreeImplicitSitesInOneMethod(final JcTestRunner analysis) {
        analysis.analyse(IbwManyImplicitInOneMethod.class, IBW_MANY_IMPLICIT_IN_ONE_METHOD, ruleVisitors());
        assertEquals(3, analysis.countReports(ruleId(), IbwManyImplicitInOneMethod.class));
    }

    @Test
    void testSilentMask0xff(final JcTestRunner analysis) {
        analysis.analyse(IbwMask0xffToInt.class, IBW_MASK0XFF_TO_INT, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwMask0xffToInt.class));
    }

    @Test
    void testSilentMask0xFFSpelling(final JcTestRunner analysis) {
        analysis.analyse(IbwMask0xFFCaseToInt.class, IBW_MASK0XFF_CASE_TO_INT, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwMask0xFFCaseToInt.class));
    }

    @Test
    void testSilentMaskThenArithmetic(final JcTestRunner analysis) {
        analysis.analyse(IbwMaskThenArithmeticToInt.class, IBW_MASK_THEN_ARITHMETIC_TO_INT, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwMaskThenArithmeticToInt.class));
    }

    @Test
    void testSilentMask0xffOnLeftOfByte(final JcTestRunner analysis) {
        analysis.analyse(IbwMask0xffOnLeftOfByte.class, IBW_MASK0XFF_ON_LEFT_OF_BYTE, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwMask0xffOnLeftOfByte.class));
    }

    @Test
    void testSilentMask255OnLeftOfByte(final JcTestRunner analysis) {
        analysis.analyse(IbwMask255OnLeftOfByte.class, IBW_MASK255_ON_LEFT_OF_BYTE, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwMask255OnLeftOfByte.class));
    }

    @Test
    void testSilentMaskParens0xffOnLeftOfByte(final JcTestRunner analysis) {
        analysis.analyse(IbwMaskParens0xffOnLeftOfByte.class, IBW_MASK_PARENS0XFF_ON_LEFT_OF_BYTE, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwMaskParens0xffOnLeftOfByte.class));
    }

    @Test
    @Disabled("Identical bytecode for implicit and explicit int cast from byte")
    void testSilentExplicitIntCastFromByte(final JcTestRunner analysis) {
        analysis.analyse(IbwExplicitIntCastFromByte.class, IBW_EXPLICIT_INT_CAST_FROM_BYTE, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwExplicitIntCastFromByte.class));
    }

    @Test
    void testSilentIntToLongNotByte(final JcTestRunner analysis) {
        analysis.analyse(IbwIntWidenToLongNoByte.class, IBW_INT_WIDEN_TO_LONG_NO_BYTE, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwIntWidenToLongNoByte.class));
    }

    @Test
    void testSilentUshr2ThenMask0x3f(final JcTestRunner analysis) {
        analysis.analyse(IbwUshr2ThenMask0x3f.class, IBW_USHR2_THEN_MASK0X3F, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwUshr2ThenMask0x3f.class));
    }

    @Test
    void testSilentUshr2ThenReturnMask0x3f(final JcTestRunner analysis) {
        analysis.analyse(IbwUshr2ThenReturnMask0x3f.class, IBW_USHR2_THEN_RETURN_MASK0X3F, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwUshr2ThenReturnMask0x3f.class));
    }

    @Test
    void testSilentReturnMaskedByteShl1ToInt(final JcTestRunner analysis) {
        analysis.analyse(IbwReturnMaskedByteShl1ToInt.class, IBW_RETURN_MASKED_BYTE_SHL1_TO_INT, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwReturnMaskedByteShl1ToInt.class));
    }

    @Test
    void testSilentReturnMasked0x0fAnd1Shl6(final JcTestRunner analysis) {
        analysis.analyse(
                IbwReturnMasked0x0fAnd1Shl6ToInt.class,
                IBW_RETURN_MASKED0X0F_AND1_SHL6_TO_INT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwReturnMasked0x0fAnd1Shl6ToInt.class));
    }

    @Test
    void testReturnWarnsByteUshr1WidenToInt(final JcTestRunner analysis) {
        analysis.analyse(IbwReturnByteUshr1WidenToInt.class, IBW_RETURN_BYTE_USHR1_WIDEN_TO_INT, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwReturnByteUshr1WidenToInt.class));
    }

    @Test
    void testReturnWarnsByteWidenToLong(final JcTestRunner analysis) {
        analysis.analyse(IbwReturnByteWidenToLong.class, IBW_RETURN_BYTE_WIDEN_TO_LONG, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwReturnByteWidenToLong.class));
    }

    @Test
    void testReturnWarnsByteWidenToShort(final JcTestRunner analysis) {
        analysis.analyse(IbwReturnByteWidenToShort.class, IBW_RETURN_BYTE_WIDEN_TO_SHORT, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwReturnByteWidenToShort.class));
    }

    @Test
    void testReturnWarnsArrayElementWidenToInt(final JcTestRunner analysis) {
        analysis.analyse(
                IbwReturnArrayElementWidenToInt.class, IBW_RETURN_ARRAY_ELEMENT_WIDEN_TO_INT, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwReturnArrayElementWidenToInt.class));
    }

    @Test
    void testReturnSilentMask0xffConjunctOnLeftToInt(final JcTestRunner analysis) {
        analysis.analyse(
                IbwReturnMask0xffConjunctOnLeftToInt.class,
                IBW_RETURN_MASK0XFF_CONJUNCT_LEFT_TO_INT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwReturnMask0xffConjunctOnLeftToInt.class));
    }

    @Test
    void testReturnSilentByteToByteNarrowing(final JcTestRunner analysis) {
        analysis.analyse(IbwReturnByteNoWidenToByte.class, IBW_RETURN_BYTE_NO_WIDEN_TO_BYTE, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwReturnByteNoWidenToByte.class));
    }

    @Test
    void testReturnSilentUshr1ThenReturnMask0x7f(final JcTestRunner analysis) {
        analysis.analyse(
                IbwUshr1ThenReturnMask0x7f.class, IBW_USHR1_THEN_RETURN_MASK0X7F, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwUshr1ThenReturnMask0x7f.class));
    }

    @Test
    void testReturnWarnsByteAddByteWidenToInt(final JcTestRunner analysis) {
        analysis.analyse(
                IbwReturnByteAddByteWidenToInt.class, IBW_RETURN_BYTE_ADD_BYTE_WIDEN_TO_INT, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwReturnByteAddByteWidenToInt.class));
    }

    @Test
    void testReturnWarnsByteWidenToFloat(final JcTestRunner analysis) {
        analysis.analyse(IbwReturnByteWidenToFloat.class, IBW_RETURN_BYTE_WIDEN_TO_FLOAT, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwReturnByteWidenToFloat.class));
    }

    @Test
    void testSilentUshr1ThenMask0x7f(final JcTestRunner analysis) {
        analysis.analyse(IbwUshr1ThenMask0x7f.class, IBW_USHR1_THEN_MASK0X7F, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwUshr1ThenMask0x7f.class));
    }

    @Test
    void testSilentShl2ThenMask0xfc(final JcTestRunner analysis) {
        analysis.analyse(IbwShl2ThenMask0xfc.class, IBW_SHL2_THEN_MASK0XFC, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwShl2ThenMask0xfc.class));
    }

    @Test
    void testSilentShl3ThenMask0xf8(final JcTestRunner analysis) {
        analysis.analyse(IbwShl3ThenMask0xf8.class, IBW_SHL3_THEN_MASK0XF8, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwShl3ThenMask0xf8.class));
    }

    @Test
    void testSilentShl4ThenMask0xf0(final JcTestRunner analysis) {
        analysis.analyse(IbwShl4ThenMask0xf0.class, IBW_SHL4_THEN_MASK0XF0, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwShl4ThenMask0xf0.class));
    }

    @Test
    void testSilentShl5ThenMask0xe0(final JcTestRunner analysis) {
        analysis.analyse(IbwShl5ThenMask0xe0.class, IBW_SHL5_THEN_MASK0XE0, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwShl5ThenMask0xe0.class));
    }

    @Test
    void testSilentShl6ThenMask0xc0(final JcTestRunner analysis) {
        analysis.analyse(IbwShl6ThenMask0xc0.class, IBW_SHL6_THEN_MASK0XC0, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwShl6ThenMask0xc0.class));
    }

    @Test
    void testSilentShl7ThenMask0x80(final JcTestRunner analysis) {
        analysis.analyse(IbwShl7ThenMask0x80.class, IBW_SHL7_THEN_MASK0X80, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwShl7ThenMask0x80.class));
    }

    @Test
    void testSilentShl1ThenMask0x1e0(final JcTestRunner analysis) {
        analysis.analyse(IbwShl1ThenMask0x1e0.class, IBW_SHL1_THEN_MASK0X1E0, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwShl1ThenMask0x1e0.class));
    }

    @Test
    void testSilentShl2ThenMask0x3c0(final JcTestRunner analysis) {
        analysis.analyse(IbwShl2ThenMask0x3c0.class, IBW_SHL2_THEN_MASK0X3C0, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwShl2ThenMask0x3c0.class));
    }

    @Test
    void testSilentShl3ThenMask0x780(final JcTestRunner analysis) {
        analysis.analyse(IbwShl3ThenMask0x780.class, IBW_SHL3_THEN_MASK0X780, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwShl3ThenMask0x780.class));
    }

    @Test
    void testSilentShl4ThenMask0xf00(final JcTestRunner analysis) {
        analysis.analyse(IbwShl4ThenMask0xf00.class, IBW_SHL4_THEN_MASK0XF00, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwShl4ThenMask0xf00.class));
    }

    @Test
    void testSilentShl5ThenMask0x1e00(final JcTestRunner analysis) {
        analysis.analyse(IbwShl5ThenMask0x1e00.class, IBW_SHL5_THEN_MASK0X1E00, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwShl5ThenMask0x1e00.class));
    }

    @Test
    void testSilentShl6ThenMask0x3c00(final JcTestRunner analysis) {
        analysis.analyse(IbwShl6ThenMask0x3c00.class, IBW_SHL6_THEN_MASK0X3C00, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwShl6ThenMask0x3c00.class));
    }

    @Test
    void testSilentShl7ThenMask0x7800(final JcTestRunner analysis) {
        analysis.analyse(IbwShl7ThenMask0x7800.class, IBW_SHL7_THEN_MASK0X7800, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwShl7ThenMask0x7800.class));
    }

    @Test
    void testSilentShl4ThenMask0xff(final JcTestRunner analysis) {
        analysis.analyse(IbwShl4ThenMask0xff.class, IBW_SHL4_THEN_MASK0XFF, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwShl4ThenMask0xff.class));
    }

    @Test
    void testWarnsShl3ThenMask0xfeMisaligned(final JcTestRunner analysis) {
        analysis.analyse(IbwShl3ThenMask0xfeMisaligned.class, IBW_SHL3_THEN_MASK0XFE_MISALIGNED,
                ruleVisitors());
        assertEquals(0,
                analysis.countReports(ruleId(), IbwShl3ThenMask0xfeMisaligned.class));
    }

    @Test
    void testSilentShl1ThenMask0xfe(final JcTestRunner analysis) {
        analysis.analyse(IbwShl1ThenMask0xfe.class, IBW_SHL1_THEN_MASK0XFE, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwShl1ThenMask0xfe.class));
    }

    @Test
    void testSilentUshr3After0xffThenMask0x1f(final JcTestRunner analysis) {
        analysis.analyse(IbwUshr3ByteMaskedThen0x1f.class, IBW_USHR3_BYTE_MASKED_THEN0X1F, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwUshr3ByteMaskedThen0x1f.class));
    }

    @Test
    void testSilentNarrowMaskThenShl2(final JcTestRunner analysis) {
        analysis.analyse(IbwNarrowMaskThenShl2.class, IBW_NARROW_MASK_THEN_SHL2, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwNarrowMaskThenShl2.class));
    }

    @Test
    void testSilentMask0xffThenUshr2(final JcTestRunner analysis) {
        analysis.analyse(IbwMask0xffThenUshr2.class, IBW_MASK0XFF_THEN_USHR2, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwMask0xffThenUshr2.class));
    }

    @Test
    void testWarnsMask0x100ThenShl1(final JcTestRunner analysis) {
        analysis.analyse(IbwMask0x100ThenShl1.class, IBW_MASK0X100_THEN_SHL1, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwMask0x100ThenShl1.class));
    }

    @Test
    void testWarnsMask0x1ffThenShl1(final JcTestRunner analysis) {
        analysis.analyse(IbwMask0x1ffThenShl1.class, IBW_MASK0X1FF_THEN_SHL1, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwMask0x1ffThenShl1.class));
    }

    @Test
    void testWarnsAddTwoUnmaskedBytes(final JcTestRunner analysis) {
        analysis.analyse(IbwAddTwoUnmaskedBytes.class, IBW_ADD_TWO_UNMASKED_BYTES, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwAddTwoUnmaskedBytes.class));
    }

    @Test
    void testWarnsSecondAssignIntLocalFromByte(final JcTestRunner analysis) {
        analysis.analyse(IbwSecondAssignIntLocalFromByte.class, IBW_SECOND_ASSIGN_INT_LOCAL_FROM_BYTE,
                ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwSecondAssignIntLocalFromByte.class));
    }

    @Test
    void testWarnsTernaryWidenToInt(final JcTestRunner analysis) {
        analysis.analyse(IbwTernaryWidenToInt.class, IBW_TERNARY_WIDEN_TO_INT, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwTernaryWidenToInt.class));
    }

    @Test
    void testWarnsParensAndPlusMaskedThenRaw(final JcTestRunner analysis) {
        analysis.analyse(IbwParensAndPlusMaskedThenRaw.class, IBW_PARENS_AND_PLUS_MASKED_THEN_RAW, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwParensAndPlusMaskedThenRaw.class));
    }

    @Test
    void testSilentAndPrecedencePlusLiteral(final JcTestRunner analysis) {
        analysis.analyse(IbwAndPrecedencePlusLiteral.class, IBW_AND_PRECEDENCE_PLUS_LITERAL, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwAndPrecedencePlusLiteral.class));
    }

    @Test
    void testWarnsShlByte(final JcTestRunner analysis) {
        analysis.analyse(IbwShlByte.class, IBW_SHL_BYTE, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwShlByte.class));
    }

    @Test
    void testSilentMaskOnLeftSuppressesInt(final JcTestRunner analysis) {
        analysis.analyse(IbwMaskOnLeftSuppressesInt.class, IBW_MASK_ON_LEFT_SUPPRESSES_INT, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwMaskOnLeftSuppressesInt.class));
    }

    @Test
    void testSilentLongMaskSuppressesWidening(final JcTestRunner analysis) {
        analysis.analyse(IbwLongMaskSuppressesWidening.class, IBW_LONG_MASK_SUPPRESSES_WIDENING, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwLongMaskSuppressesWidening.class));
    }

    @Test
    void testWarnsDoubleWiden(final JcTestRunner analysis) {
        analysis.analyse(IbwDoubleWiden.class, IBW_DOUBLE_WIDEN, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwDoubleWiden.class));
    }

    @Test
    void testWarnsIfStmtConstOnLeftByteOnRight(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtConstOnLeftByteOnRight.class,
                IBW_IF_STMT_CONST_ON_LEFT_BYTE_ON_RIGHT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwIfStmtConstOnLeftByteOnRight.class));
    }

    @Test
    void testWarnsIfStmtByteEqInRangeConstOneWideningOnly(final JcTestRunner analysis) {
        analysis.analyse(IbwIfStmtByteEqInRangeConst.class, IBW_IF_STMT_BYTE_EQ_IN_RANGE_CONST, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwIfStmtByteEqInRangeConst.class));
    }

    @Test
    void testWarnsCastNonConstantMask(final JcTestRunner analysis) {
        analysis.analyse(IbwCastNonConstantMask.class, IBW_CAST_NON_CONSTANT_MASK, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwCastNonConstantMask.class));
    }

    @Test
    void testSilentCastWideNibbleMask(final JcTestRunner analysis) {
        analysis.analyse(IbwCastWideNibbleMask.class, IBW_CAST_WIDE_NIBBLE_MASK, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwCastWideNibbleMask.class));
    }

    @Test
    void testWarnsAndIntAndByte(final JcTestRunner analysis) {
        analysis.analyse(IbwAndIntAndByte.class, IBW_AND_INT_AND_BYTE, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwAndIntAndByte.class));
    }

    @Test
    void testWarnsIfStmtInRangeConstOnLeft(final JcTestRunner analysis) {
        analysis.analyse(
                IbwIfStmtInRangeConstOnLeft.class, IBW_IF_STMT_IN_RANGE_CONST_ON_LEFT, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwIfStmtInRangeConstOnLeft.class));
    }

    @Test
    void testSilentIfStmtNegativeConst(final JcTestRunner analysis) {
        analysis.analyse(IbwIfStmtNegativeConst.class, IBW_IF_STMT_NEGATIVE_CONST, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwIfStmtNegativeConst.class));
    }

    @Test
    void testWarnsTwoByteComparisonOneWidening(final JcTestRunner analysis) {
        analysis.analyse(IbwTwoByteComparison.class, IBW_TWO_BYTE_COMPARISON, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwTwoByteComparison.class));
    }

    @Test
    void testSilentLongMaskSmall(final JcTestRunner analysis) {
        analysis.analyse(IbwLongMaskSmall.class, IBW_LONG_MASK_SMALL, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwLongMaskSmall.class));
    }

    @Test
    void testWarnsLongMaskNegative(final JcTestRunner analysis) {
        analysis.analyse(IbwLongMaskNegative.class, IBW_LONG_MASK_NEGATIVE, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwLongMaskNegative.class));
    }

    @Test
    void testWarnsLongMaskTooBig(final JcTestRunner analysis) {
        analysis.analyse(IbwLongMaskTooBig.class, IBW_LONG_MASK_TOO_BIG, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwLongMaskTooBig.class));
    }

    @Test
    void testKnownFalseNegativeByteAnd0xff00ToInt(final JcTestRunner analysis) {
        analysis.analyse(IbwFnByteAnd0xff00ToInt.class, IBW_FN_BYTE_AND0XFF00_TO_INT, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwFnByteAnd0xff00ToInt.class));
    }

    @Test
    void testKnownFalseNegativeByteAnd0xff0000ToInt(final JcTestRunner analysis) {
        analysis.analyse(IbwFnByteAnd0xff0000ToInt.class, IBW_FN_BYTE_AND0XFF0000_TO_INT, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwFnByteAnd0xff0000ToInt.class));
    }

    @Test
    void testKnownFalseNegativeArrayElementAnd0xff00ToInt(final JcTestRunner analysis) {
        analysis.analyse(IbwFnArrayElementAnd0xff00ToInt.class, IBW_FN_ARRAY_ELEMENT_AND0XFF00_TO_INT,
                ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwFnArrayElementAnd0xff00ToInt.class));
    }

    @Test
    void testKnownFalseNegativeByteAnd0xf00ToInt(final JcTestRunner analysis) {
        analysis.analyse(IbwFnByteAnd0xf00ToInt.class, IBW_FN_BYTE_AND0XF00_TO_INT, ruleVisitors());
        assertEquals(1, analysis.countReports(ruleId(), IbwFnByteAnd0xf00ToInt.class));
    }

    @Test
    void testKnownFalsePositiveMaskedByteShl8ToInt(final JcTestRunner analysis) {
        analysis.analyse(IbwFpMaskedByteShl8ToInt.class, IBW_FP_MASKED_BYTE_SHL8_TO_INT, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwFpMaskedByteShl8ToInt.class));
    }

    @Test
    void testKnownFalsePositiveMaskedArrayElementShl16ToInt(final JcTestRunner analysis) {
        analysis.analyse(IbwFpMaskedArrayElementShl16ToInt.class, IBW_FP_MASKED_ARRAY_ELEMENT_SHL16_TO_INT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(),
                IbwFpMaskedArrayElementShl16ToInt.class));
    }

    @Test
    void testKnownFalsePositiveSwitchStmtIntKeyMaskedByteEqUnsignedConstant(final JcTestRunner analysis) {
        analysis.analyse(
                IbwFpSwitchStmtIntKeyMaskedByteEqUnsignedConstant.class,
                IBW_FP_SWITCH_STMT_INT_KEY_MASKED_BYTE_EQ_UNSIGNED_CONSTANT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwFpSwitchStmtIntKeyMaskedByteEqUnsignedConstant.class));
    }

    @Test
    void testKnownFalsePositiveSwitchStmtIntKeyFromByteEqOne(final JcTestRunner analysis) {
        analysis.analyse(
                IbwFpSwitchStmtIntKeyFromByteEqOne.class,
                IBW_FP_SWITCH_STMT_INT_KEY_FROM_BYTE_EQ_ONE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwFpSwitchStmtIntKeyFromByteEqOne.class));
    }

    @Test
    void testKnownFalsePositiveSwitchStmtIntKeyFromByteEqSignedCase(final JcTestRunner analysis) {
        analysis.analyse(
                IbwFpSwitchStmtIntKeyFromByteEqSignedCase.class,
                IBW_FP_SWITCH_STMT_INT_KEY_FROM_BYTE_EQ_SIGNED_CASE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwFpSwitchStmtIntKeyFromByteEqSignedCase.class));
    }

    @Test
    void testWarnsAssignBooleanInvokeFromByteParam(final JcTestRunner analysis) {
        analysis.analyse(
                IbwFpAssignBooleanInvokeFromByte.class,
                IBW_FP_ASSIGN_BOOLEAN_INVOKE_FROM_BYTE,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwFpAssignBooleanInvokeFromByte.class));
    }

    @Test
    void testWarnsIfStmtBooleanInvokeFromWidenedByteParam(final JcTestRunner analysis) {
        analysis.analyse(
                IbwFpIfStmtBooleanInvokeFromWidenedByte.class,
                IBW_FP_IF_STMT_BOOLEAN_INVOKE_FROM_WIDENED_BYTE,
                ruleVisitors());
        assertEquals(1, analysis.countReports(
                ruleId(), IbwFpIfStmtBooleanInvokeFromWidenedByte.class));
    }

    @Test
    void testKnownFalsePositiveReturnByteEqOneCompareOnly(final JcTestRunner analysis) {
        analysis.analyse(
                IbwFpReturnByteEqOneCompareOnly.class,
                IBW_FP_RETURN_BYTE_EQ_ONE_COMPARE_ONLY,
                ruleVisitors());
        assertEquals(0, analysis.countReports(
                ruleId(), IbwFpReturnByteEqOneCompareOnly.class));
    }

    @Test
    void testAssignByte(final JcTestRunner analysis) {
        analysis.analyse(IbwAssignByte.class, IBW_ASSIGN_BYTE,
                ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(),
                IbwAssignByte.class));
    }

    @Test
    void testByteArrayOrCastStore(final JcTestRunner analysis) {
        analysis.analyse(IbwByteArrayOrCastStore.class, IBW_BYTE_ARRAY_OR_CAST_STORE, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwByteArrayOrCastStore.class));
    }

    @Test
    void testByteArrayOrEqMaskedCast(final JcTestRunner analysis) {
        analysis.analyse(IbwByteArrayOrEqMaskedCast.class, IBW_BYTE_ARRAY_OR_EQ_MASKED_CAST, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwByteArrayOrEqMaskedCast.class));
    }

    @Test
    void testReturnShort(final JcTestRunner analysis) {
        analysis.analyse(IbwReturnShort.class, IBW_RETURN_SHORT, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwReturnShort.class));
    }

    @Test
    void testSilentReturnNibbleMaskedByte(final JcTestRunner analysis) {
        analysis.analyse(
                IbwReturnNibbleMaskedByteToInt.class,
                IBW_RETURN_NIBBLE_MASKED_BYTE_TO_INT,
                ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwReturnNibbleMaskedByteToInt.class));
    }

    @Test
    void testShortCastIntDivEight(final JcTestRunner analysis) {
        analysis.analyse(IbwShortCastIntDivEight.class, IBW_SHORT_CAST_INT_DIV_EIGHT, ruleVisitors());
        assertEquals(0, analysis.countReports(ruleId(), IbwShortCastIntDivEight.class));
    }
}
