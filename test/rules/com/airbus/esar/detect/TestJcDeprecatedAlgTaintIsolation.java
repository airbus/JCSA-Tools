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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.airbus.esar.fixture.alg.DeprecatedAlgTaintSharedByteDomain;
import com.airbus.esar.fixture.alg.OkDeprecatedAlgTaintIsolation;
import com.airbus.esar.fixture.cipher.DeprecatedCipherAes192Ecb;
import com.airbus.esar.fixture.cipher.OkCipherOneShotSm4Ecb;
import com.airbus.esar.fixture.randomdata.DeprecatedRandomDataOneShot;
import com.airbus.esar.fixture.signature.DeprecatedSignatureMacAlgorithms;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

/** Shared {@code JC_DEPRECATED_ALG_TAINT} IFDS: each rule must only report its own sink domain. */
@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcDeprecatedAlgTaintIsolation {

    @Test
    void testDeprecatedCipherAes192OnlyCipherRuleReports(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedCipherAes192Ecb.class,
                JcTestRunner.sigVoidNoArg(DeprecatedCipherAes192Ecb.class, "entryAes192Ecb"));
        assertEquals(1,
                analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, DeprecatedCipherAes192Ecb.class));
        assertEquals(0,
                analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, DeprecatedCipherAes192Ecb.class));
        assertEquals(0,
                analysis.countReports(JcDeprecatedSignatureAlg.RULE_ID, DeprecatedCipherAes192Ecb.class));
    }

    @Test
    void testDeprecatedSignatureMac192OnlySignatureRuleReports(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedSignatureMacAlgorithms.class,
                JcTestRunner.sigVoidNoArg(DeprecatedSignatureMacAlgorithms.class, "entryMac192"));
        assertEquals(0,
                analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, DeprecatedSignatureMacAlgorithms.class));
        assertEquals(0,
                analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, DeprecatedSignatureMacAlgorithms.class));
        assertEquals(1,
                analysis.countReports(JcDeprecatedSignatureAlg.RULE_ID, DeprecatedSignatureMacAlgorithms.class));
    }

    @Test
    void testDeprecatedRandomOneShotOnlyRandomDataRuleReports(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedRandomDataOneShot.class,
                JcTestRunner.sigVoidNoArg(DeprecatedRandomDataOneShot.class, "entryPseudoOneShot"));
        assertEquals(0,
                analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, DeprecatedRandomDataOneShot.class));
        assertEquals(1,
                analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, DeprecatedRandomDataOneShot.class));
        assertEquals(0,
                analysis.countReports(JcDeprecatedSignatureAlg.RULE_ID, DeprecatedRandomDataOneShot.class));
    }

    @Test
    void testCipherOneShotSm4EcbSilentForAllDeprecatedAlgRules(final JcTestRunner analysis) {
        analysis.analyse(OkCipherOneShotSm4Ecb.class,
                JcTestRunner.sigVoidNoArg(OkCipherOneShotSm4Ecb.class, "entrySm4EcbOneShot"));
        assertEquals(0, analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, OkCipherOneShotSm4Ecb.class));
        assertEquals(0, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, OkCipherOneShotSm4Ecb.class));
        assertEquals(0, analysis.countReports(JcDeprecatedSignatureAlg.RULE_ID, OkCipherOneShotSm4Ecb.class));
    }

    @Test
    void testOkMixedApisSilentForAllDeprecatedAlgRules(final JcTestRunner analysis) {
        analysis.analyse(OkDeprecatedAlgTaintIsolation.class,
                JcTestRunner.sigVoidNoArg(OkDeprecatedAlgTaintIsolation.class, "entryAllOkCalls"));
        assertEquals(0,
                analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, OkDeprecatedAlgTaintIsolation.class));
        assertEquals(0,
                analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, OkDeprecatedAlgTaintIsolation.class));
        assertEquals(0,
                analysis.countReports(JcDeprecatedSignatureAlg.RULE_ID, OkDeprecatedAlgTaintIsolation.class));
    }

    @Test
    void testCipherDesCbcByteOneTaggedAsPseudoRandomDoesNotTriggerRandomRule(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedAlgTaintSharedByteDomain.class,
                JcTestRunner.sigVoidNoArg(DeprecatedAlgTaintSharedByteDomain.class,
                        "entryCipherGetInstanceDesCbcByteOne"));
        assertEquals(0,
                analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, DeprecatedAlgTaintSharedByteDomain.class));
        assertEquals(0,
                analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, DeprecatedAlgTaintSharedByteDomain.class));
        assertEquals(0,
                analysis.countReports(JcDeprecatedSignatureAlg.RULE_ID, DeprecatedAlgTaintSharedByteDomain.class));
    }

    @Test
    void testCipherLiteralThirtyFiveTaggedAsMac192DoesNotTriggerSignatureRule(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedAlgTaintSharedByteDomain.class,
                JcTestRunner.sigVoidNoArg(DeprecatedAlgTaintSharedByteDomain.class,
                        "entryCipherGetInstanceLiteralThirtyFive"));
        assertEquals(0,
                analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, DeprecatedAlgTaintSharedByteDomain.class));
        assertEquals(0,
                analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, DeprecatedAlgTaintSharedByteDomain.class));
        assertEquals(0,
                analysis.countReports(JcDeprecatedSignatureAlg.RULE_ID, DeprecatedAlgTaintSharedByteDomain.class));
    }

    @Test
    void testRandomPseudoByteOneDoesNotTriggerCipherOrSignatureRules(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedAlgTaintSharedByteDomain.class,
                JcTestRunner.sigVoidNoArg(DeprecatedAlgTaintSharedByteDomain.class,
                        "entryRandomDataGetInstancePseudoByteOne"));
        assertEquals(0,
                analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, DeprecatedAlgTaintSharedByteDomain.class));
        assertEquals(1,
                analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, DeprecatedAlgTaintSharedByteDomain.class));
        assertEquals(0,
                analysis.countReports(JcDeprecatedSignatureAlg.RULE_ID, DeprecatedAlgTaintSharedByteDomain.class));
    }

    @Test
    void testSignatureMac192ByteThirtyFiveDoesNotTriggerCipherOrRandomRules(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedAlgTaintSharedByteDomain.class,
                JcTestRunner.sigVoidNoArg(DeprecatedAlgTaintSharedByteDomain.class,
                        "entrySignatureGetInstanceMac192ByteThirtyFive"));
        assertEquals(0,
                analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, DeprecatedAlgTaintSharedByteDomain.class));
        assertEquals(0,
                analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, DeprecatedAlgTaintSharedByteDomain.class));
        assertEquals(1,
                analysis.countReports(JcDeprecatedSignatureAlg.RULE_ID, DeprecatedAlgTaintSharedByteDomain.class));
    }

    @Test
    void testMixedDeprecatedCipherWithOkRandomAndSignatureOnlyCipherReports(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedAlgTaintSharedByteDomain.class,
                JcTestRunner.sigVoidNoArg(DeprecatedAlgTaintSharedByteDomain.class,
                        "entryMixedDeprecatedCipherWithOkRandomAndSignature"));
        assertEquals(1,
                analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, DeprecatedAlgTaintSharedByteDomain.class));
        assertEquals(0,
                analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, DeprecatedAlgTaintSharedByteDomain.class));
        assertEquals(0,
                analysis.countReports(JcDeprecatedSignatureAlg.RULE_ID, DeprecatedAlgTaintSharedByteDomain.class));
    }
}
