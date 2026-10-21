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

// CHECKSTYLE.SUPPRESS: ParameterNumber for +1000 lines
public final class Configuration {
    private final List<String> mClassPath;
    private final List<String> mSourcePath;
    private final List<String> mEntryMethods;
    private final List<String> mEntryClasses;
    private final List<String> mPlugins;
    private final List<String> mTraversalPackageBlacklist;
    private final Boolean mIgnoreDefaultTraversalPackageBlacklist;
    private final String mRuleConfiguration;
    private final String mSarifOutput;
    private final Boolean mFailOnFinding;

    private Configuration(final List<String> classPath,
                          final List<String> sourcePath,
                          final List<String> entryMethods,
                          final List<String> entryClasses,
                          final List<String> plugins,
                          final List<String> traversalPackageBlacklist,
                          final Boolean ignoreDefaultTraversalPackageBlacklist,
                          final String ruleConfiguration,
                          final String sarifOutput,
                          final Boolean failOnFinding) {
        mClassPath = classPath;
        mSourcePath = sourcePath;
        mEntryMethods = entryMethods;
        mEntryClasses = entryClasses;
        mPlugins = plugins;
        mTraversalPackageBlacklist = traversalPackageBlacklist;
        mIgnoreDefaultTraversalPackageBlacklist = ignoreDefaultTraversalPackageBlacklist;
        mRuleConfiguration = ruleConfiguration;
        mSarifOutput = sarifOutput;
        mFailOnFinding = failOnFinding;
    }

    public static Configuration of(final List<String> classPath,
                                   final List<String> sourcePath,
                                   final List<String> entryMethods,
                                   final List<String> entryClasses,
                                   final List<String> plugins,
                                   final String ruleConfiguration,
                                   final String sarifOutput) {
        return of(classPath,
                sourcePath,
                entryMethods,
                entryClasses,
                plugins,
                null,
                null,
                ruleConfiguration,
                sarifOutput);
    }

    public static Configuration of(final List<String> classPath,
                                   final List<String> sourcePath,
                                   final List<String> entryMethods,
                                   final List<String> entryClasses,
                                   final List<String> plugins,
                                   final List<String> traversalPackageBlacklist,
                                   final String ruleConfiguration,
                                   final String sarifOutput) {
        return of(classPath,
                sourcePath,
                entryMethods,
                entryClasses,
                plugins,
                traversalPackageBlacklist,
                null,
                ruleConfiguration,
                sarifOutput);
    }

    public static Configuration of(final List<String> classPath,
                                   final List<String> sourcePath,
                                   final List<String> entryMethods,
                                   final List<String> entryClasses,
                                   final List<String> plugins,
                                   final List<String> traversalPackageBlacklist,
                                   final Boolean ignoreDefaultTraversalPackageBlacklist,
                                   final String ruleConfiguration,
                                   final String sarifOutput) {
        return of(classPath,
                sourcePath,
                entryMethods,
                entryClasses,
                plugins,
                traversalPackageBlacklist,
                ignoreDefaultTraversalPackageBlacklist,
                ruleConfiguration,
                sarifOutput,
                null);
    }

    public static Configuration of(final List<String> classPath,
                                   final List<String> sourcePath,
                                   final List<String> entryMethods,
                                   final List<String> entryClasses,
                                   final List<String> plugins,
                                   final List<String> traversalPackageBlacklist,
                                   final Boolean ignoreDefaultTraversalPackageBlacklist,
                                   final String ruleConfiguration,
                                   final String sarifOutput,
                                   final Boolean failOnFinding) {
        return new Configuration(
            classPath,
            sourcePath,
            entryMethods,
            entryClasses,
            plugins,
            traversalPackageBlacklist,
            ignoreDefaultTraversalPackageBlacklist,
            ruleConfiguration,
            sarifOutput,
            failOnFinding
        );
    }

    public static Configuration of(final YamlConfiguration configuration) {
        return new Configuration(
            configuration.getClassPath(),
            configuration.getSourcePath(),
            configuration.getEntryMethods(),
            configuration.getEntryClasses(),
            configuration.getPlugins(),
            configuration.getTraversalPackageBlacklist(),
            configuration.getIgnoreDefaultTraversalPackageBlacklist(),
            configuration.getRuleConfiguration(),
            configuration.getSarifOutput(),
            configuration.getFailOnFinding()
        );
    }

    public static Configuration merge(final Configuration primary, final Configuration secondary) {
        return Configuration.of(
        primary.getClassPath()         != null ? primary.getClassPath()         : secondary.getClassPath(),
        primary.getSourcePath()        != null ? primary.getSourcePath()        : secondary.getSourcePath(),
        primary.getEntryMethods()      != null ? primary.getEntryMethods()      : secondary.getEntryMethods(),
        primary.getEntryClasses()      != null ? primary.getEntryClasses()      : secondary.getEntryClasses(),
        primary.getPlugins()           != null ? primary.getPlugins()           : secondary.getPlugins(),
        primary.getTraversalPackageBlacklist() != null
                ? primary.getTraversalPackageBlacklist() : secondary.getTraversalPackageBlacklist(),
        primary.getIgnoreDefaultTraversalPackageBlacklist() != null
                ? primary.getIgnoreDefaultTraversalPackageBlacklist()
                : secondary.getIgnoreDefaultTraversalPackageBlacklist(),
        primary.getRuleConfiguration() != null ? primary.getRuleConfiguration() : secondary.getRuleConfiguration(),
        primary.getSarifOutput()       != null ? primary.getSarifOutput()       : secondary.getSarifOutput(),
        primary.getFailOnFinding()     != null ? primary.getFailOnFinding()     : secondary.getFailOnFinding()
        );
    }

    public List<String> getClassPath() {
        return mClassPath;
    }

    public List<String> getSourcePath() {
        return mSourcePath;
    }

    public List<String> getEntryMethods() {
        return mEntryMethods;
    }

    public List<String> getEntryClasses() {
        return mEntryClasses;
    }

    public List<String> getPlugins() {
        return mPlugins;
    }

    /**
     * Extra traversal package prefixes to exclude (merged with engine defaults at run time unless ignored).
     *
     * @return extra prefixes, or null when unset
     */
    public List<String> getTraversalPackageBlacklist() {
        return mTraversalPackageBlacklist;
    }

    /**
     * @return whether built-in traversal blacklist prefixes are skipped; null when unset
     */
    public Boolean getIgnoreDefaultTraversalPackageBlacklist() {
        return mIgnoreDefaultTraversalPackageBlacklist;
    }

    /**
     * @return true when built-in traversal blacklist prefixes must not be applied
     */
    public boolean isIgnoreDefaultTraversalPackageBlacklist() {
        return Boolean.TRUE.equals(mIgnoreDefaultTraversalPackageBlacklist);
    }

    public String getRuleConfiguration() {
        return mRuleConfiguration;
    }

    public String getSarifOutput() {
        return mSarifOutput;
    }

    /**
     * @return whether findings should fail the process; null when unset
     */
    public Boolean getFailOnFinding() {
        return mFailOnFinding;
    }

    /**
     * @return true when findings should fail the process; default true
     */
    public boolean isFailOnFinding() {
        return !Boolean.FALSE.equals(mFailOnFinding);
    }

    public boolean validate(final boolean onlyPlugins) {
        if (mPlugins == null || mPlugins.isEmpty()) {
            System.err.println("Plugins not defined");
            return false;
        }

        // Only check that plugins have been defined
        if (onlyPlugins) {
            return true;
        }

        if (mClassPath == null || mClassPath.isEmpty()) {
            System.err.println("Class path not defined");
            return false;
        }

        if (mSourcePath == null || mSourcePath.isEmpty()) {
            System.err.println("Warning: source path is empty");
        }

        final boolean hasEntryMethods = mEntryMethods != null && !mEntryMethods.isEmpty();
        final boolean hasEntryClasses = mEntryClasses != null && !mEntryClasses.isEmpty();
        if (!hasEntryMethods && !hasEntryClasses) {
            System.err.println("Entry methods or entry classes not defined");
            return false;
        }

        return true;
    }
}
