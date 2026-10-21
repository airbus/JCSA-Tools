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
import com.airbus.esar.fixture.recordlike.RlAbstractGrab;
import com.airbus.esar.fixture.recordlike.RlAccessorsPlusBusinessMethod;
import com.airbus.esar.fixture.recordlike.RlAccessorsPlusStaticMethod;
import com.airbus.esar.fixture.recordlike.RlClinitFieldAssign;
import com.airbus.esar.fixture.recordlike.RlFluentSetterReturnsThis;
import com.airbus.esar.fixture.recordlike.RlGetterChainedTempLocals;
import com.airbus.esar.fixture.recordlike.RlGetterFieldStoreThenReturn;
import com.airbus.esar.fixture.recordlike.RlGetterReturnWithSideEffect;
import com.airbus.esar.fixture.recordlike.RlGetterRhsConstantNotField;
import com.airbus.esar.fixture.recordlike.RlGetterVoidInvokeThenReturnConst;
import com.airbus.esar.fixture.recordlike.RlGetterWithExtraParam;
import com.airbus.esar.fixture.recordlike.RlInstanceVoidNoop;
import com.airbus.esar.fixture.recordlike.RlIntFieldGetVSetV;
import com.airbus.esar.fixture.recordlike.RlNoInstanceMethodsDeclared;
import com.airbus.esar.fixture.recordlike.RlOnlyAccessorsBooleanIs;
import com.airbus.esar.fixture.recordlike.RlOnlyAccessorsByteField;
import com.airbus.esar.fixture.recordlike.RlOnlyAccessorsIntField;
import com.airbus.esar.fixture.recordlike.RlOnlyNonAccessorInstanceMethods;
import com.airbus.esar.fixture.recordlike.RlSetterAssignsConstant;
import com.airbus.esar.fixture.recordlike.RlSetterTwoParams;
import com.airbus.esar.fixture.recordlike.RlSetterVoidInvokeThenReturn;
import com.airbus.esar.fixture.recordlike.RlStaticGetterInt;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

/** {@link JcRecordLikeClass} vs {@code fixture/recordlike} classes. */
@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcRecordLikeClass {

    private static final List<ProgramVisitorIf> RECORD_LIKE_ONLY = List.of(new JcRecordLikeClass());

    private static final List<MethodSignature> NO_ENTRY = List.of();

    @Test
    void testWarnsOnlyAccessorsIntField(final JcTestRunner analysis) {
        analysis.analyse(RlOnlyAccessorsIntField.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(1, analysis.countReports(JcRecordLikeClass.RULE_ID, RlOnlyAccessorsIntField.class));
    }

    @Test
    void testWarnsOnlyAccessorsBooleanIs(final JcTestRunner analysis) {
        analysis.analyse(RlOnlyAccessorsBooleanIs.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(1, analysis.countReports(JcRecordLikeClass.RULE_ID, RlOnlyAccessorsBooleanIs.class));
    }

    @Test
    void testWarnsOnlyAccessorsByteField(final JcTestRunner analysis) {
        analysis.analyse(RlOnlyAccessorsByteField.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(1, analysis.countReports(JcRecordLikeClass.RULE_ID, RlOnlyAccessorsByteField.class));
    }

    @Test
    void testSilentAccessorsPlusBusinessMethod(final JcTestRunner analysis) {
        analysis.analyse(RlAccessorsPlusBusinessMethod.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlAccessorsPlusBusinessMethod.class));
    }

    @Test
    void testSilentAccessorsPlusStaticMethod(final JcTestRunner analysis) {
        analysis.analyse(RlAccessorsPlusStaticMethod.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlAccessorsPlusStaticMethod.class));
    }

    @Test
    void testSilentOnlyNonAccessorInstanceMethods(final JcTestRunner analysis) {
        analysis.analyse(RlOnlyNonAccessorInstanceMethods.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlOnlyNonAccessorInstanceMethods.class));
    }

    @Test
    void testSilentNoInstanceMethodsDeclared(final JcTestRunner analysis) {
        analysis.analyse(RlNoInstanceMethodsDeclared.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlNoInstanceMethodsDeclared.class));
    }

    @Test
    void testSilentFluentSetterReturnsThis(final JcTestRunner analysis) {
        analysis.analyse(RlFluentSetterReturnsThis.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlFluentSetterReturnsThis.class));
    }

    @Test
    void testWarnsIntFieldGetVSetV(final JcTestRunner analysis) {
        analysis.analyse(RlIntFieldGetVSetV.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(1, analysis.countReports(JcRecordLikeClass.RULE_ID, RlIntFieldGetVSetV.class));
    }

    @Test
    void testSilentOnlyStaticAccessor(final JcTestRunner analysis) {
        analysis.analyse(RlStaticGetterInt.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlStaticGetterInt.class));
    }

    @Test
    void testSilentNamedGetterWithParameter(final JcTestRunner analysis) {
        analysis.analyse(RlGetterWithExtraParam.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlGetterWithExtraParam.class));
    }

    @Test
    void testSilentInstanceVoidNoopOnly(final JcTestRunner analysis) {
        analysis.analyse(RlInstanceVoidNoop.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlInstanceVoidNoop.class));
    }

    @Test
    void testSilentAbstractGrabOnly(final JcTestRunner analysis) {
        analysis.analyse(RlAbstractGrab.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlAbstractGrab.class));
    }

    @Test
    void testSilentClinitAndCtorOnly(final JcTestRunner analysis) {
        analysis.analyse(RlClinitFieldAssign.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlClinitFieldAssign.class));
    }

    @Test
    void testSilentGetterBodyNotTrivialFieldRead(final JcTestRunner analysis) {
        analysis.analyse(RlGetterChainedTempLocals.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlGetterChainedTempLocals.class));
    }

    @Test
    void testSilentGetterBodyRhsNotFieldRead(final JcTestRunner analysis) {
        analysis.analyse(RlGetterRhsConstantNotField.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlGetterRhsConstantNotField.class));
    }

    @Test
    void testSilentGetterBodyLeadingInvoke(final JcTestRunner analysis) {
        analysis.analyse(RlGetterVoidInvokeThenReturnConst.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlGetterVoidInvokeThenReturnConst.class));
    }

    @Test
    void testSilentGetterBodyFieldStoreBeforeReturn(final JcTestRunner analysis) {
        analysis.analyse(RlGetterFieldStoreThenReturn.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlGetterFieldStoreThenReturn.class));
    }

    @Test
    void testSilentGetterBodyWritesField(final JcTestRunner analysis) {
        analysis.analyse(RlGetterReturnWithSideEffect.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlGetterReturnWithSideEffect.class));
    }

    @Test
    void testSilentSetterBodyDoesNotStoreParameter(final JcTestRunner analysis) {
        analysis.analyse(RlSetterAssignsConstant.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlSetterAssignsConstant.class));
    }

    @Test
    void testSilentSetterBodyLeadingInvoke(final JcTestRunner analysis) {
        analysis.analyse(RlSetterVoidInvokeThenReturn.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlSetterVoidInvokeThenReturn.class));
    }

    @Test
    void testSilentSetterTwoParameters(final JcTestRunner analysis) {
        analysis.analyse(RlSetterTwoParams.class, NO_ENTRY, RECORD_LIKE_ONLY);
        assertEquals(0, analysis.countReports(JcRecordLikeClass.RULE_ID, RlSetterTwoParams.class));
    }
}
