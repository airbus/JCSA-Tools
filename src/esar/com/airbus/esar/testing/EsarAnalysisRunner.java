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

package com.airbus.esar.testing;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.views.JavaView;
import com.airbus.esar.detect.AnalysisContextIf;
import com.airbus.esar.detect.EsarFinding;
import com.airbus.esar.detect.ProgramVisitorIf;
import com.airbus.esar.internal.engine.AnalysisEngine;
import com.airbus.esar.internal.engine.TraversalPackageBlacklist;
import com.airbus.esar.internal.i18n.DefaultRuleMessageResolver;
import com.airbus.esar.internal.i18n.RuleMessageResolverIf;
import com.airbus.esar.internal.i18n.UnsupportedRuleMessageResolver;
import com.airbus.esar.soot.SignatureUtils;

public class EsarAnalysisRunner {
    // Only traverse the entry classes
    public static final String SYSPROP_TRAVERSE_ENTRY_CLASSES_ONLY = "esar.test.traverseEntryOnly";

    private final List<ProgramVisitorIf> mDetectors;
    private List<EsarFinding> mFindingsLastRun = List.of();
    private final List<Path> mLibraryPaths;

    public EsarAnalysisRunner(final List<? extends ProgramVisitorIf> detectors,
                              final List<Path> libraryPaths) {
        mDetectors = List.copyOf(detectors);
        mLibraryPaths = libraryPaths;
    }

    public List<ProgramVisitorIf> detectors() {
        return mDetectors;
    }

    protected List<String> ruleMessageBundleBaseNames() {
        return List.of();
    }

    protected RuleMessageResolverIf createMessageResolver(final Iterable<? extends ProgramVisitorIf> visitors) {
        final List<String> bundles = ruleMessageBundleBaseNames();
        if (bundles.isEmpty()) {
            return UnsupportedRuleMessageResolver.INSTANCE;
        }
        final ClassLoader cl = classLoaderFor(visitors);
        return new DefaultRuleMessageResolver(cl, Locale.getDefault(), bundles);
    }

    private static ClassLoader classLoaderFor(final Iterable<? extends ProgramVisitorIf> visitors) {
        final Iterator<? extends ProgramVisitorIf> it = visitors.iterator();
        if (it.hasNext()) {
            return it.next().getClass().getClassLoader();
        }
        final ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        return ccl != null ? ccl : ClassLoader.getSystemClassLoader();
    }

    public List<EsarFinding> getFindings() {
        return mFindingsLastRun;
    }

    public AnalysisContextIf analyze(final List<AnalysisInputLocation> inputLocations,
                                     final List<MethodSignature> entryPoints) {
        return analyze(inputLocations, entryPoints, mDetectors);
    }

    public AnalysisContextIf analyze(final List<AnalysisInputLocation> inputLocations,
                                     final List<MethodSignature> entryPoints,
                                     final Iterable<? extends ProgramVisitorIf> visitors) {
        final RuleMessageResolverIf resolver = createMessageResolver(visitors);
        final Set<String> whitelist = traversalWhitelistFromEntryPoints(entryPoints);
        return runEngine(inputLocations, entryPoints, resolver, whitelist, visitors);
    }

    /**
     * Like {@link #analyze} but visits every class in the view (no entry-class whitelist).
     *
     * @param inputLocations Soot input locations
     * @param entryPoints call-graph roots; empty skips call graph / ICFG
     * @param visitors detector instances
     * @return context from the prepared engine
     */
    public AnalysisContextIf analyzeAllClasses(final List<AnalysisInputLocation> inputLocations,
                                               final List<MethodSignature> entryPoints,
                                               final Iterable<? extends ProgramVisitorIf> visitors) {
        final RuleMessageResolverIf resolver = createMessageResolver(visitors);
        return runEngine(inputLocations, entryPoints, resolver, null, visitors);
    }

    private AnalysisContextIf runEngine(final List<AnalysisInputLocation> inputLocations,
                                        final List<MethodSignature> entryPoints,
                                        final RuleMessageResolverIf resolver,
                                        final Set<String> traversalClassFqnWhitelist,
                                        final Iterable<? extends ProgramVisitorIf> visitors) {
        final AnalysisEngine engine;
        engine = new AnalysisEngine(
                inputLocations,
                entryPoints,
                resolver,
                traversalClassFqnWhitelist,
                TraversalPackageBlacklist.defaults());
        engine.prepare();
        engine.traverse(visitors);
        mFindingsLastRun = engine.getFindings();
        return engine.getContext();
    }

    /**
     * Distinct fully qualified class names of the given entry method signatures. A null or empty list yields an
     * empty set.
     *
     * @param entryPoints method signatures, or null or empty
     * @return distinct class names, or an empty set when the argument is null or empty
     */
    public static Set<String> declaringClassFqnsFromEntryPoints(final List<MethodSignature> entryPoints) {
        if (entryPoints == null || entryPoints.isEmpty()) {
            return Set.of();
        }
        return entryPoints.stream()
                .map(sig -> sig.getDeclClassType().getFullyQualifiedName())
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * @param entryPoints current run entry method signatures; may be null
     * @return the FQN whitelist for the analysis engine when the system property
     *     esar.test.traverseEntryClassesOnly is true and there is at least one entry point, otherwise null (no
     *     class filter)
     */
    public static Set<String> traversalWhitelistFromEntryPoints(final List<MethodSignature> entryPoints) {
        if (!Boolean.getBoolean(SYSPROP_TRAVERSE_ENTRY_CLASSES_ONLY)) {
            return null;
        }
        if (entryPoints == null || entryPoints.isEmpty()) {
            return null;
        }
        return declaringClassFqnsFromEntryPoints(entryPoints);
    }

    // Utils
    public static MethodSignature methodSignature(final Class<?> declaringClass,
                                                  final String methodName,
                                                  final String returnType,
                                                  final List<String> parameterTypes) {
        final JavaView view = new JavaView(toClassPath(declaringClass));
        return SignatureUtils.methodSignature(view, declaringClass, methodName, returnType, parameterTypes);
    }

    public static MethodSignature sig(final Class<?> c,
                                      final String name,
                                      final String ret,
                                      final List<String> params) {
        return methodSignature(c, name, ret, params);
    }

    public static List<MethodSignature> sigVoidNoArg(final Class<?> declaringClass,
                                                     final String... voidNoArgMethodNames) {
        final JavaView view = new JavaView(toClassPath(declaringClass));
        return SignatureUtils.sigVoidNoArg(view, declaringClass, voidNoArgMethodNames);
    }

    public static List<AnalysisInputLocation> toClassPath(final Class<?> fixtureClass) {
        return EsarAnalysisInput.of(fixtureClass).getInputLocations();
    }

    public List<AnalysisInputLocation> classPathFor(final Class<?> fixtureClass) {
        final EsarAnalysisInput cp = EsarAnalysisInput.empty();
        cp.addClassPath(fixtureClass);
        for (final Path libraryPath : mLibraryPaths) {
            if (libraryPath != null) {
                cp.addJarsInDirectory(libraryPath);
            }
        }
        return cp.getInputLocations();
    }
}
