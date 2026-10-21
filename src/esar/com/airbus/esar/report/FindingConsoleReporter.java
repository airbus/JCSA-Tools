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

package com.airbus.esar.report;

import java.util.List;
import java.util.Objects;

import com.airbus.esar.detect.EsarFinding;
import com.airbus.esar.detect.SourceRegion;

/**
 * Formats analysis findings as plain text and prints them to standard output.
 */
public final class FindingConsoleReporter {

    private final List<EsarFinding> mFindings;

    public FindingConsoleReporter(final List<EsarFinding> findings) {
        mFindings = List.copyOf(Objects.requireNonNull(findings, "findings"));
    }

    /**
     * @return human-readable report for all findings
     */
    public String toText() {
        if (mFindings.isEmpty()) {
            return "No findings.\n";
        }
        final StringBuilder out = new StringBuilder();
        for (int i = 0; i < mFindings.size(); i++) {
            if (i > 0) {
                out.append('\n');
            }
            appendFinding(out, mFindings.get(i));
        }

        out.append(mFindings.size()).append(mFindings.size() == 1 ? " finding\n" : " findings\n");
        return out.toString();
    }

    public void print() {
        System.out.print(toText());
    }

    private static void appendFinding(final StringBuilder out, final EsarFinding finding) {
        out.append('[').append(finding.getLevel()).append("] ");
        out.append('[').append(finding.getRuleId()).append("] ");
        out.append(finding.getClassSignature());

        finding.getRegion()
                .map(FindingConsoleReporter::formatRegion)
                .filter(s -> !s.isEmpty())
                .ifPresent(region -> out.append(':').append(region));
        out.append('\n');

        out.append(finding.getMessage()).append('\n');
    }

    private static String formatRegion(final SourceRegion region) {
        if (region.getFirstLine() <= 0) {
            return "";
        }
        final String linePart;
        if (region.getFirstLine() == region.getLastLine()) {
            linePart = Integer.toString(region.getFirstLine());
        } else {
            linePart = region.getFirstLine() + "-" + region.getLastLine();
        }
        if (!hasColumnInfo(region)) {
            return linePart;
        }
        if (region.getFirstLine() == region.getLastLine()) {
            if (region.getFirstCol() == region.getLastCol()) {
                return linePart + ":" + region.getFirstCol();
            }
            return linePart + ":" + region.getFirstCol() + "-" + region.getLastCol();
        }
        return linePart + ":" + region.getFirstCol() + "-" + region.getLastCol();
    }

    private static boolean hasColumnInfo(final SourceRegion region) {
        return region.getFirstCol() >= 0 && region.getLastCol() >= 0;
    }
}
