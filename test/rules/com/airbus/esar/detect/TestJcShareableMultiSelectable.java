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
import com.airbus.esar.fixture.shareable.NonAppletShareable;
import com.airbus.esar.fixture.shareable.PlainAppletNoShareable;
import com.airbus.esar.fixture.shareable.ShareableAppletNotMultiSelectable;
import com.airbus.esar.fixture.shareable.ShareableAppletWithMultiSelectable;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcShareableMultiSelectable {

    private static final List<String> INSTALL_PARAM_TYPES = List.of("byte[]", "short", "byte");

    private static final List<MethodSignature> ENTRY_SHAREABLE_NO_MULTI = List.of(
            JcTestRunner.sig(ShareableAppletNotMultiSelectable.class, "install", "void",
                    INSTALL_PARAM_TYPES));

    private static final List<MethodSignature> ENTRY_SHAREABLE_WITH_MULTI = List.of(
            JcTestRunner.sig(ShareableAppletWithMultiSelectable.class, "install", "void",
                    INSTALL_PARAM_TYPES));

    private static final List<MethodSignature> ENTRY_PLAIN_APPLET = List.of(
            JcTestRunner.sig(PlainAppletNoShareable.class, "install", "void",
                    INSTALL_PARAM_TYPES));

    private static final List<MethodSignature> ENTRY_NON_APPLET = List.of(
            JcTestRunner.sig(NonAppletShareable.class, "getShareableInterfaceObject",
                    "java.lang.Object", List.of("java.lang.Object", "byte")));

    @Test
    void testWarnsShareableWithoutMultiSelectable(final JcTestRunner analysis) {
        analysis.analyse(ShareableAppletNotMultiSelectable.class, ENTRY_SHAREABLE_NO_MULTI);
        assertEquals(1, analysis.countReports(JcShareableMultiSelectable.RULE_ID,
                ShareableAppletNotMultiSelectable.class));
    }

    @Test
    void testSilentShareableWithMultiSelectable(final JcTestRunner analysis) {
        analysis.analyse(ShareableAppletWithMultiSelectable.class, ENTRY_SHAREABLE_WITH_MULTI);
        assertEquals(0, analysis.countReports(JcShareableMultiSelectable.RULE_ID,
                ShareableAppletWithMultiSelectable.class));
    }

    @Test
    void testSilentPlainApplet(final JcTestRunner analysis) {
        analysis.analyse(PlainAppletNoShareable.class, ENTRY_PLAIN_APPLET);
        assertEquals(0, analysis.countReports(JcShareableMultiSelectable.RULE_ID,
                PlainAppletNoShareable.class));
    }

    @Test
    void testSilentNonAppletShareable(final JcTestRunner analysis) {
        analysis.analyse(NonAppletShareable.class, ENTRY_NON_APPLET);
        assertEquals(0, analysis.countReports(JcShareableMultiSelectable.RULE_ID,
                NonAppletShareable.class));
    }
}
