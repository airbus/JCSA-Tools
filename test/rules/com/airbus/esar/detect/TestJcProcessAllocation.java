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

import com.airbus.esar.fixture.allocations.NonAppletWithProcess;
import com.airbus.esar.fixture.allocations.ProcessAllocatesArray;
import com.airbus.esar.fixture.allocations.ProcessAllocatesMultiArray;
import com.airbus.esar.fixture.allocations.ProcessAllocatesObject;
import com.airbus.esar.fixture.allocations.ProcessAllocatesObjectRecursive;
import com.airbus.esar.fixture.allocations.ProcessCalleeAllocates;
import com.airbus.esar.fixture.allocations.ProcessCtorAllocatesExtra;
import com.airbus.esar.fixture.allocations.ProcessNoAllocation;
import com.airbus.esar.fixture.allocations.ProcessWithStaticByteArrayField;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcProcessAllocation {

    private static final List<String> APDU_PARAM = List.of("javacard.framework.APDU");

    @Test
    void testWarnsWhenProcessAllocatesObject(final JcTestRunner analysis) {
        analysis.analyse(ProcessAllocatesObject.class,
                List.of(JcTestRunner.sig(ProcessAllocatesObject.class, "process", "void", APDU_PARAM)));
        assertEquals(1, analysis.countReports(JcProcessAllocation.RULE_ID, ProcessAllocatesObject.class));
    }

    @Test
    void testWarnsWhenProcessAllocatesArray(final JcTestRunner analysis) {
        analysis.analyse(ProcessAllocatesArray.class,
                List.of(JcTestRunner.sig(ProcessAllocatesArray.class, "process", "void", APDU_PARAM)));
        assertEquals(1, analysis.countReports(JcProcessAllocation.RULE_ID, ProcessAllocatesArray.class));
    }

    @Test
    void testWarnsWhenCalleeOfProcessAllocates(final JcTestRunner analysis) {
        analysis.analyse(ProcessCalleeAllocates.class,
                List.of(JcTestRunner.sig(ProcessCalleeAllocates.class, "process", "void", APDU_PARAM)));
        assertEquals(1, analysis.countReports(JcProcessAllocation.RULE_ID, ProcessCalleeAllocates.class));
    }

    @Test
    void testWarnsOnceWhenCtorAlsoAllocates(final JcTestRunner analysis) {
        analysis.analyse(ProcessCtorAllocatesExtra.class,
                List.of(JcTestRunner.sig(ProcessCtorAllocatesExtra.class, "process", "void", APDU_PARAM)));
        assertEquals(1, analysis.countReports(JcProcessAllocation.RULE_ID, ProcessCtorAllocatesExtra.class));
    }

    @Test
    void testWarnsWhenProcessIsRecursiveAndAllocates(final JcTestRunner analysis) {
        analysis.analyse(ProcessAllocatesObjectRecursive.class,
                List.of(JcTestRunner.sig(ProcessAllocatesObjectRecursive.class, "process", "void", APDU_PARAM)));
        assertEquals(1, analysis.countReports(JcProcessAllocation.RULE_ID, ProcessAllocatesObjectRecursive.class));
    }

    @Test
    void testWarnsWhenProcessAllocatesMultiArray(final JcTestRunner analysis) {
        analysis.analyse(ProcessAllocatesMultiArray.class,
                List.of(JcTestRunner.sig(ProcessAllocatesMultiArray.class, "process", "void", APDU_PARAM)));
        assertEquals(1, analysis.countReports(JcProcessAllocation.RULE_ID, ProcessAllocatesMultiArray.class));
    }

    @Test
    void testSilentWhenProcessHasNoAllocation(final JcTestRunner analysis) {
        analysis.analyse(ProcessNoAllocation.class,
                List.of(JcTestRunner.sig(ProcessNoAllocation.class, "process", "void", APDU_PARAM)));
        assertEquals(0, analysis.countReports(JcProcessAllocation.RULE_ID, ProcessNoAllocation.class));
    }

    @Test
    void testSilentWhenAppletHasStaticByteArrayField(final JcTestRunner analysis) {
        analysis.analyse(ProcessWithStaticByteArrayField.class,
                List.of(JcTestRunner.sig(ProcessWithStaticByteArrayField.class, "process", "void", APDU_PARAM)));
        assertEquals(0, analysis.countReports(JcProcessAllocation.RULE_ID, ProcessWithStaticByteArrayField.class));
    }

    @Test
    void testSilentWhenClassIsNotAnApplet(final JcTestRunner analysis) {
        analysis.analyse(NonAppletWithProcess.class,
                List.of(JcTestRunner.sig(NonAppletWithProcess.class, "process", "void", APDU_PARAM)));
        assertEquals(0, analysis.countReports(JcProcessAllocation.RULE_ID, NonAppletWithProcess.class));
    }
}
