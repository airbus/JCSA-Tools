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
import com.airbus.esar.fixture.ownerpin.OpcCheckAssignedToLocal;
import com.airbus.esar.fixture.ownerpin.OpcCheckIgnored;
import com.airbus.esar.fixture.ownerpin.OpcCheckIgnoredOnPinRef;
import com.airbus.esar.fixture.ownerpin.OpcCheckInIfCondition;
import com.airbus.esar.fixture.ownerpin.OpcCheckInvokeArg;
import com.airbus.esar.fixture.ownerpin.OpcCheckReturned;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcOwnerPinCheck {

    private static List<ProgramVisitorIf> ruleVisitors() {
        return List.of(new JcOwnerPinCheck());
    }

    private static final List<MethodSignature> OPC_CHECK_IGNORED = List.of(
            JcTestRunner.sig(OpcCheckIgnored.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> OPC_CHECK_IGNORED_ON_PIN_REF = List.of(
            JcTestRunner.sig(OpcCheckIgnoredOnPinRef.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> OPC_CHECK_ASSIGNED_TO_LOCAL = List.of(
            JcTestRunner.sig(OpcCheckAssignedToLocal.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> OPC_CHECK_IN_IF_CONDITION = List.of(
            JcTestRunner.sig(OpcCheckInIfCondition.class, "entry", "void", List.of("byte[]")));
    private static final List<MethodSignature> OPC_CHECK_RETURNED = List.of(
            JcTestRunner.sig(OpcCheckReturned.class, "entry", "boolean", List.of("byte[]")));
    private static final List<MethodSignature> OPC_CHECK_INVOKE_ARG = List.of(
            JcTestRunner.sig(OpcCheckInvokeArg.class, "entry", "void", List.of("byte[]")));

    @Test
    void testWarnsWhenCheckReturnIgnored(final JcTestRunner analysis) {
        analysis.analyse(OpcCheckIgnored.class, OPC_CHECK_IGNORED, ruleVisitors());
        assertEquals(1, analysis.countReports(JcOwnerPinCheck.RULE_ID, OpcCheckIgnored.class));
    }

    @Test
    void testWarnsWhenCheckReturnIgnoredOnPinRef(final JcTestRunner analysis) {
        analysis.analyse(OpcCheckIgnoredOnPinRef.class, OPC_CHECK_IGNORED_ON_PIN_REF, ruleVisitors());
        assertEquals(1, analysis.countReports(JcOwnerPinCheck.RULE_ID, OpcCheckIgnoredOnPinRef.class));
    }

    @Test
    void testSilentWhenCheckAssignedToLocal(final JcTestRunner analysis) {
        analysis.analyse(OpcCheckAssignedToLocal.class, OPC_CHECK_ASSIGNED_TO_LOCAL, ruleVisitors());
        assertEquals(0, analysis.countReports(JcOwnerPinCheck.RULE_ID, OpcCheckAssignedToLocal.class));
    }

    @Test
    void testSilentWhenCheckInIfCondition(final JcTestRunner analysis) {
        analysis.analyse(OpcCheckInIfCondition.class, OPC_CHECK_IN_IF_CONDITION, ruleVisitors());
        assertEquals(0, analysis.countReports(JcOwnerPinCheck.RULE_ID, OpcCheckInIfCondition.class));
    }

    @Test
    void testSilentWhenCheckReturned(final JcTestRunner analysis) {
        analysis.analyse(OpcCheckReturned.class, OPC_CHECK_RETURNED, ruleVisitors());
        assertEquals(0, analysis.countReports(JcOwnerPinCheck.RULE_ID, OpcCheckReturned.class));
    }

    @Test
    void testSilentWhenCheckInvokeArg(final JcTestRunner analysis) {
        analysis.analyse(OpcCheckInvokeArg.class, OPC_CHECK_INVOKE_ARG, ruleVisitors());
        assertEquals(0, analysis.countReports(JcOwnerPinCheck.RULE_ID, OpcCheckInvokeArg.class));
    }
}
