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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for plain data / support classes in {@code com.airbus.esar.detect}:
 * {@link BugReport}, {@link SourceRegion}, {@link SarifResultLevel}, and the default
 * {@link SharedSolverRegistryIf#getRequired} method.
 */
class TestDetectDataClasses {

    // ------------------------------------------------------------------ BugReport

    @Test
    void testBugReportGetters() {
        final SourceRegion region = new SourceRegion(1, 2, 3, 4);
        final BugReport report = new BugReport(
                "MY_RULE",
                SarifResultLevel.WARNING,
                "some message",
                Optional.of("<SomeClass: void m()>"),
                Optional.of(region));

        assertEquals("MY_RULE", report.getRuleId());
        assertSame(SarifResultLevel.WARNING, report.getLevel());
        assertEquals("some message", report.getMessage());
        assertEquals(Optional.of("<SomeClass: void m()>"), report.getMethodSignature());
        assertEquals(Optional.of(region), report.getRegion());
    }

    // ------------------------------------------------------------------ SourceRegion

    @Test
    void testSourceRegionGetters() {
        final SourceRegion r = new SourceRegion(10, 20, 3, 7);
        assertEquals(10, r.getFirstLine());
        assertEquals(20, r.getLastLine());
        assertEquals(3, r.getFirstCol());
        assertEquals(7, r.getLastCol());
    }

    // ------------------------------------------------------------------ SarifResultLevel

    @Test
    void testSarifResultLevelToString() {
        assertEquals("none", SarifResultLevel.NONE.toString());
        assertEquals("note", SarifResultLevel.NOTE.toString());
        assertEquals("warning", SarifResultLevel.WARNING.toString());
        assertEquals("error", SarifResultLevel.ERROR.toString());
    }

    // ------------------------------------------------------------------ EsarFinding

    @Test
    void testEsarFindingGetters() {
        final SourceRegion region = new SourceRegion(5, 6, 0, 10);
        final EsarFinding finding = new EsarFinding(
                "SOME_RULE",
                SarifResultLevel.NOTE,
                "some.message.id",
                "human-readable text",
                "test",
                Optional.of("<C: void m()>"),
                Optional.of(region));

        assertEquals("SOME_RULE", finding.getRuleId());
        assertSame(SarifResultLevel.NOTE, finding.getLevel());
        assertEquals("some.message.id", finding.getMessageId());
        assertEquals("human-readable text", finding.getMessage());
        assertEquals("test", finding.getClassSignature());
        assertEquals(Optional.of("<C: void m()>"), finding.getMethodSignature());
        assertEquals(Optional.of(region), finding.getRegion());
    }

    // ------------------------------------------------------------------ SharedSolverRegistryIf

    @Test
    void testSharedSolverRegistryIfGetRequiredThrowsWhenAbsent() {
        final SharedSolverKey<String> key = SharedSolverKey.of("missing-key");
        final SharedSolverRegistryIf registry = new SharedSolverRegistryIf() {
            @Override
            public <D> D lazyRegister(final SharedSolverKey<D> k, final Supplier<D> createIfAbsent) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <D> Optional<D> get(final SharedSolverKey<D> k) {
                return Optional.empty();
            }
        };

        final IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> registry.getRequired(key));
        assertEquals("No shared solver registered for key: missing-key", ex.getMessage());
    }
}
