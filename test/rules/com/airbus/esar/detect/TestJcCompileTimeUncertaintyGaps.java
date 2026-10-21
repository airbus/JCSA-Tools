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

import com.airbus.esar.fixture.comptime.ComptimeConstantThroughCallee;
import com.airbus.esar.fixture.comptime.ComptimeEdgeCaseParametricIdentityCallee;
import com.airbus.esar.fixture.comptime.ComptimeMessageDigestAlgViaConstructor;
import com.airbus.esar.fixture.comptime.ComptimeMutableStaticCipher;
import com.airbus.esar.fixture.comptime.ComptimeMutableStaticField;
import com.airbus.esar.fixture.comptime.ComptimeSwitch;
import com.airbus.esar.fixture.comptime.ComptimeUnsoundBackwardScanBranchMerge;
import com.airbus.esar.fixture.randomdata.DeprecatedRandomDataIfdsChallenging;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

/**
 * Regression tests for stricter “compile-time known” semantics (CFG merge, {@code static final} only, shallow callees
 * with call-site binding for parameters). Fixtures: {@code com.airbus.esar.fixture.comptime}.
 */
@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcCompileTimeUncertaintyGaps {
    @Test
    void testUncertaintyMutableStaticRd(final JcTestRunner analysis) {
        final Class<?> c = ComptimeMutableStaticField.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryMutableStatic", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyMutableStaticCipher(final JcTestRunner analysis) {
        final Class<?> c = ComptimeMutableStaticCipher.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryMutableStatic", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyBehindCallee(final JcTestRunner analysis) {
        final Class<?> c = ComptimeConstantThroughCallee.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "entryConstantBehindCallee"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyConstructorParamConstant(final JcTestRunner analysis) {
        final Class<?> c = ComptimeMessageDigestAlgViaConstructor.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "entryConstant"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyConstructorParamDynamic(final JcTestRunner analysis) {
        final Class<?> c = ComptimeMessageDigestAlgViaConstructor.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryDynamic", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyParametricIdentity(final JcTestRunner analysis) {
        final Class<?> c = ComptimeEdgeCaseParametricIdentityCallee.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "entryConstantThroughParameterizedIdentity"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyBranchMergedAlg(final JcTestRunner analysis) {
        final Class<?> c = ComptimeUnsoundBackwardScanBranchMerge.class;
        analysis.analyse(
                c, List.of(JcTestRunner.sig(c, "entryBranchMerge", "void", List.of("byte", "boolean"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintySwitchBuffer(final JcTestRunner analysis) {
        final Class<?> c = ComptimeSwitch.class;
        analysis.analyse(
                c, List.of(JcTestRunner.sig(c, "entry", "void", List.of("byte[]"))));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testDeepPseudoDeprecatedAndUncertainty(final JcTestRunner analysis) {
        final Class<?> c = DeprecatedRandomDataIfdsChallenging.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "entryDeepPseudoChain"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
        assertEquals(1, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, c));
    }
}
