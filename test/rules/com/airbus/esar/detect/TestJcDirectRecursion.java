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

import sootup.core.signatures.MethodSignature;
import com.airbus.esar.fixture.recursion.DirectRecursionInstance;
import com.airbus.esar.fixture.recursion.DirectRecursionStatic;
import com.airbus.esar.fixture.recursion.IndirectRecursionAb;
import com.airbus.esar.fixture.recursion.NoRecursionOk;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcDirectRecursion {

    private static List<MethodSignature> sigVoidInt(final Class<?> c, final String name) {
        return List.of(JcTestRunner.sig(c, name, "void", List.of("int")));
    }

    @Test
    void testWarnsStaticDirectRecursion(final JcTestRunner analysis) {
        analysis.analyse(DirectRecursionStatic.class, sigVoidInt(DirectRecursionStatic.class, "entry"));
        assertEquals(1, analysis.countReports(JcDirectRecursion.RULE_ID, DirectRecursionStatic.class));
    }

    @Test
    void testWarnsInstanceDirectRecursion(final JcTestRunner analysis) {
        analysis.analyse(DirectRecursionInstance.class, sigVoidInt(DirectRecursionInstance.class, "entry"));
        assertEquals(1, analysis.countReports(JcDirectRecursion.RULE_ID, DirectRecursionInstance.class));
    }

    @Test
    void testSilentIndirectOnly(final JcTestRunner analysis) {
        analysis.analyse(IndirectRecursionAb.class,
                JcTestRunner.sigVoidNoArg(IndirectRecursionAb.class, "entryA", "entryB"));
        assertEquals(0, analysis.countReports(JcDirectRecursion.RULE_ID, IndirectRecursionAb.class));
    }

    @Test
    void testSilentNoRecursion(final JcTestRunner analysis) {
        analysis.analyse(NoRecursionOk.class, JcTestRunner.sigVoidNoArg(NoRecursionOk.class, "entry"));
        assertEquals(0, analysis.countReports(JcDirectRecursion.RULE_ID, NoRecursionOk.class));
    }

    @Test
    void testMessageShape(final JcTestRunner analysis) {
        analysis.analyse(DirectRecursionStatic.class, sigVoidInt(DirectRecursionStatic.class, "entry"));
        final EsarFinding f = analysis.getFindings().stream()
                .filter(x -> JcDirectRecursion.RULE_ID.equals(x.getRuleId()))
                .filter(x -> x.getMethodSignature().orElse("").contains(DirectRecursionStatic.class.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals(JcDirectRecursion.RULE_ID, f.getRuleId());
        assertEquals(SarifResultLevel.ERROR, f.getLevel());
        assertEquals(JcDirectRecursion.MESSAGE_DIRECT_RECURSION, f.getMessageId());
        assertTrue(f.getMessage().toLowerCase().contains("recursion"));
    }
}
