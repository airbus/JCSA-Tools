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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.airbus.esar.fixture.cipher.DeprecatedCipherAes192Ecb;
import com.airbus.esar.fixture.cipher.DeprecatedCipherAlgorithms;
import com.airbus.esar.fixture.cipher.DeprecatedCipherRsa9796;
import com.airbus.esar.fixture.cipher.OkCipherAlgorithms;
import com.airbus.esar.fixture.cipher.OkCipherOneShotSm4Ecb;
import com.airbus.esar.junit.JcTestRunner;
import com.airbus.esar.junit.JcRuleAnalysisExtension;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcDeprecatedCipherAlg {

    @Test
    void testWarnsTwoDeprecatedCiphers(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedCipherAlgorithms.class,
            JcTestRunner.sigVoidNoArg(DeprecatedCipherAlgorithms.class,
                "entryAes192Cbc", "entryRsaIso14888"));
        assertEquals(2, analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, DeprecatedCipherAlgorithms.class));
    }

    @Test
    void testWarnsAesOnly(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedCipherAlgorithms.class,
            JcTestRunner.sigVoidNoArg(DeprecatedCipherAlgorithms.class, "entryAes192Cbc"));
        assertEquals(1, analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, DeprecatedCipherAlgorithms.class));
    }

    @Test
    void testWarnsRsaOnly(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedCipherAlgorithms.class,
            JcTestRunner.sigVoidNoArg(DeprecatedCipherAlgorithms.class, "entryRsaIso14888"));
        assertEquals(1, analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, DeprecatedCipherAlgorithms.class));
    }

    @Test
    void testWarnsRsa9796Fixture(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedCipherRsa9796.class,
            JcTestRunner.sigVoidNoArg(DeprecatedCipherRsa9796.class, "entryRsa9796"));
        assertEquals(1, analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, DeprecatedCipherRsa9796.class));
    }

    @Test
    void testWarnsAes192EcbFixture(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedCipherAes192Ecb.class,
            JcTestRunner.sigVoidNoArg(DeprecatedCipherAes192Ecb.class, "entryAes192Ecb"));
        assertEquals(1, analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, DeprecatedCipherAes192Ecb.class));
    }

    @Test
    void testWarnsThreeArgCipher(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedCipherAlgorithms.class,
            JcTestRunner.sigVoidNoArg(DeprecatedCipherAlgorithms.class, "entryThreeArg"));
        assertEquals(1, analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, DeprecatedCipherAlgorithms.class));
    }

    @Test
    void testSilentOkCipher(final JcTestRunner analysis) {
        analysis.analyse(OkCipherAlgorithms.class,
            JcTestRunner.sigVoidNoArg(OkCipherAlgorithms.class, "entryDesEcb"));
        assertEquals(0, analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, OkCipherAlgorithms.class));
    }

    @Test
    void testSilentCipherOneShotSm4Ecb(final JcTestRunner analysis) {
        analysis.analyse(OkCipherOneShotSm4Ecb.class,
                JcTestRunner.sigVoidNoArg(OkCipherOneShotSm4Ecb.class, "entrySm4EcbOneShot"));
        assertEquals(0, analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, OkCipherOneShotSm4Ecb.class));
    }

    /**
     * Runs the cipher rule in isolation (without other rules sharing the same solver key) so that
     * the lazy-solver factory lambda in {@link JcDeprecatedCipherAlg#initAnalysis} is exercised.
     *
     * @param analysis test runner scoped to this test method
     */
    @Test
    void testLazySolverFactoryIsInvoked(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedCipherAlgorithms.class,
                JcTestRunner.sigVoidNoArg(DeprecatedCipherAlgorithms.class, "entryAes192Cbc"),
                List.of(new JcDeprecatedCipherAlg()));
        assertEquals(1, analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, DeprecatedCipherAlgorithms.class));
    }

    @Test
    void testDeprecatedCipherMessageShape(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedCipherAlgorithms.class,
            JcTestRunner.sigVoidNoArg(DeprecatedCipherAlgorithms.class, "entryAes192Cbc"));
        final EsarFinding f = analysis.getFindings().getFirst();
        assertEquals(JcDeprecatedCipherAlg.RULE_ID, f.getRuleId());
        assertEquals(SarifResultLevel.ERROR, f.getLevel());
        assertEquals(JcDeprecatedCipherAlg.MESSAGE_DEPRECATED_CIPHER, f.getMessageId());
        assertTrue(f.getMessage().contains("deprecated"));
        assertTrue(f.getMessage().contains("ALG_AES_BLOCK_192_CBC_NOPAD"));
    }
}
