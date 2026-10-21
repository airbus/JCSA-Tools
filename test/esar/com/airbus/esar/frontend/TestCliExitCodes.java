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

package com.airbus.esar.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class TestCliExitCodes {

    @Test
    void testNoFindingsIsOk() {
        assertEquals(Cli.EXIT_OK, Cli.exitCodeForFindings(0, true));
        assertEquals(Cli.EXIT_OK, Cli.exitCodeForFindings(0, false));
    }

    @Test
    void testFindingsFailWhenEnabled() {
        assertEquals(Cli.EXIT_FINDINGS, Cli.exitCodeForFindings(1, true));
        assertEquals(Cli.EXIT_FINDINGS, Cli.exitCodeForFindings(12, true));
    }

    @Test
    void testFindingsOkWhenDisabled() {
        assertEquals(Cli.EXIT_OK, Cli.exitCodeForFindings(3, false));
    }

    @Test
    void testFailOnFindingDefaultsTrue() {
        final Configuration unset = Configuration.of(
                List.of("cp"),
                List.of(),
                List.of("void a.b()"),
                List.of(),
                List.of("plugin.jar"),
                null,
                null,
                null,
                null);
        assertTrue(unset.isFailOnFinding());
    }

    @Test
    void testFailOnFindingMergePrefersPrimary() {
        final Configuration cli = Configuration.of(
                null, null, null, null, null, null, null, null, null, Boolean.FALSE);
        final Configuration yaml = Configuration.of(
                null, null, null, null, null, null, null, null, null, Boolean.TRUE);
        assertFalse(Configuration.merge(cli, yaml).isFailOnFinding());
        assertTrue(Configuration.merge(yaml, cli).isFailOnFinding());
    }
}
