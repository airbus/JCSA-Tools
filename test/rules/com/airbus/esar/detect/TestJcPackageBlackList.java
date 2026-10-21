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
import com.airbus.esar.fixture.blacklist.ExtendsBlacklistedBasicService;
import com.airbus.esar.fixture.blacklist.FieldUsesBlacklistedDispatcher;
import com.airbus.esar.fixture.blacklist.ImplementsBlacklistedService;
import com.airbus.esar.fixture.blacklist.NoBlacklistedReference;
import com.airbus.esar.fixture.blacklist.UsesBlacklistedServiceDispatcher;
import com.airbus.esar.fixture.blacklist.UsesBlacklistedServiceException;
import com.airbus.esar.fixture.blacklist.UsesBlacklistedServiceMultiArray;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcPackageBlackList {

    private static final List<MethodSignature> ENTRY_DISPATCHER = List.of(
            JcTestRunner.sig(UsesBlacklistedServiceDispatcher.class, "entry", "void", List.of()));

    private static final List<MethodSignature> ENTRY_EXCEPTION = List.of(
            JcTestRunner.sig(UsesBlacklistedServiceException.class, "entry", "void", List.of()));

    private static final List<MethodSignature> ENTRY_EXTENDS_BASIC_SERVICE = List.of(
            JcTestRunner.sig(ExtendsBlacklistedBasicService.class, "processDataIn", "boolean",
                    List.of("javacard.framework.APDU")));

    private static final List<MethodSignature> ENTRY_IMPLEMENTS_SERVICE = List.of(
            JcTestRunner.sig(ImplementsBlacklistedService.class, "processDataIn", "boolean",
                    List.of("javacard.framework.APDU")));

    private static final List<MethodSignature> ENTRY_FIELD_DISPATCHER = List.of(
            JcTestRunner.sig(FieldUsesBlacklistedDispatcher.class, "getDispatcher",
                    "javacard.framework.service.Dispatcher", List.of()));

    private static final List<MethodSignature> ENTRY_MULTI_ARRAY = List.of(
            JcTestRunner.sig(UsesBlacklistedServiceMultiArray.class, "entry", "void", List.of()));

    private static final List<String> INSTALL_PARAM_TYPES = List.of("byte[]", "short", "byte");

    @Test
    void testWarnsBlacklistedNewInBody(final JcTestRunner analysis) {
        analysis.analyse(UsesBlacklistedServiceDispatcher.class, ENTRY_DISPATCHER);
        assertTrue(analysis.getFindings().stream()
                .filter(f -> JcPackageBlackList.RULE_ID.equals(f.getRuleId()))
                .anyMatch(f -> f.getMessage().contains("javacard.framework.service.")));
    }

    @Test
    void testWarnsBlacklistedExceptionUsage(final JcTestRunner analysis) {
        analysis.analyse(UsesBlacklistedServiceException.class, ENTRY_EXCEPTION);
        assertTrue(analysis.getFindings().stream()
                .filter(f -> JcPackageBlackList.RULE_ID.equals(f.getRuleId()))
                .anyMatch(f -> f.getMessage().contains("javacard.framework.service.")));
    }

    @Test
    void testWarnsBlacklistedSuperclass(final JcTestRunner analysis) {
        analysis.analyse(ExtendsBlacklistedBasicService.class, ENTRY_EXTENDS_BASIC_SERVICE);
        assertTrue(analysis.getFindings().stream()
                .filter(f -> JcPackageBlackList.RULE_ID.equals(f.getRuleId()))
                .anyMatch(f -> f.getMessage().contains("BasicService")));
    }

    @Test
    void testWarnsBlacklistedInterface(final JcTestRunner analysis) {
        analysis.analyse(ImplementsBlacklistedService.class, ENTRY_IMPLEMENTS_SERVICE);
        assertTrue(analysis.getFindings().stream()
                .filter(f -> JcPackageBlackList.RULE_ID.equals(f.getRuleId()))
                .anyMatch(f -> f.getMessage().contains("javacard.framework.service.")));
    }

    @Test
    void testWarnsBlacklistedField(final JcTestRunner analysis) {
        analysis.analyse(FieldUsesBlacklistedDispatcher.class, ENTRY_FIELD_DISPATCHER);
        assertTrue(analysis.getFindings().stream()
                .filter(f -> JcPackageBlackList.RULE_ID.equals(f.getRuleId()))
                .anyMatch(f -> f.getMessage().contains("Dispatcher")));
    }

    @Test
    void testWarnsBlacklistedMultiArrayAllocation(final JcTestRunner analysis) {
        analysis.analyse(UsesBlacklistedServiceMultiArray.class, ENTRY_MULTI_ARRAY);
        assertTrue(analysis.getFindings().stream()
                .filter(f -> JcPackageBlackList.RULE_ID.equals(f.getRuleId()))
                .anyMatch(f -> f.getMessage().contains("Dispatcher")));
    }

    @Test
    void testSilentNoBlacklistedReference(final JcTestRunner analysis) {
        analysis.analyse(NoBlacklistedReference.class,
                List.of(JcTestRunner.sig(NoBlacklistedReference.class, "install", "void",
                        INSTALL_PARAM_TYPES)));
        assertEquals(0, analysis.countReports(JcPackageBlackList.RULE_ID, NoBlacklistedReference.class));
    }

    @Test
    void testNoReportForBlacklistedApiClassesThemselves(final JcTestRunner analysis) {
        analysis.analyseAllClasses(
                NoBlacklistedReference.class,
                List.of(JcTestRunner.sig(NoBlacklistedReference.class, "install", "void",
                        INSTALL_PARAM_TYPES)),
                List.of(new JcPackageBlackList()));
        assertTrue(analysis.getFindings().stream()
                .filter(f -> JcPackageBlackList.RULE_ID.equals(f.getRuleId()))
                .noneMatch(f -> f.getClassSignature().startsWith("javacard.framework.service.")));
    }
}
