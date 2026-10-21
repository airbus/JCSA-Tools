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

package com.airbus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;

@Command(
    name = "jcsa-stack-analyser",
    mixinStandardHelpOptions = true,
    version = "1.0.1-NB",
    description = "JCSA stack analyser",
    subcommands = {
        StackDepth.class,
        ClassCommand.class,
    }
)
public class Main implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        CommandLine.usage(this, System.out);
        return 0;
    }

    public static void main(final String[] args) {
        final int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}

class MethodSignatureConverter implements ITypeConverter<Method> {
    private static Method parseMethod(final String input) {
        final int openParen = input.indexOf('(');
        final int closeParen = input.lastIndexOf(')');

        if (openParen == -1 || closeParen == -1) {
            throw new IllegalArgumentException("Missing parenthesis in entry method");
        }

        final String preamble = input.substring(0, openParen).trim();

        final int lastSpace = preamble.lastIndexOf(' ');
        final String returnType = preamble.substring(0, lastSpace).trim();
        final String methodName = preamble.substring(lastSpace + 1).trim();

        final String paramString = input.substring(openParen + 1, closeParen);

        final List<String> params = new ArrayList<>();
        if (!paramString.isBlank()) {
            for (String p : paramString.split(",")) {
                params.add(p.trim());
            }
        }

        return new Method(returnType, methodName, params);
    }

    @Override
    public Method convert(final String value) {
        return parseMethod(value);
    }
}

@Command(
    name = "stack_depth",
    description = "Analyse program's stack depth"
)
class StackDepth implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StackDepth.class);
    @Option(names = {"-c", "--class-path"}, paramLabel = "class-path",
            description = "Path to class files (multiple can be defined)", required = true)
    private List<String> mClassPath;

    @Option(names = {"-a", "--applet"}, paramLabel = "applet",
            description = "Applet (or class) to analyse (example: com.airbus.BadApplet)", required = true)
    private String mApplet;

    @Option(names = {"-e", "--entry-method"}, paramLabel = "etry-method",
            defaultValue = "void process(javacard.framework.APDU)",
            description = "Entry method for analysis (example: void process(javacard.framework.APDU))",
            required = false, converter = MethodSignatureConverter.class)
    private Method mEntryMethod;

    @Option(names = {"-l", "--limit"}, paramLabel = "limit", defaultValue = "100",
            description = "Limit the amount of results (default: 100)", required = false)
    private int mLimit;

    @Option(names = {"--assert"}, paramLabel = "max-depth-in-bytes", defaultValue = "-1",
            description = "Assert that no stack size goes over this limit", required = false)
    private int mAssert;

    @Option(names = {"--jc-home"}, paramLabel = "jc-home",
            description = "Java Card home path by defaut ($JC_HOME_TOOLS)", required = false)
    private String mJcHome;

    @Option(names = {"--jc-version"}, paramLabel = "jc-version", defaultValue = "3.1.0",
            description = "Java Card target version", required = false)
    private String mJcVersion;

    private String mApiClassic;
    private String mApiClassicAnnotations;

    private boolean discoverJCDK() {
        final String homePath;
        if (mJcHome == null) {
            homePath = System.getenv("JC_HOME_TOOLS");
        } else {
            homePath = mJcHome;
        }
        if (homePath == null || homePath.isEmpty()) {
            System.err.println(
                "Java Card home must be defined either with parameter or $JC_HOME_TOOLS environment variable"
            );
            return false;
        }
        final Path basePath = Paths.get(homePath);

        final String apiClassic = String.format("api_classic-%s.jar", mJcVersion);
        final Path apiPath = Paths.get("lib", apiClassic);
        final Path fullApiPath = basePath.resolve(apiPath);

        final String apiClassicAnnotations = String.format("api_classic_annotations-%s.jar", mJcVersion);
        final Path apiAnnotationsPath = Paths.get("lib", apiClassicAnnotations);
        final Path fullApiAnnotationsPath = basePath.resolve(apiAnnotationsPath);

        if (!Files.exists(fullApiPath)) {
            System.err.printf("cannot find api jar: %s\n", fullApiPath.toString());
            return false;
        }

        if (!Files.exists(fullApiAnnotationsPath)) {
            System.err.printf("cannot find annotations api jar: %s\n", fullApiAnnotationsPath.toString());
            return false;
        }

        mApiClassic = fullApiPath.toString();
        mApiClassicAnnotations = fullApiAnnotationsPath.toString();

        return true;
    }

    private List<AnalysisInputLocation> buildInputLocations() {
        final List<AnalysisInputLocation> inputLocations = new ArrayList<>();
        // User supplied class paths
        for (final String classPath : mClassPath) {
            inputLocations.add(new JavaClassPathAnalysisInputLocation(classPath));
        }

        // Java Card Development Kit
        inputLocations.add(new JavaClassPathAnalysisInputLocation(mApiClassic));
        inputLocations.add(new JavaClassPathAnalysisInputLocation(mApiClassicAnnotations));

        return inputLocations;
    }

    private void printResults(final List<StackDepthInfo> stackDepthInfos) {
        for (int i = 0; i < stackDepthInfos.size(); i++) {
            if (i == mLimit) {
                break;
            }
            System.out.println("\n" + stackDepthInfos.get(i));
        }
    }

    private int assertResults(final List<StackDepthInfo> stackDepthInfos) {
        final List<StackDepthInfo> failedStacks = new ArrayList<>();

        for (final StackDepthInfo info : stackDepthInfos) {
            if (info.getTotalDepthBytes() <= mAssert) {
                break;
            }
            failedStacks.add(info);
        }

        if (failedStacks.isEmpty()) {
            System.err.println("OK");
            return 0;
        }

        System.err.println("Assert failed:");
        for (final StackDepthInfo info : failedStacks) {
            System.err.println("\n" + info);
        }
        return 1;
    }

    public Integer call() {
        if (!discoverJCDK()) {
            return 1;
        }

        final List<AnalysisInputLocation> inputLocations = buildInputLocations();

        System.out.println("Building call graph...");
        final CallGraphBuilder builder = new CallGraphBuilder(inputLocations);
        final CallGraphNode root = builder.buildCallGraph(mApplet, mEntryMethod);
        try {
            System.out.println("Analysing maximum stack depth...");
            final JCVMStackAnalyser analyser = new JCVMStackAnalyser(mClassPath, inputLocations);
            final List<StackDepthInfo> stackDepthInfos = analyser.analyseCallGraph(root);
            if (mAssert == -1) {
                printResults(stackDepthInfos);
            } else {
                return assertResults(stackDepthInfos);
            }
        } catch (IOException e) {
            LOGGER.error("could not analyse applet \"{}\"", mApplet, e);
            return 1;
        }

        return 0;
    }
}

@Command(
    name = "class",
    description = "Analyse class' method stack"
)
class ClassCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassCommand.class);
    @Option(names = {"-c", "--class"}, paramLabel = "class-path",
            description = "Path to the .class file", required = true)
    private String mClassPath;

    @Option(names = {"-m", "--method"}, paramLabel = "method-name",
            description = "Name of the method to analyser", required = false)
    private String mMethodName;

    @Option(names = {"-v", "--verbose"}, description = "More detailed analysis", required = false)
    private boolean mVerbose;

    @Override
    public Integer call() {
        try {
            final JCVMStackAnalyser analyser = new JCVMStackAnalyser(List.of(""));
            final List<MethodStackInfo> results = analyser.analyseClass(mClassPath, Optional.ofNullable(mMethodName));

            System.out.println("Analysis for \"" + mClassPath + "\"");

            if (results.isEmpty()) {
                System.out.println("no results");
                return 1;
            }
            for (final MethodStackInfo method : results) {
                System.out.println("\n" + method.toString());

                if (mVerbose) {
                    System.out.println("\n" + method.localsToString());
                }
            }
        } catch (IOException e) {
            LOGGER.error("could not analyse class \"{}\"", mClassPath, e);
            return 1;
        }
        return 0;
    }
}
