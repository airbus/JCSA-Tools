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

import sootup.core.signatures.MethodSignature;
import com.airbus.esar.fixture.catchexception.CatchesException;
import com.airbus.esar.fixture.catchexception.CatchesSpecificException;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcCatchException {

    private static final List<MethodSignature> ENTRY_CATCHES_EXCEPTION = List.of(
            JcTestRunner.sig(CatchesException.class, "doWork", "void", List.of()));

    private static final List<MethodSignature> ENTRY_CATCHES_SPECIFIC = List.of(
            JcTestRunner.sig(CatchesSpecificException.class, "doWork", "void", List.of()));

    @Test
    void testWarnsCatchException(final JcTestRunner analysis) {
        analysis.analyse(CatchesException.class, ENTRY_CATCHES_EXCEPTION);
        assertEquals(1, analysis.countReports(JcCatchException.RULE_ID, CatchesException.class));
        final SourceRegion region = analysis.getFindings().stream()
                .filter(f -> JcCatchException.RULE_ID.equals(f.getRuleId()))
                .findFirst()
                .flatMap(f -> f.getRegion())
                .orElseThrow();
        assertEquals(25, region.getFirstLine());
    }

    @Test
    void testSilentSpecificException(final JcTestRunner analysis) {
        analysis.analyse(CatchesSpecificException.class, ENTRY_CATCHES_SPECIFIC);
        assertEquals(0, analysis.countReports(JcCatchException.RULE_ID, CatchesSpecificException.class));
    }
}
