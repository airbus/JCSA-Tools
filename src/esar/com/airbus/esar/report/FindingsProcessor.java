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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.airbus.esar.detect.EsarFinding;
import com.airbus.esar.detect.SourceRegion;
import com.airbus.esar.frontend.Configuration;

public final class FindingsProcessor {
    private final List<EsarFinding> mFindings;
    private final FindingSuppressor mSuppressor;

    private static final Comparator<EsarFinding> BY_CLASS_THEN_LINE = Comparator
        .comparing(EsarFinding::getClassSignature)
        .thenComparingInt(FindingsProcessor::firstLine);

    public FindingsProcessor(final List<EsarFinding> findings, final Configuration configuration) {
        mFindings = Objects.requireNonNull(findings, "findings");
        Objects.requireNonNull(configuration, "configuration");
        mSuppressor = FindingSuppressor.fromSourcePaths(configuration.getSourcePath());
    }

    public List<EsarFinding> process() {
        return sortByClassAndLine(applySuppressions(mFindings));
    }

    private List<EsarFinding> applySuppressions(final List<EsarFinding> findings) {
        return findings.stream()
                .filter(f -> !mSuppressor.isSuppressed(f))
                .collect(Collectors.toList());
    }

    /**
     * @param findings findings to order (not modified)
     * @return copy sorted by class name, then first line ascending; findings without a region sort last
     */
    private static List<EsarFinding> sortByClassAndLine(final List<EsarFinding> findings) {
        final List<EsarFinding> sorted = new ArrayList<>(findings);
        sorted.sort(BY_CLASS_THEN_LINE);
        return sorted;
    }

    private static int firstLine(final EsarFinding finding) {
        return finding.getRegion().map(SourceRegion::getFirstLine).orElse(Integer.MAX_VALUE);
    }
}
