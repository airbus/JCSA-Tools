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
import com.airbus.esar.fixture.uninstall.AppletWithUninstall;
import com.airbus.esar.fixture.uninstall.AppletWithoutUninstall;
import com.airbus.esar.fixture.uninstall.NonAppletWithUninstall;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcMissingUninstall {

    private static final List<String> INSTALL_PARAM_TYPES = List.of("byte[]", "short", "byte");

    private static final List<MethodSignature> ENTRY_WITH_UNINSTALL = List.of(
            JcTestRunner.sig(AppletWithUninstall.class, "install", "void", INSTALL_PARAM_TYPES));

    private static final List<MethodSignature> ENTRY_WITHOUT_UNINSTALL = List.of(
            JcTestRunner.sig(AppletWithoutUninstall.class, "install", "void", INSTALL_PARAM_TYPES));

    private static final List<MethodSignature> ENTRY_NON_APPLET = List.of(
            JcTestRunner.sig(NonAppletWithUninstall.class, "uninstall", "void", List.of()));

    @Test
    void testWarnsAppletWithoutUninstall(final JcTestRunner analysis) {
        analysis.analyse(AppletWithoutUninstall.class, ENTRY_WITHOUT_UNINSTALL);
        assertEquals(1, analysis.countReports(JcMissingUninstall.RULE_ID,
                AppletWithoutUninstall.class));
        final String installSig = AppletWithoutUninstall.class.getName()
                + ": void install(byte[],short,byte)";
        assertTrue(analysis.getFindings().stream()
                .filter(f -> JcMissingUninstall.RULE_ID.equals(f.getRuleId()))
                .anyMatch(f -> f.getMethodSignature().orElse("").contains(installSig)));
    }

    @Test
    void testSilentAppletWithUninstall(final JcTestRunner analysis) {
        analysis.analyse(AppletWithUninstall.class, ENTRY_WITH_UNINSTALL);
        assertEquals(0, analysis.countReports(JcMissingUninstall.RULE_ID,
                AppletWithUninstall.class));
    }

    @Test
    void testSilentNonApplet(final JcTestRunner analysis) {
        analysis.analyse(NonAppletWithUninstall.class, ENTRY_NON_APPLET);
        assertEquals(0, analysis.countReports(JcMissingUninstall.RULE_ID,
                NonAppletWithUninstall.class));
    }
}
