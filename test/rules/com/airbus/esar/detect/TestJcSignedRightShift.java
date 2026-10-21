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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import sootup.core.signatures.MethodSignature;
import com.airbus.esar.fixture.bitshift.LeftShiftOnly;
import com.airbus.esar.fixture.bitshift.MixedUnsignedAndSignedInt;
import com.airbus.esar.fixture.bitshift.NoBitShift;
import com.airbus.esar.fixture.bitshift.SignedRightShiftByteArrayNibble;
import com.airbus.esar.fixture.bitshift.SignedRightShiftInt;
import com.airbus.esar.fixture.bitshift.SignedRightShiftLong;
import com.airbus.esar.fixture.bitshift.SignedRightShiftTwice;
import com.airbus.esar.fixture.bitshift.UnsignedRightShiftInt;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcSignedRightShift {

    private static final List<MethodSignature> ENTRY_VOID_INT_SIGNED_RIGHT_SHIFT_INT = List.of(
            JcTestRunner.sig(SignedRightShiftInt.class, "entry", "void", List.of("int")));

    private static final List<MethodSignature> ENTRY_VOID_LONG_SIGNED_RIGHT_SHIFT_LONG = List.of(
            JcTestRunner.sig(SignedRightShiftLong.class, "entry", "void", List.of("long")));

    private static final List<MethodSignature> ENTRY_VOID_INT_INT_SIGNED_RIGHT_SHIFT_TWICE = List.of(
            JcTestRunner.sig(SignedRightShiftTwice.class, "entry", "void", List.of("int", "int")));

    private static final List<MethodSignature> ENTRY_VOID_INT_UNSIGNED_RIGHT_SHIFT_INT = List.of(
            JcTestRunner.sig(UnsignedRightShiftInt.class, "entry", "void", List.of("int")));

    private static final List<MethodSignature> ENTRY_VOID_INT_LEFT_SHIFT_ONLY = List.of(
            JcTestRunner.sig(LeftShiftOnly.class, "entry", "void", List.of("int")));

    private static final List<MethodSignature> ENTRY_VOID_INT_NO_BIT_SHIFT = List.of(
            JcTestRunner.sig(NoBitShift.class, "entry", "void", List.of("int")));

    private static final List<MethodSignature> ENTRY_VOID_INT_INT_MIXED_UNSIGNED_AND_SIGNED = List.of(
            JcTestRunner.sig(MixedUnsignedAndSignedInt.class, "entry", "void", List.of("int", "int")));

    private static final List<MethodSignature> ENTRY_BYTE_ARRAY_NIBBLE = List.of(
            JcTestRunner.sig(SignedRightShiftByteArrayNibble.class, "entry", "void",
                    List.of("byte[]", "short", "byte[]")));

    @Test
    void testWarnsSignedIntRightShift(final JcTestRunner analysis) {
        analysis.analyse(SignedRightShiftInt.class, ENTRY_VOID_INT_SIGNED_RIGHT_SHIFT_INT);
        assertEquals(1, analysis.countReports(JcSignedRightShift.RULE_ID, SignedRightShiftInt.class));
    }

    @Test
    void testWarnsSignedLongRightShift(final JcTestRunner analysis) {
        analysis.analyse(SignedRightShiftLong.class, ENTRY_VOID_LONG_SIGNED_RIGHT_SHIFT_LONG);
        assertEquals(1, analysis.countReports(JcSignedRightShift.RULE_ID, SignedRightShiftLong.class));
    }

    @Test
    void testWarnsEachSignedShiftInMethod(final JcTestRunner analysis) {
        analysis.analyse(SignedRightShiftTwice.class, ENTRY_VOID_INT_INT_SIGNED_RIGHT_SHIFT_TWICE);
        assertEquals(2, analysis.countReports(JcSignedRightShift.RULE_ID, SignedRightShiftTwice.class));
    }

    @Test
    void testSilentUnsignedIntRightShift(final JcTestRunner analysis) {
        analysis.analyse(UnsignedRightShiftInt.class, ENTRY_VOID_INT_UNSIGNED_RIGHT_SHIFT_INT);
        assertEquals(0, analysis.countReports(JcSignedRightShift.RULE_ID, UnsignedRightShiftInt.class));
    }

    @Test
    void testSilentLeftShiftOnly(final JcTestRunner analysis) {
        analysis.analyse(LeftShiftOnly.class, ENTRY_VOID_INT_LEFT_SHIFT_ONLY);
        assertEquals(0, analysis.countReports(JcSignedRightShift.RULE_ID, LeftShiftOnly.class));
    }

    @Test
    void testSilentNoBitShift(final JcTestRunner analysis) {
        analysis.analyse(NoBitShift.class, ENTRY_VOID_INT_NO_BIT_SHIFT);
        assertEquals(0, analysis.countReports(JcSignedRightShift.RULE_ID, NoBitShift.class));
    }

    @Test
    void testWarnsOnlySignedInMixedUnsignedAndSigned(final JcTestRunner analysis) {
        analysis.analyse(MixedUnsignedAndSignedInt.class, ENTRY_VOID_INT_INT_MIXED_UNSIGNED_AND_SIGNED);
        assertEquals(1, analysis.countReports(JcSignedRightShift.RULE_ID, MixedUnsignedAndSignedInt.class));
    }

    @Test
    void testWarnsSignedRightShiftOnByteArrayNibbles(final JcTestRunner analysis) {
        analysis.analyse(SignedRightShiftByteArrayNibble.class, ENTRY_BYTE_ARRAY_NIBBLE);
        assertEquals(4, analysis.countReports(
                JcSignedRightShift.RULE_ID, SignedRightShiftByteArrayNibble.class));
    }
}
