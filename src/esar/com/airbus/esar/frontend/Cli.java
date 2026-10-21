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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Callable;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.MethodModifier;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;
import com.airbus.esar.EsarDetectorProvider;
import com.airbus.esar.config.RuleConfiguration;
import com.airbus.esar.detect.EsarFinding;
import com.airbus.esar.detect.ProgramVisitorIf;
import com.airbus.esar.internal.engine.AnalysisEngine;
import com.airbus.esar.internal.engine.DetectorTiming;
import com.airbus.esar.internal.engine.TraversalPackageBlacklist;
import com.airbus.esar.internal.i18n.DefaultRuleMessageResolver;
import com.airbus.esar.internal.util.EntryPointSpec;
import com.airbus.esar.report.FindingConsoleReporter;
import com.airbus.esar.report.FindingSarifReporter;
import com.airbus.esar.report.FindingsProcessor;
import com.airbus.esar.report.SarifRunMetadata;
import com.airbus.esar.testing.EsarAnalysisInput;

@Command(
    name = "ESAR",
    mixinStandardHelpOptions = true,
    version = Cli.VERSION,
    description = "Extendable SootUp Analysis Runner"
)
public class Cli implements Callable<Integer> {
    public static final String VERSION = "1.2.0-NB";

    public static final int EXIT_OK = 0;
    public static final int EXIT_SOFTWARE = 1;
    public static final int EXIT_USAGE = 2;
    public static final int EXIT_FINDINGS = 4;

    @Option(names = {"-cp", "--class-path"}, paramLabel = "class-path",
            description = "Path to class files", required = false)
    private List<String> mClassPath;
    @Option(names = {"-s", "--source"}, paramLabel = "source-path",
            description = "Path to source code", required = false)
    private List<String> mSourcePath;
    @Option(names = {"-e", "--entry-method"}, paramLabel = "etry-methods",
            description = "Entry method for analysis (example: void com.airbus.Test.process(javacard.framework.APDU))",
            required = false)
    private List<String> mEntryMethods;
    @Option(names = {"-ec", "--entry-class"}, paramLabel = "entry-class",
            description = "Entry class: every public concrete method becomes an entry point",
            required = false)
    private List<String> mEntryClasses;
    @Option(names = {"-p", "--plugin"}, paramLabel = "class-path",
            description = "Path to plugin jar", required = false)
    private List<String> mPlugins;
    @Option(names = {"--config"}, paramLabel = "config",
            description = "Path to ESAR configuration", required = false)
    private String mConfigurationFile;
    @Option(names = {"--rule-config"}, paramLabel = "rule-config",
            description = "Path to rule configuration", required = false)
    private String mRuleConfiguration;
    @Option(names = {"-v", "--verbose"}, description = "More detailed analysis", required = false)
    private boolean mVerbose;
    @Option(names = {"--sarif-output"}, paramLabel = "path",
            description = "SARIF output path", required = false)
    private String mSarifOutput;
    @Option(names = {"--traversal-package-blacklist"}, paramLabel = "prefix",
            description = "Package prefix to skip during traversal (merged with defaults)",
            required = false)
    private List<String> mTraversalPackageBlacklist;
    @Option(names = {"--no-default-traversal-package-blacklist"},
            description = "Ignore built-in traversal package blacklist "
                    + "(explicit --traversal-package-blacklist / YAML prefixes still apply)",
            required = false)
    private Boolean mNoDefaultTraversalPackageBlacklist;
    @Option(names = {"--dump-default-rule-configuration"},
            description = "Dump the default rule configuration YAML", required = false)
    private String mRuleConfDumpPath;
    @Option(names = {"--fail-on-finding"}, negatable = true,
            description = "Exit 4 when findings exist (default: true). --no-fail-on-finding exits 0",
            required = false)
    private Boolean mFailOnFinding;
    private Configuration mConfiguration = null;

    private static final SarifRunMetadata SARIF_RUN_METADATA = SarifRunMetadata.of("ESAR", VERSION, null);

    private void configureLogging() {
        if (!mVerbose) {
            System.setProperty(
                "org.slf4j.simpleLogger.log.sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG",
                "off");
        }
    }

    private Configuration parseYamlConfig(final String path) {
        if (path == null) {
            return null;
        }
        final LoaderOptions opts = new LoaderOptions();
        final Constructor constructor = new Constructor(YamlConfiguration.class, opts);

        final Yaml yaml = new Yaml(constructor);
        try (InputStream in = Files.newInputStream(Path.of(path))) {
            return Configuration.of(yaml.load(in));
        } catch (final IOException e) {
            System.err.println("Cannot read path \"" + path + "\"");
        }

        return null;
    }

    private Configuration createCliConfiguration() {
        return Configuration.of(
            mClassPath,
            mSourcePath,
            mEntryMethods,
            mEntryClasses,
            mPlugins,
            mTraversalPackageBlacklist,
            mNoDefaultTraversalPackageBlacklist,
            mRuleConfiguration,
            mSarifOutput,
            mFailOnFinding
        );
    }

    private static List<AnalysisInputLocation> buildInputLocations(final List<String> classPath) {
        final EsarAnalysisInput cp = EsarAnalysisInput.empty();

        for (final String location : classPath) {
            final Path path = Path.of(location);
            if (!Files.exists(path)) {
                System.err.println("Class path \"" + location + "\" does not exists");
                return null;
            }
            cp.addClassPath(path);
        }

        return cp.getInputLocations();
    }

    private static List<MethodSignature> buildEntryPoints(final List<AnalysisInputLocation> inputLocations,
                                                          final List<String> entryMethods,
                                                          final List<String> entryClasses) {
        if (inputLocations == null || inputLocations.isEmpty()) {
            return null;
        }

        final List<String> methods = entryMethods == null ? List.of() : entryMethods;
        final List<String> classes = entryClasses == null ? List.of() : entryClasses;
        if (methods.isEmpty() && classes.isEmpty()) {
            return null;
        }

        final JavaView view = new JavaView(inputLocations);
        final Set<MethodSignature> out = new LinkedHashSet<>();

        for (final String raw : methods) {
            if (raw == null || raw.isBlank()) {
                continue;
            }

            final EntryPointSpec spec = EntryPointSpec.parse(raw.trim());
            final ClassType classType = view.getIdentifierFactory().getClassType(spec.declaringClass());
            if (classType == null) {
                System.err.println("Cannot resolve entry method class \"" + spec.declaringClass() + "\"");
                return null;
            }

            out.add(view.getIdentifierFactory().getMethodSignature(
                classType,
                spec.methodName(),
                spec.returnType(),
                spec.parameterTypes())
            );
        }

        for (final String raw : classes) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            final String className = raw.trim();
            final ClassType classType = view.getIdentifierFactory().getClassType(className);
            final Optional<JavaSootClass> clazzOpt = view.getClass(classType);
            if (clazzOpt.isEmpty()) {
                System.err.println("Cannot resolve entry class \"" + className + "\"");
                return null;
            }
            for (final JavaSootMethod method : clazzOpt.get().getMethods()) {
                if (!method.isPublic() || !method.isConcrete() || !method.hasBody()) {
                    continue;
                }
                if (MethodModifier.isConstructor(method.getModifiers())) {
                    continue;
                }
                if ("<clinit>".equals(method.getName())) {
                    continue;
                }
                out.add(method.getSignature());
            }
        }

        if (out.isEmpty()) {
            return null;
        }
        return Collections.unmodifiableList(new ArrayList<>(out));
    }

    private static URL[] loadPlugins(final List<String> plugins) {
        final List<Path> jars = new ArrayList<>();

        for (final String jar : plugins) {
            final Path path = Path.of(jar);
            if (!Files.exists(path) || Files.isDirectory(path)) {
                System.err.println("Plugin \"" + jar + "\" does not exists");
                return null;
            }
            jars.add(path);
        }

        return jars.stream()
            .map(Path::toUri)
            .map(uri -> {
                try {
                    return uri.toURL();
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(uri.toString(), e);
                }
            })
            .toArray(URL[]::new);
    }

    private static DefaultRuleMessageResolver getMessageResolver(final URL[] urls) {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final List<String> bundleNames = new ArrayList<>();
        final URLClassLoader pluginLoader = new URLClassLoader("plugins", urls, cl);

        for (EsarDetectorProvider provider : ServiceLoader.load(EsarDetectorProvider.class, pluginLoader)) {
            bundleNames.addAll(provider.ruleMessageBundleNames());
        }

        if (bundleNames.isEmpty()) {
            System.err.println("failed to load bundle name (no plugins found)");
            return null;
        }

        return new DefaultRuleMessageResolver(pluginLoader, Locale.getDefault(), bundleNames);
    }

    private static List<ProgramVisitorIf> loadDetectors(final URL[] urls) {
        final List<ProgramVisitorIf> detectors = new ArrayList<>();
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final URLClassLoader pluginLoader = new URLClassLoader("plugins", urls, cl);

        for (EsarDetectorProvider provider : ServiceLoader.load(EsarDetectorProvider.class, pluginLoader)) {
            detectors.addAll(provider.detectors());
        }

        if (detectors.isEmpty()) {
            System.err.println("failed to load detectors");
            return null;
        }

        return detectors;
    }

    private static int startAnalysis(final List<AnalysisInputLocation> inputLocations,
                                     final List<MethodSignature> entryPoints,
                                     final Configuration configuration,
                                     final boolean verbose) {
        final URL[] plugins = loadPlugins(configuration.getPlugins());
        final DefaultRuleMessageResolver messageResolver = getMessageResolver(plugins);
        if (messageResolver == null) {
            return EXIT_SOFTWARE;
        }

        final List<String> packageBlacklist = TraversalPackageBlacklist.resolve(
                configuration.getTraversalPackageBlacklist(),
                !configuration.isIgnoreDefaultTraversalPackageBlacklist());
        final AnalysisEngine engine = new AnalysisEngine(
                inputLocations,
                entryPoints,
                messageResolver,
                null,
                packageBlacklist);
        final List<ProgramVisitorIf> detectors = loadDetectors(plugins);
        if (detectors == null) {
            return EXIT_SOFTWARE;
        }

        final RuleConfiguration ruleConfiguration =
                RuleConfiguration.fromPath(configuration.getRuleConfiguration());
        final List<ProgramVisitorIf> configuredDetectors = ruleConfiguration.configureDetectors(detectors);

        engine.prepare();
        engine.traverse(configuredDetectors, ruleConfiguration.severityOverrides());

        if (verbose) {
            printDetectorTimings(engine.getDetectorTimings());
        }

        final List<EsarFinding> findings =
                new FindingsProcessor(engine.getFindings(), configuration).process();
        new FindingConsoleReporter(findings).print();

        final FindingSarifReporter sarifReporter = new FindingSarifReporter(
            findings,
            configuration.getSourcePath(),
            SARIF_RUN_METADATA
        );

        if (configuration.getSarifOutput() != null) {
            if (!sarifReporter.write(Path.of(configuration.getSarifOutput()))) {
                System.err.println("failed to write sarif output");
            }
        }

        return exitCodeForFindings(findings.size(), configuration.isFailOnFinding());
    }

    static int exitCodeForFindings(final int findingCount, final boolean failOnFinding) {
        if (findingCount > 0 && failOnFinding) {
            return EXIT_FINDINGS;
        }
        return EXIT_OK;
    }

    private static void printDetectorTimings(final List<DetectorTiming> timings) {
        System.err.println("Detector CPU time (initAnalysis + traverse), sorted by total:");
        long totalNanos = 0L;
        for (final DetectorTiming timing : timings) {
            System.err.println("  " + timing.formatLine());
            totalNanos += timing.getTotalCpuNanos();
        }
        System.err.printf(
                Locale.ROOT,
                "  %-40s total=%8.1f ms%n",
                "ALL DETECTORS",
                totalNanos / 1_000_000.0);
    }

    private void dumpDefaultRuleConfiguration(final Configuration configuration, final String dumpPath) {
        final URL[] plugins = loadPlugins(configuration.getPlugins());

        final List<ProgramVisitorIf> detectors = loadDetectors(plugins);
        if (detectors == null) {
            System.err.println("cannot load detectors");
        }

        try {
            RuleConfiguration.dumpDefaults(detectors, Path.of(dumpPath));
        } catch (final IOException e) {
            System.err.println(e.getMessage());
            return;
        }
    }

    @Override
    public Integer call() throws Exception {
        configureLogging();

        final Configuration yamlConfiguration = parseYamlConfig(mConfigurationFile);
        final Configuration cliConfiguration = createCliConfiguration();

        if (yamlConfiguration == null) {
            mConfiguration = cliConfiguration;
        } else {
            mConfiguration = Configuration.merge(cliConfiguration, yamlConfiguration);
        }

        final boolean dumpDefaultConf = mRuleConfDumpPath != null;
        if (!mConfiguration.validate(dumpDefaultConf)) {
            CommandLine.usage(this, System.out);
            return EXIT_USAGE;
        }

        if (dumpDefaultConf) {
            dumpDefaultRuleConfiguration(mConfiguration, mRuleConfDumpPath);
            return EXIT_OK;
        }

        final List<AnalysisInputLocation> inputLocations = buildInputLocations(mConfiguration.getClassPath());
        if (inputLocations == null) {
            System.err.println("no input locations");
            return EXIT_USAGE;
        }
        final List<MethodSignature> entryPoints =
            buildEntryPoints(
                inputLocations,
                mConfiguration.getEntryMethods(),
                mConfiguration.getEntryClasses());
        if (entryPoints == null) {
            System.err.println("no entry points");
            return EXIT_USAGE;
        }

        final int analysisResult = startAnalysis(
            inputLocations,
            entryPoints,
            mConfiguration,
            mVerbose
        );
        if (analysisResult == EXIT_SOFTWARE) {
            System.err.println("analysis failed");
        }
        return analysisResult;
    }
}

