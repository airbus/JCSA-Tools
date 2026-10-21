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
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.airbus.esar.fixture.iso7816sw1.InvalidSw26201;
import com.airbus.esar.fixture.iso7816sw1.InvalidSw26382;
import com.airbus.esar.fixture.iso7816sw1.InvalidSw26481;
import com.airbus.esar.fixture.iso7816sw1.InvalidSw26701;
import com.airbus.esar.fixture.iso7816sw1.InvalidSw26A8B;
import com.airbus.esar.fixture.iso7816sw1.InvalidSw29001;
import com.airbus.esar.fixture.iso7816sw1.SilentUndecidableSw1FromParam;
import com.airbus.esar.fixture.iso7816sw1.ThrowsInvalidSw1AssignedLocal;
import com.airbus.esar.fixture.iso7816sw1.ThrowsInvalidSw1Gap70;
import com.airbus.esar.fixture.iso7816sw1.ThrowsInvalidSw1LowNibble;
import com.airbus.esar.fixture.iso7816sw1.ThrowsInvalidSw1Reserved60;
import com.airbus.esar.fixture.iso7816sw1.ThrowsInvalidSw1TwoBranchesOneInvalid;
import com.airbus.esar.fixture.iso7816sw1.ThrowsInvalidSw1ViaMethod;
import com.airbus.esar.fixture.iso7816sw1.ThrowsInvalidSwNineBranchesOneInvalid;
import com.airbus.esar.fixture.iso7816sw1.ThrowsValidSwTwoBranches;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwAppletSelectFailed;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwAuthenticationMethodBlocked;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwBytesRemaining00;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwBytesRemaining05;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwChangedCounterC0;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwChangedFileFilled;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwClaNotSupported;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwCommandChainingNotSupported;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwCommandNotAllowed;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwConditionsNotSatisfied;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwCorrectLength00;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwDataInvalid;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwDfNameAlreadyExists;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwErrorMemoryFailure;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwExecutionImmediateResponse;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwFileAlreadyExists;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwFileFull;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwFileNotFound;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwFuncNotSupported;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwIncorrectP1P2;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwInsNotSupported;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwLastCommandExpected;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwLogicalChannelNotSupported;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwNoError;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwProprietary9100;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwRecordNotFound;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwSecureMessagingNotSupported;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwSecurityStatusNotSatisfied;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwUnknown;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwWarningEof;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwWarningFciFormat;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwWarningFileDeactivated;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwWarningNoSensorData;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwWarningPartCorrupted;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwWarningStateUnchanged;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwWarningTerminationState;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwWrongData;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwWrongLe05;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwWrongLength;
import com.airbus.esar.fixture.iso7816sw1.valid.ValidSwWrongP1P2;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcIsoExceptionInvalidSw {

    @Test
    void testWarnsInvalidSw1LowNibble(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsInvalidSw1LowNibble.class, List.of(), 1);
    }

    @Test
    void testWarnsInvalidSw1Reserved60(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsInvalidSw1Reserved60.class, List.of(), 1);
    }

    @Test
    void testWarnsInvalidSw1Gap70(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsInvalidSw1Gap70.class, List.of(), 1);
    }

    @Test
    void testWarnsInvalidSw1AssignedLocal(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsInvalidSw1AssignedLocal.class, List.of(), 1);
    }

    @Test
    void testWarnsInvalidSw1ViaMethod(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsInvalidSw1ViaMethod.class, List.of(), 1);
    }

    @Test
    void testWarnsInvalidSw1TwoBranchesOneInvalid(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsInvalidSw1TwoBranchesOneInvalid.class, List.of("boolean"), 1);
    }

    @Test
    void testSilentValidSwTwoBranches(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsValidSwTwoBranches.class, List.of("boolean"), 0);
    }

    @Test
    void testWarnsInvalidSwNineBranchesOneInvalid(final JcTestRunner analysis) {
        assertReports(analysis, ThrowsInvalidSwNineBranchesOneInvalid.class, List.of("byte"), 1);
    }

    @Test
    void testWarnsInvalidSw29001(final JcTestRunner analysis) {
        assertReports(analysis, InvalidSw29001.class, List.of(), 1);
    }

    @Test
    void testSilencesAllowlistedCustomSw(final JcTestRunner analysis) {
        final JcIsoExceptionInvalidSw detector = new JcIsoExceptionInvalidSw();
        final JcIsoExceptionInvalidSw.Config config = new JcIsoExceptionInvalidSw.Config();
        config.setAllowedCustomSws(List.of(0x9001));
        detector.configure(config);

        analysis.analyse(
                InvalidSw29001.class,
                List.of(JcTestRunner.sig(InvalidSw29001.class, "entry", "void", List.of())),
                List.of(detector));
        assertEquals(0, analysis.countReports(JcIsoExceptionInvalidSw.RULE_ID, InvalidSw29001.class));
    }

    @Test
    void testWarnsInvalidSw26701(final JcTestRunner analysis) {
        assertReports(analysis, InvalidSw26701.class, List.of(), 1);
    }

    @Test
    void testWarnsInvalidSw26201(final JcTestRunner analysis) {
        assertReports(analysis, InvalidSw26201.class, List.of(), 1);
    }

    @Test
    void testWarnsInvalidSw26382(final JcTestRunner analysis) {
        assertReports(analysis, InvalidSw26382.class, List.of(), 1);
    }

    @Test
    void testWarnsInvalidSw26A8B(final JcTestRunner analysis) {
        assertReports(analysis, InvalidSw26A8B.class, List.of(), 1);
    }

    @Test
    void testWarnsInvalidSw26481(final JcTestRunner analysis) {
        assertReports(analysis, InvalidSw26481.class, List.of(), 1);
    }

    @Test
    void testSilentUndecidableSw1FromParam(final JcTestRunner analysis) {
        assertReports(analysis, SilentUndecidableSw1FromParam.class, List.of("short"), 0);
    }

    @Test
    void testSilentValidInterindustryStatusWords(final JcTestRunner analysis) {
        for (final Class<?> fixture : validInterindustryFixtures()) {
            assertReports(analysis, fixture, List.of(), 0);
        }
    }

    private static List<Class<?>> validInterindustryFixtures() {
        return Stream.of(
                ValidSwNoError.class,
                ValidSwBytesRemaining00.class,
                ValidSwBytesRemaining05.class,
                ValidSwWarningStateUnchanged.class,
                ValidSwWarningPartCorrupted.class,
                ValidSwWarningEof.class,
                ValidSwWarningFileDeactivated.class,
                ValidSwWarningFciFormat.class,
                ValidSwWarningTerminationState.class,
                ValidSwWarningNoSensorData.class,
                ValidSwChangedFileFilled.class,
                ValidSwChangedCounterC0.class,
                ValidSwExecutionImmediateResponse.class,
                ValidSwErrorMemoryFailure.class,
                ValidSwWrongLength.class,
                ValidSwLogicalChannelNotSupported.class,
                ValidSwSecureMessagingNotSupported.class,
                ValidSwLastCommandExpected.class,
                ValidSwCommandChainingNotSupported.class,
                ValidSwSecurityStatusNotSatisfied.class,
                ValidSwAuthenticationMethodBlocked.class,
                ValidSwDataInvalid.class,
                ValidSwConditionsNotSatisfied.class,
                ValidSwCommandNotAllowed.class,
                ValidSwAppletSelectFailed.class,
                ValidSwWrongData.class,
                ValidSwFuncNotSupported.class,
                ValidSwFileNotFound.class,
                ValidSwRecordNotFound.class,
                ValidSwIncorrectP1P2.class,
                ValidSwFileFull.class,
                ValidSwFileAlreadyExists.class,
                ValidSwDfNameAlreadyExists.class,
                ValidSwWrongP1P2.class,
                ValidSwWrongLe05.class,
                ValidSwCorrectLength00.class,
                ValidSwInsNotSupported.class,
                ValidSwClaNotSupported.class,
                ValidSwUnknown.class,
                ValidSwProprietary9100.class).toList();
    }

    private static void assertReports(final JcTestRunner analysis,
                                      final Class<?> fixture,
                                      final List<String> paramTypes,
                                      final int expected) {
        analysis.analyse(fixture, List.of(JcTestRunner.sig(fixture, "entry", "void", paramTypes)));
        assertEquals(expected, analysis.countReports(JcIsoExceptionInvalidSw.RULE_ID, fixture));
    }
}
