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

import com.airbus.esar.fixture.randomdata.DeprecatedRandomData;
import com.airbus.esar.fixture.randomdata.DeprecatedRandomDataCastPseudo;
import com.airbus.esar.fixture.randomdata.DeprecatedRandomDataDataFlow;
import com.airbus.esar.fixture.randomdata.DeprecatedRandomDataIfdsChallenging;
import com.airbus.esar.fixture.randomdata.DeprecatedRandomDataDynamicAlg;
import com.airbus.esar.fixture.randomdata.DeprecatedRandomDataIfdsUnresolved;
import com.airbus.esar.fixture.randomdata.DeprecatedRandomDataOneShot;
import com.airbus.esar.fixture.randomdata.DeprecatedRandomDataNumeric;
import com.airbus.esar.fixture.randomdata.OkRandomData;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcDeprecatedRandomDataAlg {
    @Test
    void testWarnsFieldPseudoAndSecure(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedRandomData.class,
            JcTestRunner.sigVoidNoArg(DeprecatedRandomData.class, "pseudo", "secure"));
        assertEquals(2, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, DeprecatedRandomData.class));
    }

    @Test
    void testWarnsPseudoOnly(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedRandomData.class,
            JcTestRunner.sigVoidNoArg(DeprecatedRandomData.class, "pseudo"));
        assertEquals(1, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, DeprecatedRandomData.class));
    }

    @Test
    void testWarnsSecureOnly(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedRandomData.class,
            JcTestRunner.sigVoidNoArg(DeprecatedRandomData.class, "secure"));
        assertEquals(1, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, DeprecatedRandomData.class));
    }

    @Test
    void testWarnsCastPseudoFixture(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedRandomDataCastPseudo.class,
            JcTestRunner.sigVoidNoArg(DeprecatedRandomDataCastPseudo.class, "entryCastPseudo"));
        assertEquals(1, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, DeprecatedRandomDataCastPseudo.class));
    }

    @Test
    void testWarnsNumericLiteral(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedRandomDataNumeric.class,
            JcTestRunner.sigVoidNoArg(DeprecatedRandomDataNumeric.class, "literal"));
        assertEquals(1, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, DeprecatedRandomDataNumeric.class));
    }

    @Test
    void testWarnsDataflowPseudoAndSecure(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedRandomDataDataFlow.class,
            JcTestRunner.sigVoidNoArg(DeprecatedRandomDataDataFlow.class,
            "dataflowPseudo", "dataflowSecure"));
        assertEquals(2, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, DeprecatedRandomDataDataFlow.class));
    }

    @Test
    void testWarnsDeepPseudoChain(final JcTestRunner analysis) {
        final Class<?> c = DeprecatedRandomDataIfdsChallenging.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "entryDeepPseudoChain"));
        assertEquals(1, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, c));
    }

    @Test
    void testWarnsNumericThroughCallees(final JcTestRunner analysis) {
        final Class<?> c = DeprecatedRandomDataIfdsChallenging.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "entryNumericThroughCallee"));
        assertEquals(1, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, c));
    }

    @Test
    void testWarnsBranchMayPseudo(final JcTestRunner analysis) {
        final Class<?> c = DeprecatedRandomDataIfdsChallenging.class;
        analysis.analyse(c,
                List.of(JcTestRunner.sig(c, "entryMayPseudoDependingOnParam", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, c));
    }

    @Test
    void testWarnsBranchPseudoAndSecure(final JcTestRunner analysis) {
        final Class<?> c = DeprecatedRandomDataIfdsChallenging.class;
        analysis.analyse(c,
                List.of(JcTestRunner.sig(c, "entryBranchPseudoAndSecure", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, c));
    }

    @Test
    void testSilentTrngDeepChain(final JcTestRunner analysis) {
        final Class<?> c = DeprecatedRandomDataIfdsChallenging.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "entryTrngDeepOnly"));
        assertEquals(0, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, c));
    }

    @Test
    void testSilentParameterFedAlg(final JcTestRunner analysis) {
        final Class<?> c = DeprecatedRandomDataDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryParameterByte", "void", List.of("byte"))));
        assertEquals(0, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, c));
    }

    @Test
    void testWarnsFoldedLocalConstant(final JcTestRunner analysis) {
        final Class<?> c = DeprecatedRandomDataIfdsUnresolved.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "entryLocalConstantMismatch"));
        assertEquals(1, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, c));
    }

    @Test
    void testSilentOkRandomData(final JcTestRunner analysis) {
        analysis.analyse(OkRandomData.class,
                JcTestRunner.sigVoidNoArg(OkRandomData.class, "ok1", "ok2", "ok3", "ok4"));
        assertEquals(0, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, OkRandomData.class));
    }

    @Test
    void testWarnsOneShotPseudo(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedRandomDataOneShot.class,
                JcTestRunner.sigVoidNoArg(DeprecatedRandomDataOneShot.class, "entryPseudoOneShot"));
        assertEquals(1, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, DeprecatedRandomDataOneShot.class));
    }

    @Test
    void testFindingShapeLiteral(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedRandomDataNumeric.class,
                JcTestRunner.sigVoidNoArg(DeprecatedRandomDataNumeric.class, "literal"));
        final EsarFinding f = analysis.getFindings().getFirst();
        assertEquals(JcDeprecatedRandomDataAlg.RULE_ID, f.getRuleId());
        assertEquals(SarifResultLevel.ERROR, f.getLevel());
        assertEquals(JcDeprecatedRandomDataAlg.MESSAGE_DEPRECATED_ALGORITHM, f.getMessageId());
        assertTrue(f.getMessage().contains("deprecated"));
        assertTrue(f.getMessage().contains("ALG_PSEUDO_RANDOM") || f.getMessage().contains("numeric"));
    }

    @Test
    void testFindingShapeLiteral2(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedRandomDataNumeric.class,
                JcTestRunner.sigVoidNoArg(DeprecatedRandomDataNumeric.class, "literal2"));
        final EsarFinding f = analysis.getFindings().getFirst();
        assertEquals(JcDeprecatedRandomDataAlg.RULE_ID, f.getRuleId());
        assertEquals(SarifResultLevel.ERROR, f.getLevel());
        assertEquals(JcDeprecatedRandomDataAlg.MESSAGE_DEPRECATED_ALGORITHM, f.getMessageId());
        assertTrue(f.getMessage().contains("deprecated"));
        assertTrue(f.getMessage().contains("ALG_PSEUDO_RANDOM") || f.getMessage().contains("numeric"));
    }
}
