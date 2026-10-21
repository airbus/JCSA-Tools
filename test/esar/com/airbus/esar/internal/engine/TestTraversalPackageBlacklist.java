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

package com.airbus.esar.internal.engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class TestTraversalPackageBlacklist {

    @Test
    void testDefaultsMatchPlatformPackages() {
        final List<String> prefixes = TraversalPackageBlacklist.defaults();
        assertTrue(TraversalPackageBlacklist.matches("java.lang.String", prefixes));
        assertTrue(TraversalPackageBlacklist.matches("javax.microedition.io.Connector", prefixes));
        assertTrue(TraversalPackageBlacklist.matches("javacard.framework.Applet", prefixes));
        assertTrue(TraversalPackageBlacklist.matches("javacardx.crypto.Cipher", prefixes));
        assertTrue(TraversalPackageBlacklist.matches("com.sun.javacard.impl.PackageMgr", prefixes));
        assertTrue(TraversalPackageBlacklist.matches("sim.toolkit.ToolkitRegistry", prefixes));
        assertTrue(TraversalPackageBlacklist.matches("sim.access.SIMView", prefixes));
        assertTrue(TraversalPackageBlacklist.matches("uicc.access.FileView", prefixes));
        assertTrue(TraversalPackageBlacklist.matches("uicc.system.HandlerBuilder", prefixes));
        assertTrue(TraversalPackageBlacklist.matches("uicc.toolkit.ToolkitRegistry", prefixes));
        assertTrue(TraversalPackageBlacklist.matches("org.globalplatform.GPSystem", prefixes));
        assertTrue(TraversalPackageBlacklist.matches("visa.openplatform.OPSystem", prefixes));
        assertFalse(TraversalPackageBlacklist.matches("de.opentlssc.tls.Crypto_HMAC", prefixes));
        assertFalse(TraversalPackageBlacklist.matches("com.airbus.esar.fixture.comptime.Ok", prefixes));
    }

    @Test
    void testMergeDefaultsAddsExtras() {
        final List<String> prefixes = TraversalPackageBlacklist.mergeDefaults(List.of("com.vendor."));
        assertTrue(TraversalPackageBlacklist.matches("com.vendor.Lib", prefixes));
        assertTrue(TraversalPackageBlacklist.matches("java.lang.Object", prefixes));
    }

    @Test
    void testResolveWithoutDefaultsUsesOnlyConfigured() {
        final List<String> prefixes =
                TraversalPackageBlacklist.resolve(List.of("com.vendor."), false);
        assertTrue(TraversalPackageBlacklist.matches("com.vendor.Lib", prefixes));
        assertFalse(TraversalPackageBlacklist.matches("java.lang.Object", prefixes));
        assertFalse(TraversalPackageBlacklist.matches(
                "com.sun.javacard.crypto.ac",
                TraversalPackageBlacklist.resolve(null, false)));
    }
}
