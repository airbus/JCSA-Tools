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

import com.airbus.esar.fixture.oneshot.BadOneShotCloseAfterTryNoFinally;
import com.airbus.esar.fixture.oneshot.BadOneShotCloseOutsideFinally;
import com.airbus.esar.fixture.oneshot.BadOneShotCloseBeforeTry;
import com.airbus.esar.fixture.oneshot.BadOneShotCloseInsideTry;
import com.airbus.esar.fixture.oneshot.BadOneShotMissingCloseAllApiClasses;
import com.airbus.esar.fixture.oneshot.BadOneShotOpenOutsideTryAllApiClasses;
import com.airbus.esar.fixture.oneshot.BadOneShotOpenOutsideTryCloseInFinally;
import com.airbus.esar.fixture.oneshot.OkOneShotAllApiClasses;
import com.airbus.esar.fixture.oneshot.OkOneShotCipherWithCatch;
import com.airbus.esar.fixture.oneshot.OkOneShotReturnInFinallyIf;
import com.airbus.esar.fixture.oneshot.OkOneShotReturnValueInFinallyIf;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcOneShotUsage {

    private static final List<ProgramVisitorIf> ONE_SHOT_ONLY = List.of(new JcOneShotUsage());

    @Test
    void testOkRandomDataOneShot(final JcTestRunner analysis) {
        assertOk(analysis, OkOneShotAllApiClasses.class, "okRandomData");
    }

    @Test
    void testOkMessageDigestOneShot(final JcTestRunner analysis) {
        assertOk(analysis, OkOneShotAllApiClasses.class, "okMessageDigest");
    }

    @Test
    void testOkInitializedMessageDigestOneShot(final JcTestRunner analysis) {
        assertOk(analysis, OkOneShotAllApiClasses.class, "okInitializedMessageDigest");
    }

    @Test
    void testOkSignatureOneShot(final JcTestRunner analysis) {
        assertOk(analysis, OkOneShotAllApiClasses.class, "okSignature");
    }

    @Test
    void testOkCipherOneShot(final JcTestRunner analysis) {
        assertOk(analysis, OkOneShotAllApiClasses.class, "okCipher");
    }

    @Test
    void testOkDerivationFunctionOneShot(final JcTestRunner analysis) {
        assertOk(analysis, OkOneShotAllApiClasses.class, "okDerivationFunction");
    }

    @Test
    void testOkCipherWithCatch(final JcTestRunner analysis) {
        assertOk(analysis, OkOneShotCipherWithCatch.class, "entry");
    }

    @Test
    void testOkReturnInFinallyIf(final JcTestRunner analysis) {
        assertOk(analysis, OkOneShotReturnInFinallyIf.class, "entry");
    }

    @Test
    void testOkReturnValueInFinallyIf(final JcTestRunner analysis) {
        analysis.analyse(
                OkOneShotReturnValueInFinallyIf.class,
                List.of(JcTestRunner.sig(OkOneShotReturnValueInFinallyIf.class, "entry", "byte", List.of())),
                ONE_SHOT_ONLY);
        assertEquals(0, analysis.countReports(JcOneShotUsage.RULE_ID, OkOneShotReturnValueInFinallyIf.class));
    }

    @Test
    void testBadOpenOutsideTryCloseInFinally(final JcTestRunner analysis) {
        assertNok(analysis, BadOneShotOpenOutsideTryCloseInFinally.class, "entry");
    }

    @Test
    void testBadRandomDataOpenOutsideTry(final JcTestRunner analysis) {
        assertNok(analysis, BadOneShotOpenOutsideTryAllApiClasses.class, "badRandomDataOpenOutsideTry");
    }

    @Test
    void testBadMessageDigestOpenOutsideTry(final JcTestRunner analysis) {
        assertNok(analysis, BadOneShotOpenOutsideTryAllApiClasses.class, "badMessageDigestOpenOutsideTry");
    }

    @Test
    void testBadInitializedMessageDigestOpenOutsideTry(final JcTestRunner analysis) {
        assertNok(analysis, BadOneShotOpenOutsideTryAllApiClasses.class, "badInitializedMessageDigestOpenOutsideTry");
    }

    @Test
    void testBadSignatureOpenOutsideTry(final JcTestRunner analysis) {
        assertNok(analysis, BadOneShotOpenOutsideTryAllApiClasses.class, "badSignatureOpenOutsideTry");
    }

    @Test
    void testBadCipherOpenOutsideTry(final JcTestRunner analysis) {
        assertNok(analysis, BadOneShotOpenOutsideTryAllApiClasses.class, "badCipherOpenOutsideTry");
    }

    @Test
    void testBadDerivationFunctionOpenOutsideTry(final JcTestRunner analysis) {
        assertNok(analysis, BadOneShotOpenOutsideTryAllApiClasses.class, "badDerivationFunctionOpenOutsideTry");
    }

    @Test
    void testBadRandomDataMissingClose(final JcTestRunner analysis) {
        assertNok(analysis, BadOneShotMissingCloseAllApiClasses.class, "badRandomDataMissingClose");
    }

    @Test
    void testBadMessageDigestMissingClose(final JcTestRunner analysis) {
        assertNok(analysis, BadOneShotMissingCloseAllApiClasses.class, "badMessageDigestMissingClose");
    }

    @Test
    void testBadInitializedMessageDigestMissingClose(final JcTestRunner analysis) {
        assertNok(analysis, BadOneShotMissingCloseAllApiClasses.class, "badInitializedMessageDigestMissingClose");
    }

    @Test
    void testBadSignatureMissingClose(final JcTestRunner analysis) {
        assertNok(analysis, BadOneShotMissingCloseAllApiClasses.class, "badSignatureMissingClose");
    }

    @Test
    void testBadCipherMissingClose(final JcTestRunner analysis) {
        assertNok(analysis, BadOneShotMissingCloseAllApiClasses.class, "badCipherMissingClose");
    }

    @Test
    void testBadDerivationFunctionMissingClose(final JcTestRunner analysis) {
        assertNok(analysis, BadOneShotMissingCloseAllApiClasses.class, "badDerivationFunctionMissingClose");
    }

    @Test
    void testBadCloseInsideTry(final JcTestRunner analysis) {
        assertNok(analysis, BadOneShotCloseInsideTry.class, "entry");
    }

    @Test
    void testBadCloseAfterTryWithoutFinally(final JcTestRunner analysis) {
        assertNok(analysis, BadOneShotCloseAfterTryNoFinally.class, "entry");
    }

    @Test
    void testBadCloseOutsideFinally(final JcTestRunner analysis) {
        assertNok(analysis, BadOneShotCloseOutsideFinally.class, "entry");
    }

    @Test
    void testBadCloseBeforeTryDoesNotExcuseMissingFinallyClose(final JcTestRunner analysis) {
        assertNok(analysis, BadOneShotCloseBeforeTry.class, "entry");
    }

    private static void assertOk(final JcTestRunner analysis, final Class<?> fixture, final String method) {
        analysis.analyse(fixture, JcTestRunner.sigVoidNoArg(fixture, method), ONE_SHOT_ONLY);
        assertEquals(0, analysis.countReports(JcOneShotUsage.RULE_ID, fixture));
    }

    private static void assertNok(final JcTestRunner analysis, final Class<?> fixture, final String method) {
        analysis.analyse(fixture, JcTestRunner.sigVoidNoArg(fixture, method), ONE_SHOT_ONLY);
        assertEquals(1, analysis.countReports(JcOneShotUsage.RULE_ID, fixture));
    }
}
