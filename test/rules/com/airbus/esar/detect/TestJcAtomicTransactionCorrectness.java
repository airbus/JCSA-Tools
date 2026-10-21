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
import com.airbus.esar.fixture.transaction.BalancedAbort;
import com.airbus.esar.fixture.transaction.BalancedCommit;
import com.airbus.esar.fixture.transaction.CatchThrowableAborts;
import com.airbus.esar.fixture.transaction.CommitOnlyInOneBranch;
import com.airbus.esar.fixture.transaction.CommitOrAbortOnBothBranches;
import com.airbus.esar.fixture.transaction.ExceptionPathAborts;
import com.airbus.esar.fixture.transaction.ExceptionPathLeaks;
import com.airbus.esar.fixture.transaction.HelperCommits;
import com.airbus.esar.fixture.transaction.HelperCommitsConditionally;
import com.airbus.esar.fixture.transaction.HelperDoesNotCommit;
import com.airbus.esar.fixture.transaction.LoopWithBeginCommit;
import com.airbus.esar.fixture.transaction.MissingClose;
import com.airbus.esar.fixture.transaction.NestedAllMissing;
import com.airbus.esar.fixture.transaction.NestedBalanced;
import com.airbus.esar.fixture.transaction.NestedOuterMissingClose;
import com.airbus.esar.fixture.transaction.NoTransaction;
import com.airbus.esar.fixture.transaction.TransitiveConditionalCommit;
import com.airbus.esar.fixture.transaction.TryFinallyBeginCommit;
import com.airbus.esar.fixture.transaction.TryFinallyCommitAbortCleanup;
import com.airbus.esar.fixture.transaction.TwoSequentialTransactions;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcAtomicTransactionCorrectness {

    private static List<MethodSignature> entryWork(final Class<?> fixture, final String... paramTypes) {
        return List.of(JcTestRunner.sig(fixture, "work", "void", List.of(paramTypes)));
    }

    @Test
    void testSilentOnBalancedCommit(final JcTestRunner analysis) {
        analysis.analyse(BalancedCommit.class, entryWork(BalancedCommit.class, "short"));
        assertEquals(0, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, BalancedCommit.class));
    }

    @Test
    void testSilentOnBalancedAbort(final JcTestRunner analysis) {
        analysis.analyse(BalancedAbort.class, entryWork(BalancedAbort.class, "short"));
        assertEquals(0, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, BalancedAbort.class));
    }

    @Test
    void testWarnsWhenCloseMissing(final JcTestRunner analysis) {
        analysis.analyse(MissingClose.class, entryWork(MissingClose.class, "short"));
        assertEquals(1, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, MissingClose.class));
    }

    @Test
    void testWarnsWhenOnlyOneBranchCloses(final JcTestRunner analysis) {
        analysis.analyse(CommitOnlyInOneBranch.class,
                entryWork(CommitOnlyInOneBranch.class, "short", "boolean"));
        assertEquals(1, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, CommitOnlyInOneBranch.class));
    }

    @Test
    void testSilentWhenBothBranchesClose(final JcTestRunner analysis) {
        analysis.analyse(CommitOrAbortOnBothBranches.class,
                entryWork(CommitOrAbortOnBothBranches.class, "short", "boolean"));
        assertEquals(0, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, CommitOrAbortOnBothBranches.class));
    }

    @Test
    void testSilentOnNestedBalanced(final JcTestRunner analysis) {
        analysis.analyse(NestedBalanced.class, entryWork(NestedBalanced.class, "short"));
        assertEquals(0, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, NestedBalanced.class));
    }

    @Test
    void testWarnsOnNestedOuterMissingClose(final JcTestRunner analysis) {
        analysis.analyse(NestedOuterMissingClose.class,
                entryWork(NestedOuterMissingClose.class, "short"));
        assertEquals(1, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, NestedOuterMissingClose.class));
    }

    @Test
    void testWarnsOnNestedAllMissing(final JcTestRunner analysis) {
        analysis.analyse(NestedAllMissing.class, entryWork(NestedAllMissing.class, "short"));
        assertTrue(analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, NestedAllMissing.class) >= 1,
                "expected at least one unbalanced begin report");
    }

    @Test
    void testSilentOnTwoSequentialTransactions(final JcTestRunner analysis) {
        analysis.analyse(TwoSequentialTransactions.class,
                entryWork(TwoSequentialTransactions.class, "short"));
        assertEquals(0, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, TwoSequentialTransactions.class));
    }

    @Test
    void testSilentWhenHelperCommits(final JcTestRunner analysis) {
        analysis.analyse(HelperCommits.class, entryWork(HelperCommits.class, "short"));
        assertEquals(0, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, HelperCommits.class));
    }

    @Test
    void testWarnsWhenHelperDoesNotCommit(final JcTestRunner analysis) {
        analysis.analyse(HelperDoesNotCommit.class,
                entryWork(HelperDoesNotCommit.class, "short"));
        assertEquals(1, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, HelperDoesNotCommit.class));
    }

    @Test
    void testSilentOnNoTransaction(final JcTestRunner analysis) {
        analysis.analyse(NoTransaction.class, entryWork(NoTransaction.class, "short"));
        assertEquals(0, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, NoTransaction.class));
    }

    @Test
    void testSilentOnExceptionPathAborts(final JcTestRunner analysis) {
        analysis.analyse(ExceptionPathAborts.class,
                entryWork(ExceptionPathAborts.class, "short"));
        assertEquals(0, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, ExceptionPathAborts.class));
    }

    @Test
    void testWarnsOnExceptionPathLeaks(final JcTestRunner analysis) {
        analysis.analyse(ExceptionPathLeaks.class,
                entryWork(ExceptionPathLeaks.class, "short"));
        assertEquals(1, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, ExceptionPathLeaks.class));
    }

    @Test
    void testSilentOnLoopWithBeginCommit(final JcTestRunner analysis) {
        analysis.analyse(LoopWithBeginCommit.class,
                entryWork(LoopWithBeginCommit.class, "short"));
        assertEquals(0, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, LoopWithBeginCommit.class));
    }

    @Test
    void testWarnsOnHelperCommitsConditionally(final JcTestRunner analysis) {
        analysis.analyse(HelperCommitsConditionally.class,
                entryWork(HelperCommitsConditionally.class, "short", "boolean"));
        assertEquals(1, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, HelperCommitsConditionally.class));
    }

    @Test
    void testWarnsOnTransitiveConditionalCommit(final JcTestRunner analysis) {
        analysis.analyse(TransitiveConditionalCommit.class,
                entryWork(TransitiveConditionalCommit.class, "short", "boolean"));
        assertEquals(1, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, TransitiveConditionalCommit.class));
    }

    @Test
    void testSilentOnTryFinallyBeginCommit(final JcTestRunner analysis) {
        analysis.analyse(TryFinallyBeginCommit.class,
                entryWork(TryFinallyBeginCommit.class, "short"));
        assertEquals(0, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, TryFinallyBeginCommit.class));
    }

    @Test
    void testSilentOnTryFinallyCommitAbortCleanup(final JcTestRunner analysis) {
        analysis.analyse(TryFinallyCommitAbortCleanup.class,
                entryWork(TryFinallyCommitAbortCleanup.class, "short"));
        assertEquals(0, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, TryFinallyCommitAbortCleanup.class));
    }

    @Test
    void testSilentOnCatchThrowableAborts(final JcTestRunner analysis) {
        analysis.analyse(CatchThrowableAborts.class,
                entryWork(CatchThrowableAborts.class, "short"));
        assertEquals(0, analysis.countReports(
                JcAtomicTransactionCorrectness.RULE_ID, CatchThrowableAborts.class));
    }

}
