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
import com.airbus.esar.fixture.runtimeexception.AllowedCardRuntimeRethrows;
import com.airbus.esar.fixture.runtimeexception.CustomRte;
import com.airbus.esar.fixture.runtimeexception.ThrowsAssignedRte;
import com.airbus.esar.fixture.runtimeexception.ThrowsAssignedRteBranching;
import com.airbus.esar.fixture.runtimeexception.ThrowsIsoException;
import com.airbus.esar.fixture.runtimeexception.ThrowsCardException;
import com.airbus.esar.fixture.runtimeexception.ThrowsRuntimeException;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcRuntimeExceptionsNotAllowed {

    private static final List<MethodSignature> ENTRY_THROWS_RUNTIME =
            List.of(JcTestRunner.sig(ThrowsRuntimeException.class, "process", "void", List.of()));

    private static final List<MethodSignature> ENTRY_THROWS_ISO =
            List.of(JcTestRunner.sig(ThrowsIsoException.class, "process", "void", List.of()));

    private static final List<MethodSignature> ENTRY_CUSTOM_RTE =
            List.of(JcTestRunner.sig(CustomRte.class, "throwIt", "void", List.of()));

    private static final List<MethodSignature> ENTRY_ASSIGNED_RTE =
            List.of(JcTestRunner.sig(ThrowsAssignedRte.class, "process", "void", List.of()));

    private static final List<MethodSignature> ENTRY_ASSIGNED_RTE_BRANCHING =
            List.of(JcTestRunner.sig(ThrowsAssignedRteBranching.class, "process", "void", List.of("boolean")));

    private static final List<MethodSignature> ENTRY_THROWS_CARD_EXCEPTION =
            List.of(JcTestRunner.sig(ThrowsCardException.class, "process", "void",
                    List.of("javacard.framework.CardException")));

    private static void assertSilentAllowedRethrow(final JcTestRunner analysis, final String methodName) {
        analysis.analyse(AllowedCardRuntimeRethrows.class,
                List.of(JcTestRunner.sig(AllowedCardRuntimeRethrows.class, methodName, "void", List.of())));
        final String needle = AllowedCardRuntimeRethrows.class.getName() + ": void " + methodName + "()";
        final long reports = analysis.getFindings().stream()
                .filter(f -> JcRuntimeExceptionsNotAllowed.RULE_ID.equals(f.getRuleId()))
                .filter(f -> f.getMethodSignature().orElse("").contains(needle))
                .count();
        assertEquals(0, reports, methodName);
    }

    @Test
    void testWarnsThrowRuntimeException(final JcTestRunner analysis) {
        analysis.analyse(ThrowsRuntimeException.class, ENTRY_THROWS_RUNTIME);
        assertEquals(1, analysis.countReports(JcRuntimeExceptionsNotAllowed.RULE_ID, ThrowsRuntimeException.class));
    }

    @Test
    void testSilentIsoExceptionThrowIt(final JcTestRunner analysis) {
        analysis.analyse(ThrowsIsoException.class, ENTRY_THROWS_ISO);
        assertEquals(0, analysis.countReports(JcRuntimeExceptionsNotAllowed.RULE_ID, ThrowsIsoException.class));
    }

    @Test
    void testSilentRethrowCardRuntimeException(final JcTestRunner analysis) {
        assertSilentAllowedRethrow(analysis, "rethrowCardRuntimeException");
    }

    @Test
    void testSilentRethrowIsoException(final JcTestRunner analysis) {
        assertSilentAllowedRethrow(analysis, "rethrowIsoException");
    }

    @Test
    void testSilentRethrowApduException(final JcTestRunner analysis) {
        assertSilentAllowedRethrow(analysis, "rethrowApduException");
    }

    @Test
    void testSilentRethrowPinException(final JcTestRunner analysis) {
        assertSilentAllowedRethrow(analysis, "rethrowPinException");
    }

    @Test
    void testSilentRethrowSystemException(final JcTestRunner analysis) {
        assertSilentAllowedRethrow(analysis, "rethrowSystemException");
    }

    @Test
    void testSilentRethrowTransactionException(final JcTestRunner analysis) {
        assertSilentAllowedRethrow(analysis, "rethrowTransactionException");
    }

    @Test
    void testSilentRethrowServiceException(final JcTestRunner analysis) {
        assertSilentAllowedRethrow(analysis, "rethrowServiceException");
    }

    @Test
    void testSilentRethrowCryptoException(final JcTestRunner analysis) {
        assertSilentAllowedRethrow(analysis, "rethrowCryptoException");
    }

    @Test
    void testSilentRethrowBioException(final JcTestRunner analysis) {
        assertSilentAllowedRethrow(analysis, "rethrowBioException");
    }

    @Test
    void testSilentRethrowBio1toNException(final JcTestRunner analysis) {
        assertSilentAllowedRethrow(analysis, "rethrowBio1toNException");
    }

    @Test
    void testSilentRethrowExternalException(final JcTestRunner analysis) {
        assertSilentAllowedRethrow(analysis, "rethrowExternalException");
    }

    @Test
    void testSilentRethrowStringException(final JcTestRunner analysis) {
        assertSilentAllowedRethrow(analysis, "rethrowStringException");
    }

    @Test
    void testSilentRethrowDateTimeException(final JcTestRunner analysis) {
        assertSilentAllowedRethrow(analysis, "rethrowDateTimeException");
    }

    @Test
    void testSilentRethrowTlvException(final JcTestRunner analysis) {
        assertSilentAllowedRethrow(analysis, "rethrowTlvException");
    }

    @Test
    void testSilentRethrowUtilException(final JcTestRunner analysis) {
        assertSilentAllowedRethrow(analysis, "rethrowUtilException");
    }

    @Test
    void testSilentRethrowCertificateException(final JcTestRunner analysis) {
        assertSilentAllowedRethrow(analysis, "rethrowCertificateException");
    }

    @Test
    void testWarnsCustomRteSubclassThrow(final JcTestRunner analysis) {
        analysis.analyse(CustomRte.class, ENTRY_CUSTOM_RTE);
        assertEquals(1, analysis.countReports(JcRuntimeExceptionsNotAllowed.RULE_ID, CustomRte.class));
    }

    @Test
    void testWarnsAssignedRte(final JcTestRunner analysis) {
        analysis.analyse(ThrowsAssignedRte.class, ENTRY_ASSIGNED_RTE);
        assertEquals(1, analysis.countReports(JcRuntimeExceptionsNotAllowed.RULE_ID, ThrowsAssignedRte.class));
    }

    @Test
    void testWarnsAssignedRteBranching(final JcTestRunner analysis) {
        analysis.analyse(ThrowsAssignedRteBranching.class, ENTRY_ASSIGNED_RTE_BRANCHING);
        assertEquals(1, analysis.countReports(JcRuntimeExceptionsNotAllowed.RULE_ID, ThrowsAssignedRteBranching.class));
    }

    @Test
    void testSilentCardExceptionIsNotRuntimeException(final JcTestRunner analysis) {
        analysis.analyse(ThrowsCardException.class, ENTRY_THROWS_CARD_EXCEPTION);
        assertEquals(0, analysis.countReports(JcRuntimeExceptionsNotAllowed.RULE_ID, ThrowsCardException.class));
    }
}
