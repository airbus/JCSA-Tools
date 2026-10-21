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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.airbus.esar.detect.EsarFinding;
import com.airbus.esar.detect.SourceRegion;

/**
 * Formats analysis findings as a SARIF 2.1.0 log and writes or prints JSON.
 */
public final class FindingSarifReporter {

    private static final String SCHEMA =
            "https://json.schemastore.org/sarif-2.1.0.json";
    private static final String VERSION = "2.1.0";

    private final List<EsarFinding> mFindings;
    private final List<String> mSourcePath;
    private final SarifRunMetadata mMetadata;
    private final boolean mPrettyPrint;
    private final List<String> mSourceFiles;

    private static final String SOURCE_FILE_EXTENSION = ".java";

    public FindingSarifReporter(final List<EsarFinding> findings, final List<String> sourcePath) {
        this(findings, sourcePath, SarifRunMetadata.defaults());
    }

    public FindingSarifReporter(final List<EsarFinding> findings,
                                final List<String> sourcePath,
                                final SarifRunMetadata metadata) {
        this(findings, sourcePath, metadata, false);
    }

    public FindingSarifReporter(final List<EsarFinding> findings,
                                final List<String> sourcePath,
                                final SarifRunMetadata metadata,
                                final boolean prettyPrint) {
        mFindings = List.copyOf(Objects.requireNonNull(findings, "findings"));
        mSourcePath = sourcePath == null ? List.of() : List.copyOf(sourcePath);
        mMetadata = Objects.requireNonNull(metadata, "metadata");
        mPrettyPrint = prettyPrint;
        mSourceFiles = new ArrayList<>();
        discoverSources();
    }

    public void discoverSources() {
        for (final String sourcePath : mSourcePath) {
            if (sourcePath == null || sourcePath.isBlank()) {
                continue;
            }
            final Path startPath = Paths.get(sourcePath);
            try (Stream<Path> stream = Files.walk(startPath)) {
                stream.filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(path -> path.endsWith(SOURCE_FILE_EXTENSION))
                    .forEach(path -> mSourceFiles.add(path));
            } catch (final IOException e) {
                System.err.println("cannot read sources: " + e.getMessage());
            }
        }
    }

    /**
     * @return SARIF 2.1.0 JSON document for all findings
     */
    public String toJson() {
        return createGson().toJson(buildLog());
    }

    /**
     * @param path output file; parent directories are created when missing
     *
     * @return true on success
     */
    public boolean write(final Path path) {
        try {
            Objects.requireNonNull(path, "path");
            final Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, toJson(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return false;
        }

        return true;
    }

    public void print() {
        System.out.print(toJson());
    }

    private Gson createGson() {
        if (mPrettyPrint) {
            return new GsonBuilder().setPrettyPrinting().create();
        }
        return new Gson();
    }

    private Map<String, Object> buildLog() {
        final Map<String, Object> log = new LinkedHashMap<>();
        log.put("$schema", SCHEMA);
        log.put("version", VERSION);
        log.put("runs", List.of(buildRun()));
        return log;
    }

    private Map<String, Object> buildRun() {
        final Map<String, RuleAccumulator> rules = new LinkedHashMap<>();
        final List<Map<String, Object>> results = new ArrayList<>();
        for (final EsarFinding finding : mFindings) {
            rules.computeIfAbsent(finding.getRuleId(), RuleAccumulator::new).observe(finding);
            results.add(buildResult(finding));
        }

        final Map<String, Object> run = new LinkedHashMap<>();
        run.put("tool", Map.of("driver", buildDriver(rules)));
        run.put("results", results);
        return run;
    }

    private Map<String, Object> buildDriver(final Map<String, RuleAccumulator> rules) {
        final Map<String, Object> driver = new LinkedHashMap<>();
        driver.put("name", mMetadata.getToolName());
        driver.put("version", mMetadata.getToolVersion());
        mMetadata.getInformationUri().ifPresent(uri -> driver.put("informationUri", uri));
        driver.put("rules", rules.values().stream().map(RuleAccumulator::toMap).toList());
        return driver;
    }

    private Map<String, Object> buildResult(final EsarFinding finding) {
        final Map<String, Object> result = new LinkedHashMap<>();
        result.put("ruleId", finding.getRuleId());
        result.put("level", finding.getLevel().toString());
        result.put("message", buildMessage(finding));
        result.put("locations", List.of(buildLocation(finding)));
        return result;
    }

    private static Map<String, Object> buildMessage(final EsarFinding finding) {
        final Map<String, Object> message = new LinkedHashMap<>();
        message.put("id", finding.getMessageId());
        message.put("text", finding.getMessage());
        return message;
    }

    private Map<String, Object> buildLocation(final EsarFinding finding) {
        final Map<String, Object> location = new LinkedHashMap<>();
        location.put("logicalLocations", List.of(buildLogicalLocation(finding)));
        buildPhysicalLocation(finding.getClassSignature(), finding.getRegion().orElse(null))
                .ifPresent(physical -> location.put("physicalLocation", physical));
        return location;
    }

    private static Map<String, Object> buildLogicalLocation(final EsarFinding finding) {
        final Map<String, Object> logical = new LinkedHashMap<>();
        logical.put("fullyQualifiedName", finding.getClassSignature());
        final java.util.Optional<String> methodSignature = finding.getMethodSignature();
        if (methodSignature.isPresent()) {
            logical.put("name", methodSignature.get());
            logical.put("kind", "member");
        } else {
            logical.put("kind", "type");
        }
        return logical;
    }

    private java.util.Optional<Map<String, Object>> buildPhysicalLocation(final String classFqn,
                                                                                 final SourceRegion region) {
        if (region == null || region.getFirstLine() <= 0) {
            return java.util.Optional.empty();
        }
        final Map<String, Object> physical = new LinkedHashMap<>();
        physical.put("artifactLocation", Map.of("uri", classFqnToSourceUri(classFqn)));
        physical.put("region", buildRegion(region));
        return java.util.Optional.of(physical);
    }

    private static Map<String, Object> buildRegion(final SourceRegion region) {
        final Map<String, Object> sarifRegion = new LinkedHashMap<>();
        sarifRegion.put("startLine", region.getFirstLine());
        sarifRegion.put("endLine", region.getLastLine() > 0 ? region.getLastLine() : region.getFirstLine());
        if (hasColumnInfo(region)) {
            sarifRegion.put("startColumn", region.getFirstCol() + 1);
            sarifRegion.put("endColumn", region.getLastCol() + 1);
        }
        return sarifRegion;
    }

    private static boolean hasColumnInfo(final SourceRegion region) {
        return region.getFirstCol() >= 0 && region.getLastCol() >= 0;
    }

    private String classFqnToSourceUri(final String classFqn) {
        final String targetSouceName;
        final int dollarIndex = classFqn.indexOf('$');
        if (dollarIndex == -1) {
            targetSouceName = classFqn.replace('.', '/') + SOURCE_FILE_EXTENSION;
        } else {
            targetSouceName = classFqn.substring(0, dollarIndex).replace('.', '/') + SOURCE_FILE_EXTENSION;
        }
        for (final String path : mSourceFiles) {
            if (path.endsWith(targetSouceName)) {
                return path;
            }
        }
        // Cannot find source file
        return targetSouceName;
    }

    private static final class RuleAccumulator {
        private final String mRuleId;
        private String mShortDescription;

        private RuleAccumulator(final String ruleId) {
            mRuleId = ruleId;
        }

        private void observe(final EsarFinding finding) {
            if (mShortDescription == null) {
                mShortDescription = finding.getMessage();
            }
        }

        private Map<String, Object> toMap() {
            final Map<String, Object> rule = new LinkedHashMap<>();
            rule.put("id", mRuleId);
            rule.put("name", mRuleId);
            if (mShortDescription != null) {
                rule.put("shortDescription", Map.of("text", mShortDescription));
            }
            return rule;
        }
    }
}
