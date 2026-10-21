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

import com.airbus.esar.fixture.knownarraysize.KnownArrayAllocFinalLenBranchInit;
import com.airbus.esar.fixture.knownarraysize.KnownArrayAllocFinalLenBranchStaticFinal;
import com.airbus.esar.fixture.knownarraysize.KnownArrayAllocFinalLenNestedBranchInit;
import com.airbus.esar.fixture.knownarraysize.KnownArrayAllocFinalLenTernaryInit;
import com.airbus.esar.fixture.knownarraysize.KnownArrayAllocBranchBothConstant;
import com.airbus.esar.fixture.knownarraysize.KnownArrayAllocBranchIfElseBothConstant;
import com.airbus.esar.fixture.knownarraysize.KnownArrayAllocStaticFinalLen;
import com.airbus.esar.fixture.knownarraysize.KnownArrayNewLocal;
import com.airbus.esar.fixture.knownarraysize.UnknownArrayAllocBranchBothDynamic;
import com.airbus.esar.fixture.knownarraysize.UnknownArrayAllocBranchMixed;
import com.airbus.esar.fixture.knownarraysize.UnknownArrayAllocFinalLenBranchMixed;
import com.airbus.esar.fixture.knownarraysize.UnknownArrayAllocFinalLenBranchParamOnIf;
import com.airbus.esar.fixture.knownarraysize.UnknownArrayAllocFromParam;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcUnknownArraySize {

    @Test
    void testSilentAllocConstant(final JcTestRunner analysis) {
        assertReports(analysis, KnownArrayNewLocal.class, "entry", List.of(), 0);
    }

    @Test
    void testSilentAllocStaticFinalLen(final JcTestRunner analysis) {
        assertReports(analysis, KnownArrayAllocStaticFinalLen.class, "entry", List.of(), 0);
    }

    @Test
    void testWarnsAllocFromParam(final JcTestRunner analysis) {
        assertReports(analysis, UnknownArrayAllocFromParam.class, "entry", List.of("byte"), 1);
    }

    @Test
    void testSilentAllocBranchBothConstant(final JcTestRunner analysis) {
        assertReports(analysis, KnownArrayAllocBranchBothConstant.class, "entry", List.of("boolean"), 0);
    }

    @Test
    void testSilentAllocBranchIfElseBothConstant(final JcTestRunner analysis) {
        assertReports(analysis, KnownArrayAllocBranchIfElseBothConstant.class, "entry", List.of("boolean"), 0);
    }

    @Test
    void testSilentAllocFinalLenBranchInit(final JcTestRunner analysis) {
        assertReports(analysis, KnownArrayAllocFinalLenBranchInit.class, "entry", List.of("boolean"), 0);
    }

    @Test
    void testSilentAllocFinalLenTernaryInit(final JcTestRunner analysis) {
        assertReports(analysis, KnownArrayAllocFinalLenTernaryInit.class, "entry", List.of("boolean"), 0);
    }

    @Test
    void testSilentAllocFinalLenBranchStaticFinal(final JcTestRunner analysis) {
        assertReports(analysis, KnownArrayAllocFinalLenBranchStaticFinal.class, "entry", List.of("boolean"), 0);
    }

    @Test
    void testSilentAllocFinalLenNestedBranchInit(final JcTestRunner analysis) {
        assertReports(
                analysis, KnownArrayAllocFinalLenNestedBranchInit.class, "entry", List.of("boolean", "boolean"), 0);
    }

    @Test
    void testWarnsAllocFinalLenBranchMixed(final JcTestRunner analysis) {
        assertReports(analysis, UnknownArrayAllocFinalLenBranchMixed.class, "entry", List.of("boolean", "byte"), 1);
    }

    @Test
    void testWarnsAllocFinalLenBranchParamOnIf(final JcTestRunner analysis) {
        assertReports(analysis, UnknownArrayAllocFinalLenBranchParamOnIf.class, "entry", List.of("boolean", "byte"), 1);
    }

    @Test
    void testWarnsAllocBranchMixed(final JcTestRunner analysis) {
        assertReports(analysis, UnknownArrayAllocBranchMixed.class, "entry", List.of("boolean", "byte"), 1);
    }

    @Test
    void testWarnsAllocBranchBothDynamic(final JcTestRunner analysis) {
        assertReports(
                analysis, UnknownArrayAllocBranchBothDynamic.class, "entry", List.of("boolean", "byte", "byte"), 2);
    }

    private static void assertReports(final JcTestRunner analysis,
                                    final Class<?> fixture,
                                    final String methodName,
                                    final List<String> paramTypes,
                                    final int expected) {
        analysis.analyse(fixture, List.of(JcTestRunner.sig(fixture, methodName, "void", paramTypes)));
        assertEquals(expected, analysis.countReports(JcUnknownArraySize.RULE_ID, fixture));
    }
}
