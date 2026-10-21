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
import com.airbus.esar.fixture.blacklist.NoBlacklistedReference;
import com.airbus.esar.fixture.exception.CustomCardException;
import com.airbus.esar.fixture.exception.CustomCheckedException;
import com.airbus.esar.fixture.exception.CustomRuntimeException;
import com.airbus.esar.fixture.exception.NotAnException;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcCustomException {

    private static final List<MethodSignature> ENTRY_CHECKED = List.of(
            JcTestRunner.sig(CustomCheckedException.class, "<init>", "void",
                    List.of("java.lang.String")));

    private static final List<MethodSignature> ENTRY_RUNTIME = List.of(
            JcTestRunner.sig(CustomRuntimeException.class, "<init>", "void",
                    List.of("java.lang.String")));

    private static final List<MethodSignature> ENTRY_CARD = List.of(
            JcTestRunner.sig(CustomCardException.class, "<init>", "void",
                    List.of("short")));

    private static final List<MethodSignature> ENTRY_NOT_EXCEPTION = List.of(
            JcTestRunner.sig(NotAnException.class, "doSomething", "void", List.of()));

    private static final List<String> INSTALL_PARAM_TYPES = List.of("byte[]", "short", "byte");

    private static final List<MethodSignature> ENTRY_CLEAN_APPLET = List.of(
            JcTestRunner.sig(NoBlacklistedReference.class, "install", "void", INSTALL_PARAM_TYPES));

    @Test
    void testWarnsCustomCheckedException(final JcTestRunner analysis) {
        analysis.analyse(CustomCheckedException.class, ENTRY_CHECKED);
        assertEquals(1, analysis.countReports(JcCustomException.RULE_ID,
                CustomCheckedException.class));
    }

    @Test
    void testWarnsCustomRuntimeException(final JcTestRunner analysis) {
        analysis.analyse(CustomRuntimeException.class, ENTRY_RUNTIME);
        assertEquals(1, analysis.countReports(JcCustomException.RULE_ID,
                CustomRuntimeException.class));
    }

    @Test
    void testWarnsCustomCardException(final JcTestRunner analysis) {
        analysis.analyse(CustomCardException.class, ENTRY_CARD);
        assertEquals(1, analysis.countReports(JcCustomException.RULE_ID,
                CustomCardException.class));
    }

    @Test
    void testSilentNotAnException(final JcTestRunner analysis) {
        analysis.analyse(NotAnException.class, ENTRY_NOT_EXCEPTION);
        assertEquals(0, analysis.countReports(JcCustomException.RULE_ID,
                NotAnException.class));
    }

    @Test
    void testMessageContainsClassName(final JcTestRunner analysis) {
        analysis.analyse(CustomCheckedException.class, ENTRY_CHECKED);
        assertTrue(analysis.getFindings().stream()
                .filter(f -> JcCustomException.RULE_ID.equals(f.getRuleId()))
                .anyMatch(f -> f.getMessage().contains("CustomCheckedException")));
    }

    @Test
    void testSilentJavaLangException(final JcTestRunner analysis) {
        assertSilentWhitelistedException(analysis, "java.lang.IndexOutOfBoundsException");
    }

    @Test
    void testSilentJavaIoException(final JcTestRunner analysis) {
        assertSilentWhitelistedException(analysis, "java.io.IOException");
    }

    @Test
    void testSilentJavaRmiException(final JcTestRunner analysis) {
        assertSilentWhitelistedException(analysis, "java.rmi.RemoteException");
    }

    @Test
    void testSilentJavacardFrameworkException(final JcTestRunner analysis) {
        assertSilentWhitelistedException(analysis, "javacard.framework.CardException");
    }

    @Test
    void testSilentJavacardSecurityException(final JcTestRunner analysis) {
        assertSilentWhitelistedException(analysis, "javacard.security.CryptoException");
    }

    @Test
    void testSilentJavacardxBiometryException(final JcTestRunner analysis) {
        assertSilentWhitelistedException(analysis, "javacardx.biometry.BioException");
    }

    @Test
    void testSilentJavacardxBiometry1toNException(final JcTestRunner analysis) {
        assertSilentWhitelistedException(analysis, "javacardx.biometry1toN.Bio1toNException");
    }

    @Test
    void testSilentJavacardxExternalException(final JcTestRunner analysis) {
        assertSilentWhitelistedException(analysis, "javacardx.external.ExternalException");
    }

    @Test
    void testSilentJavacardxFrameworkException(final JcTestRunner analysis) {
        assertSilentWhitelistedException(analysis, "javacardx.framework.string.StringException");
    }

    @Test
    void testSilentJavacardxSecurityException(final JcTestRunner analysis) {
        assertSilentWhitelistedException(analysis, "javacardx.security.cert.CertificateException");
    }

    private static void assertSilentWhitelistedException(final JcTestRunner analysis,
                                                           final String exceptionFqn) {
        analysis.analyseAllClasses(
                NoBlacklistedReference.class,
                ENTRY_CLEAN_APPLET,
                List.of(new JcCustomException()));
        assertTrue(analysis.getFindings().stream()
                .filter(f -> JcCustomException.RULE_ID.equals(f.getRuleId()))
                .noneMatch(f -> f.getClassSignature().equals(exceptionFqn)),
                () -> "unexpected report for " + exceptionFqn);
    }
}
