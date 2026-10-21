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

package com.airbus.esar.junit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javacard.security.RandomData;
import sootup.core.signatures.MethodSignature;
import com.airbus.esar.detect.AnalysisContextIf;
import com.airbus.esar.detect.ProgramVisitorIf;
import com.airbus.esar.messages.JavacardRuleMessages;
import com.airbus.esar.testing.ClassCodeSource;
import com.airbus.esar.testing.EsarAnalysisRunner;

public final class JcTestRunner extends EsarAnalysisRunner {
    /**
     * @param detectors visitors (often from EsarDetectorProviders)
     */
    public JcTestRunner(final List<? extends ProgramVisitorIf> detectors) {
        super(detectors, getJcApiJar());
    }

    /**
     * @return Directory that holds the host-stripped Java Card API JARs
     */
    private static List<Path> getJcApiJar() {
        final Path jar = ClassCodeSource.rootPath(RandomData.class);
        if (jar == null || !Files.isRegularFile(jar) || !jar.getFileName().toString().endsWith(".jar")) {
            return List.of();
        }
        final Path parent = jar.getParent();
        return parent != null ? List.of(parent) : List.of();
    }

    @Override
    protected List<String> ruleMessageBundleBaseNames() {
        return List.of(JavacardRuleMessages.class.getName());
    }

    /**
     * @param fixtureClass fixture bytecode to put on the classpath
     * @param entryPoints call-graph roots; empty skips call graph / ICFG
     * @return context from the prepared engine
     */
    public AnalysisContextIf analyze(final Class<?> fixtureClass, final List<MethodSignature> entryPoints) {
        return analyze(classPathFor(fixtureClass), entryPoints);
    }

    /**
     * @param fixtureClass fixture bytecode to put on the classpath
     * @param entryPoints call-graph roots; empty skips call graph / ICFG
     * @return context from the prepared engine
     */
    public AnalysisContextIf analyse(final Class<?> fixtureClass,
                                     final List<MethodSignature> entryPoints) {
        return analyze(classPathFor(fixtureClass), entryPoints);
    }

    /**
     * @param fixtureClass fixture bytecode to put on the classpath
     * @param entryPoints call-graph roots; empty skips call graph / ICFG
     * @param visitors detector instances
     * @return context from the prepared engine
     */
    public AnalysisContextIf analyse(final Class<?> fixtureClass,
                                     final List<MethodSignature> entryPoints,
                                     final Iterable<? extends ProgramVisitorIf> visitors) {
        return analyze(classPathFor(fixtureClass), entryPoints, visitors);
    }

    /**
     * @param fixtureClass fixture bytecode to put on the classpath
     * @param entryPoints call-graph roots; empty skips call graph / ICFG
     * @param visitors detector instances
     * @return context from the prepared engine
     */
    public AnalysisContextIf analyseAllClasses(final Class<?> fixtureClass,
                                               final List<MethodSignature> entryPoints,
                                               final Iterable<? extends ProgramVisitorIf> visitors) {
        return analyzeAllClasses(classPathFor(fixtureClass), entryPoints, visitors);
    }

    private static String sootDeclaringTypePrefix(final Class<?> clazz) {
        return "<" + clazz.getName() + ":";
    }
    public long countReports(final String ruleId, final Class<?> declaringClass) {
        final String prefix = sootDeclaringTypePrefix(declaringClass);

        return getFindings().stream()
            .filter(f -> ruleId.equals(f.getRuleId()))
            .filter(f -> f.getMethodSignature().orElse("").startsWith(prefix))
            .count();
    }
}
