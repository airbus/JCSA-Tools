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

import com.airbus.esar.fixture.replybusy.SilentNoIsoException;
import com.airbus.esar.fixture.replybusy.SilentReplyBusyForwardedConstant;
import com.airbus.esar.fixture.replybusy.SilentReplyBusyOnlyInUnreachableCallee;
import com.airbus.esar.fixture.replybusy.ThrowsOtherStatusWord;
import com.airbus.esar.fixture.replybusy.ThrowsReplyBusyAssignedLocal;
import com.airbus.esar.fixture.replybusy.ThrowsReplyBusyCopiedLocal;
import com.airbus.esar.fixture.replybusy.ThrowsReplyBusyFromParam;
import com.airbus.esar.fixture.replybusy.ThrowsReplyBusyInCallee;
import com.airbus.esar.fixture.replybusy.ThrowsReplyBusyLiteral;
import com.airbus.esar.fixture.replybusy.ThrowsReplyBusyMinus27904Form;
import com.airbus.esar.fixture.replybusy.ThrowsReplyBusyTwoThrowsOneBusy;
import com.airbus.esar.fixture.replybusy.ThrowsReplyBusyViaMethod;
import com.airbus.esar.fixture.replybusy.ThrowsReplyBusyViaNestedMethod;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
@Disabled
class TestJcIsoExceptionReplyBusy {

    @Test
    void testWarnsReplyBusyLiteral(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsReplyBusyLiteral.class, List.of(), 1);
    }

    @Test
    void testWarnsReplyBusyMinus27904Form(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsReplyBusyMinus27904Form.class, List.of(), 1);
    }

    @Test
    void testWarnsReplyBusyAssignedLocal(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsReplyBusyAssignedLocal.class, List.of(), 1);
    }

    @Test
    void testWarnsReplyBusyCopiedLocal(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsReplyBusyCopiedLocal.class, List.of(), 1);
    }

    @Test
    void testWarnsReplyBusyViaMethod(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsReplyBusyViaMethod.class, List.of(), 1);
    }

    @Test
    void testWarnsReplyBusyViaNestedMethod(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsReplyBusyViaNestedMethod.class, List.of(), 1);
    }

    @Test
    void testWarnsReplyBusyInCallee(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsReplyBusyInCallee.class, List.of(), 1);
    }

    @Test
    void testSilentReplyBusyForwardedConstant(final JcTestRunner analysis) {
        assertReports(analysis, SilentReplyBusyForwardedConstant.class, List.of(), 0);
    }

    @Test
    void testWarnsReplyBusyTwoThrowsOneBusy(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsReplyBusyTwoThrowsOneBusy.class, List.of(), 1);
    }

    @Test
    void testSilentOtherStatusWord(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsOtherStatusWord.class, List.of(), 0);
    }

    @Test
    void testSilentReplyBusyFromParam(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsReplyBusyFromParam.class, List.of("short"), 0);
    }

    @Test
    void testSilentNoIsoException(final JcTestRunner analysis) {
        assertReports(analysis, SilentNoIsoException.class, List.of(), 0);
    }

    @Test
    void testSilentReplyBusyOnlyInUnreachableCallee(final JcTestRunner analysis) {
        assertReports(analysis, SilentReplyBusyOnlyInUnreachableCallee.class, List.of(), 0);
    }

    private static void assertReports(final JcTestRunner analysis,
                                      final Class<?> fixture,
                                      final List<String> paramTypes,
                                      final int expected) {
        analysis.analyse(fixture, List.of(JcTestRunner.sig(fixture, "entry", "void", paramTypes)));
        assertEquals(expected, analysis.countReports(JcIsoExceptionReplyBusy.RULE_ID, fixture));
    }
}
