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

import java.util.Objects;
import java.util.Optional;

/**
 * Tool metadata written into a SARIF log {@code runs[].tool.driver} block.
 */
public final class SarifRunMetadata {

    private static final String DEFAULT_TOOL_NAME = "ESAR";

    private final String mToolName;
    private final String mToolVersion;
    private final Optional<String> mInformationUri;

    private SarifRunMetadata(final String toolName,
                             final String toolVersion,
                             final Optional<String> informationUri) {
        mToolName = Objects.requireNonNull(toolName, "toolName");
        mToolVersion = Objects.requireNonNull(toolVersion, "toolVersion");
        mInformationUri = Objects.requireNonNull(informationUri, "informationUri");
    }

    /**
     * @return metadata with tool name {@code ESAR} and version from the JAR manifest when present
     */
    public static SarifRunMetadata defaults() {
        return of(DEFAULT_TOOL_NAME, readToolVersion(), null);
    }

    /**
     * @param toolName SARIF {@code tool.driver.name}
     * @param toolVersion SARIF {@code tool.driver.version}
     * @param informationUri optional SARIF {@code tool.driver.informationUri}; may be null
     * @return run metadata for SARIF export
     */
    public static SarifRunMetadata of(final String toolName,
                                      final String toolVersion,
                                      final String informationUri) {
        return new SarifRunMetadata(
                toolName,
                toolVersion,
                Optional.ofNullable(informationUri).filter(s -> !s.isBlank()));
    }

    public String getToolName() {
        return mToolName;
    }

    public String getToolVersion() {
        return mToolVersion;
    }

    public Optional<String> getInformationUri() {
        return mInformationUri;
    }

    private static String readToolVersion() {
        final Package pkg = SarifRunMetadata.class.getPackage();
        if (pkg == null) {
            return "unknown";
        }
        final String version = pkg.getImplementationVersion();
        if (version == null || version.isBlank()) {
            return "unknown";
        }
        return version;
    }
}
