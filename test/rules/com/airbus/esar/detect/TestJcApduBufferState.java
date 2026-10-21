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
import com.airbus.esar.fixture.apdu.ApduBalancedChunked;
import com.airbus.esar.fixture.apdu.ApduBalancedFull;
import com.airbus.esar.fixture.apdu.ApduBalancedShortcut;
import com.airbus.esar.fixture.apdu.ApduBranchOneMissingSend;
import com.airbus.esar.fixture.apdu.ApduDoubleSetIncoming;
import com.airbus.esar.fixture.apdu.ApduDoubleSetOutgoing;
import com.airbus.esar.fixture.apdu.ApduGetIncomingLengthAfterSetOutgoing;
import com.airbus.esar.fixture.apdu.ApduGetIncomingLengthWithoutReceive;
import com.airbus.esar.fixture.apdu.ApduGetOffsetCdataWithoutReceive;
import com.airbus.esar.fixture.apdu.ApduHelperSends;
import com.airbus.esar.fixture.apdu.ApduIncomingBalancedFull;
import com.airbus.esar.fixture.apdu.ApduIncomingReceiveOnly;
import com.airbus.esar.fixture.apdu.ApduMissingLength;
import com.airbus.esar.fixture.apdu.ApduMissingSend;
import com.airbus.esar.fixture.apdu.ApduNoProtocol;
import com.airbus.esar.fixture.apdu.ApduPathCorrelatedBranches;
import com.airbus.esar.fixture.apdu.ApduReceiveWithoutSetIncoming;
import com.airbus.esar.fixture.apdu.ApduSendWithoutLength;
import com.airbus.esar.fixture.apdu.ApduSendWithoutSetOutgoing;
import com.airbus.esar.fixture.apdu.ApduWaitExtensionAfterNoChaining;
import com.airbus.esar.fixture.apdu.ApduWaitExtensionWhileIncoming;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcApduBufferState {

    private static final String APDU_FQN = "javacard.framework.APDU";

    private static List<MethodSignature> entryWork(final Class<?> fixture, final String... paramTypes) {
        return List.of(JcTestRunner.sig(fixture, "work",
                fixture.equals(ApduNoProtocol.class) ? "byte" : "void",
                List.of(paramTypes)));
    }

    @Test
    void testSilentOnBalancedFull(final JcTestRunner analysis) {
        analysis.analyse(ApduBalancedFull.class, entryWork(ApduBalancedFull.class, APDU_FQN));
        assertEquals(0, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduBalancedFull.class));
    }

    @Test
    void testSilentOnBalancedShortcut(final JcTestRunner analysis) {
        analysis.analyse(ApduBalancedShortcut.class, entryWork(ApduBalancedShortcut.class, APDU_FQN));
        assertEquals(0, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduBalancedShortcut.class));
    }

    @Test
    void testSilentOnBalancedChunked(final JcTestRunner analysis) {
        analysis.analyse(ApduBalancedChunked.class, entryWork(ApduBalancedChunked.class, APDU_FQN));
        assertEquals(0, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduBalancedChunked.class));
    }

    @Test
    void testWarnsWhenLengthMissing(final JcTestRunner analysis) {
        analysis.analyse(ApduMissingLength.class, entryWork(ApduMissingLength.class, APDU_FQN));
        assertEquals(1, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduMissingLength.class));
    }

    @Test
    void testWarnsWhenSendMissing(final JcTestRunner analysis) {
        analysis.analyse(ApduMissingSend.class, entryWork(ApduMissingSend.class, APDU_FQN));
        assertEquals(1, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduMissingSend.class));
    }

    @Test
    void testWarnsOnSendWithoutLength(final JcTestRunner analysis) {
        analysis.analyse(ApduSendWithoutLength.class,
                entryWork(ApduSendWithoutLength.class, APDU_FQN));
        assertEquals(1, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduSendWithoutLength.class));
    }

    @Test
    void testWarnsOnDoubleSetOutgoing(final JcTestRunner analysis) {
        analysis.analyse(ApduDoubleSetOutgoing.class,
                entryWork(ApduDoubleSetOutgoing.class, APDU_FQN));
        assertEquals(1, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduDoubleSetOutgoing.class));
    }

    @Test
    void testWarnsOnSendWithoutSetOutgoing(final JcTestRunner analysis) {
        analysis.analyse(ApduSendWithoutSetOutgoing.class,
                entryWork(ApduSendWithoutSetOutgoing.class, APDU_FQN));
        assertEquals(1, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduSendWithoutSetOutgoing.class));
    }

    @Test
    void testWarnsOnBranchOneMissingSend(final JcTestRunner analysis) {
        analysis.analyse(ApduBranchOneMissingSend.class,
                entryWork(ApduBranchOneMissingSend.class, APDU_FQN, "boolean"));
        assertEquals(1, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduBranchOneMissingSend.class));
    }

    @Test
    void testSilentWhenHelperSends(final JcTestRunner analysis) {
        analysis.analyse(ApduHelperSends.class, entryWork(ApduHelperSends.class, APDU_FQN));
        assertEquals(0, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduHelperSends.class));
    }

    @Test
    void testSilentOnNoProtocol(final JcTestRunner analysis) {
        analysis.analyse(ApduNoProtocol.class, entryWork(ApduNoProtocol.class, APDU_FQN));
        assertEquals(0, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduNoProtocol.class));
    }

    @Test
    void testSilentOnPathCorrelatedBranches(final JcTestRunner analysis) {
        analysis.analyse(ApduPathCorrelatedBranches.class,
                entryWork(ApduPathCorrelatedBranches.class, APDU_FQN, "boolean"));
        assertEquals(0, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduPathCorrelatedBranches.class));
    }

    @Test
    void testSilentOnIncomingBalancedFull(final JcTestRunner analysis) {
        analysis.analyse(ApduIncomingBalancedFull.class, entryWork(ApduIncomingBalancedFull.class, APDU_FQN));
        assertEquals(0, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduIncomingBalancedFull.class));
    }

    @Test
    void testSilentOnIncomingReceiveOnly(final JcTestRunner analysis) {
        analysis.analyse(ApduIncomingReceiveOnly.class, entryWork(ApduIncomingReceiveOnly.class, APDU_FQN));
        assertEquals(0, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduIncomingReceiveOnly.class));
    }

    @Test
    void testSilentOnWaitExtensionWhileIncoming(final JcTestRunner analysis) {
        analysis.analyse(ApduWaitExtensionWhileIncoming.class,
                entryWork(ApduWaitExtensionWhileIncoming.class, APDU_FQN));
        assertEquals(0, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduWaitExtensionWhileIncoming.class));
    }

    @Test
    void testWarnsOnReceiveWithoutSetIncoming(final JcTestRunner analysis) {
        analysis.analyse(ApduReceiveWithoutSetIncoming.class,
                entryWork(ApduReceiveWithoutSetIncoming.class, APDU_FQN));
        assertEquals(1, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduReceiveWithoutSetIncoming.class));
    }

    @Test
    void testWarnsOnDoubleSetIncoming(final JcTestRunner analysis) {
        analysis.analyse(ApduDoubleSetIncoming.class, entryWork(ApduDoubleSetIncoming.class, APDU_FQN));
        assertEquals(1, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduDoubleSetIncoming.class));
    }

    @Test
    void testWarnsOnGetIncomingLengthWithoutReceive(final JcTestRunner analysis) {
        analysis.analyse(ApduGetIncomingLengthWithoutReceive.class,
                entryWork(ApduGetIncomingLengthWithoutReceive.class, APDU_FQN));
        assertEquals(1, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduGetIncomingLengthWithoutReceive.class));
    }

    @Test
    void testWarnsOnGetOffsetCdataWithoutReceive(final JcTestRunner analysis) {
        analysis.analyse(ApduGetOffsetCdataWithoutReceive.class,
                entryWork(ApduGetOffsetCdataWithoutReceive.class, APDU_FQN));
        assertEquals(1, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduGetOffsetCdataWithoutReceive.class));
    }

    @Test
    void testWarnsOnGetIncomingLengthAfterSetOutgoing(final JcTestRunner analysis) {
        analysis.analyse(ApduGetIncomingLengthAfterSetOutgoing.class,
                entryWork(ApduGetIncomingLengthAfterSetOutgoing.class, APDU_FQN));
        assertEquals(1, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduGetIncomingLengthAfterSetOutgoing.class));
    }

    @Test
    void testWarnsOnWaitExtensionAfterNoChaining(final JcTestRunner analysis) {
        analysis.analyse(ApduWaitExtensionAfterNoChaining.class,
                entryWork(ApduWaitExtensionAfterNoChaining.class, APDU_FQN));
        assertEquals(1, analysis.countReports(
                JcApduBufferState.RULE_ID, ApduWaitExtensionAfterNoChaining.class));
    }
}
