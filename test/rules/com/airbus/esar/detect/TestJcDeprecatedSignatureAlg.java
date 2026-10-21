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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.airbus.esar.fixture.signature.DeprecatedSignatureMacAlgorithms;
import com.airbus.esar.fixture.signature.OkSignatureAlgorithms;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcDeprecatedSignatureAlg {

    @Test
    void testWarnsMac192And256(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedSignatureMacAlgorithms.class,
                JcTestRunner.sigVoidNoArg(DeprecatedSignatureMacAlgorithms.class, "entryMac192", "entryMac256"));
        assertEquals(2,
                analysis.countReports(JcDeprecatedSignatureAlg.RULE_ID, DeprecatedSignatureMacAlgorithms.class));
    }

    @Test
    void testWarnsMac192Only(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedSignatureMacAlgorithms.class,
                JcTestRunner.sigVoidNoArg(DeprecatedSignatureMacAlgorithms.class, "entryMac192"));
        assertEquals(1,
                analysis.countReports(JcDeprecatedSignatureAlg.RULE_ID, DeprecatedSignatureMacAlgorithms.class));
    }

    @Test
    void testSilentOkSignature(final JcTestRunner analysis) {
        analysis.analyse(OkSignatureAlgorithms.class,
                JcTestRunner.sigVoidNoArg(OkSignatureAlgorithms.class, "entryRsaShaPkcs1"));
        assertEquals(0, analysis.countReports(JcDeprecatedSignatureAlg.RULE_ID, OkSignatureAlgorithms.class));
    }

    @Test
    void testDeprecatedSignatureMessageShape(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedSignatureMacAlgorithms.class,
                JcTestRunner.sigVoidNoArg(DeprecatedSignatureMacAlgorithms.class, "entryMac192"));
        final EsarFinding f = analysis.getFindings().getFirst();
        assertEquals(JcDeprecatedSignatureAlg.RULE_ID, f.getRuleId());
        assertEquals(SarifResultLevel.ERROR, f.getLevel());
        assertEquals(JcDeprecatedSignatureAlg.MESSAGE_DEPRECATED_SIGNATURE, f.getMessageId());
        assertTrue(f.getMessage().contains("deprecated"));
        assertTrue(f.getMessage().contains("ALG_AES_MAC_192_NOPAD"));
    }
}
