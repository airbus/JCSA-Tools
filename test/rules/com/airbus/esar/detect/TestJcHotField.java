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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.airbus.esar.fixture.hotfield.HfFieldCopiedToLocalReadSixTimes;
import com.airbus.esar.fixture.hotfield.HfFrequentReadAfterSwitch;
import com.airbus.esar.fixture.hotfield.HfKnownFnLoopAccessEarlyReturnInsideFor;
import com.airbus.esar.fixture.hotfield.HfKnownFpEarlyReturnInsideFor;
import com.airbus.esar.fixture.hotfield.HfFieldAccessedInDoWhileLoop;
import com.airbus.esar.fixture.hotfield.HfFieldAccessedInLoop;
import com.airbus.esar.fixture.hotfield.HfFieldAccessedInWhileLoop;
import com.airbus.esar.fixture.hotfield.HfFieldAccessedThreeTimes;
import com.airbus.esar.fixture.hotfield.HfFieldAccessedTwice;
import com.airbus.esar.fixture.hotfield.HfFieldFrequentReadInConditionals;
import com.airbus.esar.fixture.hotfield.HfFieldIfElseThreeReadsPerBranch;
import com.airbus.esar.fixture.hotfield.HfFieldIncrementInForUpdate;
import com.airbus.esar.fixture.hotfield.HfFieldOnlyOutsideLoop;
import com.airbus.esar.fixture.hotfield.HfFieldPassedToInvokeInLoop;
import com.airbus.esar.fixture.hotfield.HfFieldReadInIfCondition;
import com.airbus.esar.fixture.hotfield.HfFieldReadInIfElse;
import com.airbus.esar.fixture.hotfield.HfFieldReadInTryCatch;
import com.airbus.esar.fixture.hotfield.HfFieldReadInIfThen;
import com.airbus.esar.fixture.hotfield.HfFieldReadInsideIfInForLoop;
import com.airbus.esar.fixture.hotfield.HfFieldUsedInLoopCondition;
import com.airbus.esar.fixture.hotfield.HfFieldUsedInLoopInit;
import com.airbus.esar.fixture.hotfield.HfFieldWritesOnlyFiveTimes;
import com.airbus.esar.fixture.hotfield.HfSameFieldTwoReceiversBelowThreshold;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

/** {@link JcHotField} vs {@code fixture/hotfield} classes (registry default threshold). */
@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcHotField {

    private static List<ProgramVisitorIf> hotField(final int threshold) {
        return List.of(new JcHotField(threshold));
    }

    @Test
    void testWarnsWhenFieldAccessedThreeTimes(final JcTestRunner analysis) {
        analysis.analyse(HfFieldAccessedThreeTimes.class,
                JcTestRunner.sigVoidNoArg(HfFieldAccessedThreeTimes.class, "entry"));
        assertEquals(1, analysis.countReports(JcHotField.RULE_ID, HfFieldAccessedThreeTimes.class));
        final SourceRegion region = analysis.getFindings().getFirst().getRegion().orElseThrow();
        assertEquals(25, region.getFirstLine());
    }

    @Test
    void testFrequentAccessAnchorsEarliestSourceLineAfterSwitch(final JcTestRunner analysis) {
        analysis.analyse(HfFrequentReadAfterSwitch.class,
                List.of(JcTestRunner.sig(HfFrequentReadAfterSwitch.class, "entry", "void",
                        List.of("byte"))));
        assertEquals(1, analysis.countReports(JcHotField.RULE_ID, HfFrequentReadAfterSwitch.class));
        final SourceRegion region = analysis.getFindings().stream()
                .filter(f -> JcHotField.RULE_ID.equals(f.getRuleId()))
                .findFirst()
                .orElseThrow()
                .getRegion()
                .orElseThrow();
        assertEquals(38, region.getFirstLine());
    }

    @Test
    void testKnownFpFrequentAccessOnEarlyReturnInsideFor(final JcTestRunner analysis) {
        analysis.analyse(HfKnownFpEarlyReturnInsideFor.class,
                List.of(JcTestRunner.sig(HfKnownFpEarlyReturnInsideFor.class, "entry", "void",
                        List.of("boolean", "short"))));
        final long frequent = analysis.getFindings().stream()
                .filter(f -> JcHotField.RULE_ID.equals(f.getRuleId()))
                .filter(f -> JcHotField.MESSAGE_FREQUENT.equals(f.getMessageId()))
                .count();
        assertEquals(1, frequent);
    }

    @Disabled("NaturalLoopStmts misses early-return arm inside for(;;); enable when fixed")
    @Test
    void testKnownFnMissingLoopAccessOnEarlyReturnInsideFor(final JcTestRunner analysis) {
        analysis.analyse(HfKnownFnLoopAccessEarlyReturnInsideFor.class,
                List.of(JcTestRunner.sig(HfKnownFnLoopAccessEarlyReturnInsideFor.class, "entry",
                        "void", List.of("boolean", "short"))));
        final long loop = analysis.getFindings().stream()
                .filter(f -> JcHotField.RULE_ID.equals(f.getRuleId()))
                .filter(f -> JcHotField.MESSAGE_LOOP.equals(f.getMessageId()))
                .filter(f -> f.getMethodSignature().orElse("")
                        .startsWith("<" + HfKnownFnLoopAccessEarlyReturnInsideFor.class.getName() + ":"))
                .count();
        assertEquals(1, loop);
    }

    @Test
    void testWarnsWhenFieldFrequentlyReadInIfConditions(final JcTestRunner analysis) {
        analysis.analyse(HfFieldFrequentReadInConditionals.class,
                JcTestRunner.sigVoidNoArg(HfFieldFrequentReadInConditionals.class, "entry"));
        assertEquals(1, analysis.countReports(JcHotField.RULE_ID, HfFieldFrequentReadInConditionals.class));
    }

    @Test
    void testSilentWhenFieldCopiedToLocalAndLocalReadSixTimes(final JcTestRunner analysis) {
        analysis.analyse(HfFieldCopiedToLocalReadSixTimes.class,
                JcTestRunner.sigVoidNoArg(HfFieldCopiedToLocalReadSixTimes.class, "entry"));
        assertEquals(0, analysis.countReports(JcHotField.RULE_ID, HfFieldCopiedToLocalReadSixTimes.class));
    }

    @Test
    void testSilentWhenFieldAccessedTwice(final JcTestRunner analysis) {
        analysis.analyse(HfFieldAccessedTwice.class,
                JcTestRunner.sigVoidNoArg(HfFieldAccessedTwice.class, "entry"));
        assertEquals(0, analysis.countReports(JcHotField.RULE_ID, HfFieldAccessedTwice.class));
    }

    @Test
    void testWarnsWhenFieldAccessedInLoop(final JcTestRunner analysis) {
        analysis.analyse(HfFieldAccessedInLoop.class,
                JcTestRunner.sigVoidNoArg(HfFieldAccessedInLoop.class, "entry"));
        assertEquals(1, analysis.countReports(JcHotField.RULE_ID, HfFieldAccessedInLoop.class));
    }

    @Test
    void testWarnsWhenFieldAccessedInWhileLoop(final JcTestRunner analysis) {
        analysis.analyse(HfFieldAccessedInWhileLoop.class,
                JcTestRunner.sigVoidNoArg(HfFieldAccessedInWhileLoop.class, "entry"));
        assertEquals(1, analysis.countReports(JcHotField.RULE_ID, HfFieldAccessedInWhileLoop.class));
    }

    @Test
    void testWarnsWhenFieldAccessedInDoWhileLoop(final JcTestRunner analysis) {
        analysis.analyse(HfFieldAccessedInDoWhileLoop.class,
                JcTestRunner.sigVoidNoArg(HfFieldAccessedInDoWhileLoop.class, "entry"));
        assertEquals(1, analysis.countReports(JcHotField.RULE_ID, HfFieldAccessedInDoWhileLoop.class));
    }

    @Test
    void testWarnsWhenFieldReadInsideIfInsideForLoop(final JcTestRunner analysis) {
        analysis.analyse(HfFieldReadInsideIfInForLoop.class,
                JcTestRunner.sigVoidNoArg(HfFieldReadInsideIfInForLoop.class, "entry"));
        assertEquals(1, analysis.countReports(JcHotField.RULE_ID, HfFieldReadInsideIfInForLoop.class));
    }

    @Test
    void testWarnsWhenFieldPassedToInvokeInForLoop(final JcTestRunner analysis) {
        analysis.analyse(HfFieldPassedToInvokeInLoop.class,
                JcTestRunner.sigVoidNoArg(HfFieldPassedToInvokeInLoop.class, "entry"));
        assertEquals(1, analysis.countReports(JcHotField.RULE_ID, HfFieldPassedToInvokeInLoop.class));
    }

    @Test
    void testWarnsWhenFieldUsedInForCondition(final JcTestRunner analysis) {
        analysis.analyse(HfFieldUsedInLoopCondition.class,
                JcTestRunner.sigVoidNoArg(HfFieldUsedInLoopCondition.class, "entry"));
        assertEquals(1, analysis.countReports(JcHotField.RULE_ID, HfFieldUsedInLoopCondition.class));
    }

    @Test
    void testSilentWhenFieldUsedInForInit(final JcTestRunner analysis) {
        analysis.analyse(HfFieldUsedInLoopInit.class,
                JcTestRunner.sigVoidNoArg(HfFieldUsedInLoopInit.class, "entry"));
        assertEquals(0, analysis.countReports(JcHotField.RULE_ID, HfFieldUsedInLoopInit.class));
    }

    @Test
    void testWarnsWhenFieldIncrementedInForUpdate(final JcTestRunner analysis) {
        analysis.analyse(HfFieldIncrementInForUpdate.class,
                JcTestRunner.sigVoidNoArg(HfFieldIncrementInForUpdate.class, "entry"));
        assertEquals(1, analysis.countReports(JcHotField.RULE_ID, HfFieldIncrementInForUpdate.class));
    }

    @Test
    void testSilentWhenFieldOnlyOutsideLoop(final JcTestRunner analysis) {
        analysis.analyse(HfFieldOnlyOutsideLoop.class,
                JcTestRunner.sigVoidNoArg(HfFieldOnlyOutsideLoop.class, "entry"));
        assertEquals(0, analysis.countReports(JcHotField.RULE_ID, HfFieldOnlyOutsideLoop.class));
    }

    @Test
    void testSilentWhenFieldReadInIfThenBranch(final JcTestRunner analysis) {
        analysis.analyse(HfFieldReadInIfThen.class,
                JcTestRunner.sigVoidNoArg(HfFieldReadInIfThen.class, "entry"));
        assertEquals(0, analysis.countReports(JcHotField.RULE_ID, HfFieldReadInIfThen.class));
    }

    @Test
    void testSilentWhenFieldReadInIfCondition(final JcTestRunner analysis) {
        analysis.analyse(HfFieldReadInIfCondition.class,
                JcTestRunner.sigVoidNoArg(HfFieldReadInIfCondition.class, "entry"));
        assertEquals(0, analysis.countReports(JcHotField.RULE_ID, HfFieldReadInIfCondition.class));
    }

    @Test
    void testSilentWhenFieldReadInIfElseBranches(final JcTestRunner analysis) {
        analysis.analyse(HfFieldReadInIfElse.class,
                JcTestRunner.sigVoidNoArg(HfFieldReadInIfElse.class, "entry"));
        assertEquals(0, analysis.countReports(JcHotField.RULE_ID, HfFieldReadInIfElse.class));
    }

    @Test
    void testSilentWhenFieldReadInTryCatch(final JcTestRunner analysis) {
        analysis.analyse(HfFieldReadInTryCatch.class,
                JcTestRunner.sigVoidNoArg(HfFieldReadInTryCatch.class, "entry"));
        assertEquals(0, analysis.countReports(JcHotField.RULE_ID, HfFieldReadInTryCatch.class));
    }

    @Test
    void testSilentWhenIfElseMaxPathBelowThreshold(final JcTestRunner analysis) {
        analysis.analyse(HfFieldIfElseThreeReadsPerBranch.class,
                JcTestRunner.sigVoidNoArg(HfFieldIfElseThreeReadsPerBranch.class, "entry"),
                hotField(5));
        assertEquals(0, analysis.countReports(JcHotField.RULE_ID, HfFieldIfElseThreeReadsPerBranch.class));
    }

    @Test
    void testSilentWhenOnlyFieldWrites(final JcTestRunner analysis) {
        analysis.analyse(HfFieldWritesOnlyFiveTimes.class,
                JcTestRunner.sigVoidNoArg(HfFieldWritesOnlyFiveTimes.class, "entry"));
        assertEquals(0, analysis.countReports(JcHotField.RULE_ID, HfFieldWritesOnlyFiveTimes.class));
    }

    @Test
    void testSilentWhenSameFieldOnTwoReceiversEachBelowThreshold(final JcTestRunner analysis) {
        analysis.analyse(HfSameFieldTwoReceiversBelowThreshold.class,
                List.of(JcTestRunner.sig(HfSameFieldTwoReceiversBelowThreshold.class, "copy", "void",
                        List.of(HfSameFieldTwoReceiversBelowThreshold.class.getName()))));
        assertEquals(0, analysis.countReports(
                JcHotField.RULE_ID, HfSameFieldTwoReceiversBelowThreshold.class));
    }
}
