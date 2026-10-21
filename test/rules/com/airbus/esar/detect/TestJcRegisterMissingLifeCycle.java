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

import com.airbus.esar.fixture.appletregister.InstallExtendsRegisteringBaseApplet;
import com.airbus.esar.fixture.appletregister.InstallCallsInheritedSyncRegister;
import com.airbus.esar.fixture.appletregister.InstallCallsNonAppletRegister;
import com.airbus.esar.fixture.appletregister.InstallCallsRegister;
import com.airbus.esar.fixture.appletregister.InstallConstructorCallsRegister;
import com.airbus.esar.fixture.appletregister.InstallConstructorRegisterWithAid;
import com.airbus.esar.fixture.appletregister.InstallDeepChainNoRegister;
import com.airbus.esar.fixture.appletregister.InstallEmptyNoRegister;
import com.airbus.esar.fixture.appletregister.InstallHelperDoesNotRegister;
import com.airbus.esar.fixture.appletregister.InstallMissingRegister;
import com.airbus.esar.fixture.appletregister.NonAppletWithInstallMethod;
import com.airbus.esar.fixture.appletregister.InstallRegisterViaHelper;
import com.airbus.esar.fixture.appletregister.InstallRegisterWithAid;
import com.airbus.esar.fixture.appletregister.InstallStaticPeerNoRegister;
import com.airbus.esar.fixture.appletregister.InstallThrowsWithoutRegister;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcRegisterMissingLifeCycle {

    private static final List<String> INSTALL_ENTRY_PARAM_TYPES = List.of("byte[]", "short", "byte");

    @Test
    void testWarnsWhenInstallOmitsRegister(final JcTestRunner analysis) {
        analysis.analyse(InstallMissingRegister.class,
                List.of(JcTestRunner.sig(InstallMissingRegister.class, "install", "void",
                        INSTALL_ENTRY_PARAM_TYPES)));
        assertEquals(1, analysis.countReports(JcRegisterMissingLifeCycle.RULE_ID, InstallMissingRegister.class));
    }

    @Test
    void testWarnsWhenInstallBodyEmpty(final JcTestRunner analysis) {
        analysis.analyse(InstallEmptyNoRegister.class,
                List.of(JcTestRunner.sig(InstallEmptyNoRegister.class, "install", "void",
                        INSTALL_ENTRY_PARAM_TYPES)));
        assertEquals(1, analysis.countReports(JcRegisterMissingLifeCycle.RULE_ID, InstallEmptyNoRegister.class));
    }

    @Test
    void testWarnsWhenHelperOmitsRegister(final JcTestRunner analysis) {
        analysis.analyse(InstallHelperDoesNotRegister.class,
                List.of(JcTestRunner.sig(InstallHelperDoesNotRegister.class, "install", "void",
                        INSTALL_ENTRY_PARAM_TYPES)));
        assertEquals(1,
                analysis.countReports(JcRegisterMissingLifeCycle.RULE_ID, InstallHelperDoesNotRegister.class));
    }

    @Test
    void testWarnsWhenInstallThrowsWithoutRegister(final JcTestRunner analysis) {
        analysis.analyse(InstallThrowsWithoutRegister.class,
                List.of(JcTestRunner.sig(InstallThrowsWithoutRegister.class, "install", "void",
                        INSTALL_ENTRY_PARAM_TYPES)));
        assertEquals(1,
                analysis.countReports(JcRegisterMissingLifeCycle.RULE_ID, InstallThrowsWithoutRegister.class));
    }

    @Test
    void testWarnsWhenStaticPeerOmitsRegister(final JcTestRunner analysis) {
        analysis.analyse(InstallStaticPeerNoRegister.class,
                List.of(JcTestRunner.sig(InstallStaticPeerNoRegister.class, "install", "void",
                        INSTALL_ENTRY_PARAM_TYPES)));
        assertEquals(1,
                analysis.countReports(JcRegisterMissingLifeCycle.RULE_ID, InstallStaticPeerNoRegister.class));
    }

    @Test
    void testWarnsWhenDeepCallChainOmitsRegister(final JcTestRunner analysis) {
        analysis.analyse(InstallDeepChainNoRegister.class,
                List.of(JcTestRunner.sig(InstallDeepChainNoRegister.class, "install", "void",
                        INSTALL_ENTRY_PARAM_TYPES)));
        assertEquals(1,
                analysis.countReports(JcRegisterMissingLifeCycle.RULE_ID, InstallDeepChainNoRegister.class));
    }

    @Test
    void testSilentWhenClassDoesNotExtendApplet(final JcTestRunner analysis) {
        analysis.analyse(NonAppletWithInstallMethod.class,
                List.of(JcTestRunner.sig(NonAppletWithInstallMethod.class, "install", "void",
                        INSTALL_ENTRY_PARAM_TYPES)));
        assertEquals(0,
                analysis.countReports(JcRegisterMissingLifeCycle.RULE_ID, NonAppletWithInstallMethod.class));
    }

    @Test
    void testSilentInstallCallsRegister(final JcTestRunner analysis) {
        analysis.analyse(InstallCallsRegister.class,
                List.of(JcTestRunner.sig(InstallCallsRegister.class, "install", "void",
                        INSTALL_ENTRY_PARAM_TYPES)));
        assertEquals(0, analysis.countReports(JcRegisterMissingLifeCycle.RULE_ID, InstallCallsRegister.class));
    }

    @Test
    void testSilentInstallCallsInheritedSyncRegister(final JcTestRunner analysis) {
        analysis.analyse(InstallCallsInheritedSyncRegister.class,
                List.of(JcTestRunner.sig(InstallCallsInheritedSyncRegister.class, "install", "void",
                        INSTALL_ENTRY_PARAM_TYPES)));
        assertEquals(0,
                analysis.countReports(JcRegisterMissingLifeCycle.RULE_ID, InstallCallsInheritedSyncRegister.class));
    }

    @Test
    void testSilentInstallRegisterWithAid(final JcTestRunner analysis) {
        analysis.analyse(InstallRegisterWithAid.class,
                List.of(JcTestRunner.sig(InstallRegisterWithAid.class, "install", "void",
                        INSTALL_ENTRY_PARAM_TYPES)));
        assertEquals(0, analysis.countReports(JcRegisterMissingLifeCycle.RULE_ID, InstallRegisterWithAid.class));
    }

    @Test
    void testSilentInstallRegisterViaHelper(final JcTestRunner analysis) {
        analysis.analyse(InstallRegisterViaHelper.class,
                List.of(JcTestRunner.sig(InstallRegisterViaHelper.class, "install", "void",
                        INSTALL_ENTRY_PARAM_TYPES)));
        assertEquals(0, analysis.countReports(JcRegisterMissingLifeCycle.RULE_ID, InstallRegisterViaHelper.class));
    }

    @Test
    void testSilentInstallExtendsAppletWithRegisteringConstructor(final JcTestRunner analysis) {
        analysis.analyse(InstallExtendsRegisteringBaseApplet.class,
                List.of(JcTestRunner.sig(InstallExtendsRegisteringBaseApplet.class, "install", "void",
                        INSTALL_ENTRY_PARAM_TYPES)));
        assertEquals(0,
                analysis.countReports(JcRegisterMissingLifeCycle.RULE_ID, InstallExtendsRegisteringBaseApplet.class));
    }

    @Test
    void testSilentConstructorCallsRegister(final JcTestRunner analysis) {
        analysis.analyse(InstallConstructorCallsRegister.class,
                List.of(JcTestRunner.sig(InstallConstructorCallsRegister.class, "install", "void",
                        INSTALL_ENTRY_PARAM_TYPES)));
        assertEquals(0,
                analysis.countReports(JcRegisterMissingLifeCycle.RULE_ID, InstallConstructorCallsRegister.class));
    }

    @Test
    void testSilentInstallSkipsNonAppletRegisterCall(final JcTestRunner analysis) {
        analysis.analyse(InstallCallsNonAppletRegister.class,
                List.of(JcTestRunner.sig(InstallCallsNonAppletRegister.class, "install", "void",
                        INSTALL_ENTRY_PARAM_TYPES)));
        assertEquals(0,
                analysis.countReports(JcRegisterMissingLifeCycle.RULE_ID, InstallCallsNonAppletRegister.class));
    }

    @Test
    void testSilentConstructorRegisterWithAid(final JcTestRunner analysis) {
        analysis.analyse(InstallConstructorRegisterWithAid.class,
                List.of(JcTestRunner.sig(InstallConstructorRegisterWithAid.class, "install", "void",
                        INSTALL_ENTRY_PARAM_TYPES)));
        assertEquals(0,
                analysis.countReports(JcRegisterMissingLifeCycle.RULE_ID, InstallConstructorRegisterWithAid.class));
    }
}
