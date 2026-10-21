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

import com.airbus.esar.fixture.recursion.DirectRecursionStatic;
import com.airbus.esar.fixture.recursion.IndirectCycleWithExternalCall;
import com.airbus.esar.fixture.recursion.IndirectRecursionAb;
import com.airbus.esar.fixture.recursion.NoRecursionOk;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcCallGraphCycle {
    @Test
    void testNotesIndirectCycle(final JcTestRunner analysis) {
        analysis.analyse(IndirectRecursionAb.class,
                JcTestRunner.sigVoidNoArg(IndirectRecursionAb.class, "entryA"));
        assertEquals(2, analysis.countReports(JcCallGraphCycle.RULE_ID, IndirectRecursionAb.class));
    }

    @Test
    void testNotesIndirectCycleWithExternalCall(final JcTestRunner analysis) {
        analysis.analyse(IndirectCycleWithExternalCall.class,
                List.of(JcTestRunner.sig(IndirectCycleWithExternalCall.class, "entryA", "void", List.of("byte[]"))));
        assertEquals(2, analysis.countReports(JcCallGraphCycle.RULE_ID, IndirectCycleWithExternalCall.class));
    }

    @Test
    void testSilentDirectOnlySelfLoop(final JcTestRunner analysis) {
        analysis.analyse(DirectRecursionStatic.class,
                List.of(JcTestRunner.sig(DirectRecursionStatic.class, "entry", "void", List.of("int"))));
        assertEquals(0, analysis.countReports(JcCallGraphCycle.RULE_ID, DirectRecursionStatic.class));
    }

    @Test
    void testSilentAcyclic(final JcTestRunner analysis) {
        analysis.analyse(NoRecursionOk.class, JcTestRunner.sigVoidNoArg(NoRecursionOk.class, "entry"));
        assertEquals(0, analysis.countReports(JcCallGraphCycle.RULE_ID, NoRecursionOk.class));
    }
}
