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

import java.util.List;

// CHECKSTYLE.SUPPRESS: MemberName for +1000 lines
public final class YamlConfiguration {
    private List<String> classPath;
    private List<String> sourcePath;
    private List<String> entryMethods;
    private List<String> entryClasses;
    private List<String> plugins;
    private List<String> traversalPackageBlacklist;
    private Boolean ignoreDefaultTraversalPackageBlacklist;
    private String ruleConfiguration;
    private String sarifOutput;
    private Boolean failOnFinding;

    public List<String> getClassPath() {
        return classPath;
    }

    public List<String> getSourcePath() {
        return sourcePath;
    }

    public List<String> getEntryMethods() {
        return entryMethods;
    }

    public List<String> getEntryClasses() {
        return entryClasses;
    }

    public List<String> getPlugins() {
        return plugins;
    }

    public List<String> getTraversalPackageBlacklist() {
        return traversalPackageBlacklist;
    }

    public Boolean getIgnoreDefaultTraversalPackageBlacklist() {
        return ignoreDefaultTraversalPackageBlacklist;
    }

    public String getRuleConfiguration() {
        return ruleConfiguration;
    }

    public String getSarifOutput() {
        return sarifOutput;
    }

    public Boolean getFailOnFinding() {
        return failOnFinding;
    }

    public void setClassPath(final List<String> value) {
        this.classPath = value;
    }

    public void setSourcePath(final List<String> value) {
        this.sourcePath = value;
    }

    public void setEntryMethods(final List<String> value) {
        this.entryMethods = value;
    }

    public void setEntryClasses(final List<String> value) {
        this.entryClasses = value;
    }

    public void setPlugins(final List<String> value) {
        this.plugins = value;
    }

    public void setTraversalPackageBlacklist(final List<String> value) {
        this.traversalPackageBlacklist = value;
    }

    public void setIgnoreDefaultTraversalPackageBlacklist(final Boolean value) {
        this.ignoreDefaultTraversalPackageBlacklist = value;
    }

    public void setRuleConfiguration(final String value) {
        this.ruleConfiguration = value;
    }

    public void setSarifOutput(final String value) {
        this.sarifOutput = value;
    }

    public void setFailOnFinding(final Boolean value) {
        this.failOnFinding = value;
    }
}
