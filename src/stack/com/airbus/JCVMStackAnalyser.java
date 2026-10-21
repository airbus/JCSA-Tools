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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sootup.core.inputlocation.AnalysisInputLocation;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class JCVMStackAnalyser {
    private static final Logger LOGGER = LoggerFactory.getLogger(JCVMStackAnalyser.class);

    public static final int WORD_SIZE = 2;
    public static final int METHOD_OVERHEAD_COSMO = 3;
    public static final String CLASS_EXTENSION = ".class";

    private final List<Path> mClassPath;
    private final List<String> mPackages;
    private final HashMap<String, List<MethodStackInfo>> mAnalysedClasses;

    public JCVMStackAnalyser(final List<String> classPath) throws IOException {
        this(classPath, null);
    }

    public JCVMStackAnalyser(final List<String> classPath,
                             final List<AnalysisInputLocation> inputLocations) throws IOException {
        mClassPath = new ArrayList<>();
        mPackages = new ArrayList<>();
        mAnalysedClasses = new HashMap<>();

        for (final String path : classPath) {
            mClassPath.add(Path.of(path));
        }
        exploreClassPath();
    }

    private static class CallSiteAnalysisResult {
        private final int mMaxStack;
        private final Map<String, Integer> mCallSiteStackDepths;

        CallSiteAnalysisResult(final int maxStack, final Map<String, Integer> callSiteStackDepths) {
            this.mMaxStack = maxStack;
            this.mCallSiteStackDepths = callSiteStackDepths;
        }
    }

    private void exploreClassPath() throws IOException {
        for (final Path path : mClassPath) {
            // Skip CWD
            if (path.toString().equals("")) {
                continue;
            }

            try (Stream<Path> stream = Files.walk(path)) {
                stream
                    .filter(Files::isDirectory)
                    .filter(p -> !p.equals(path))
                    .map(path::relativize)
                    .map(p -> p.toString().replace(File.separator, "/"))
                    .forEach(mPackages::add);
            }
        }
    }

    public List<MethodStackInfo> analyseClass(final String classPath, final Optional<String> methodName)
            throws IOException {
        FileInputStream inputStream = null;
        for (final Path basePath : mClassPath) {
            try {
                inputStream = new FileInputStream(basePath.resolve(classPath).toString());
            } catch (IOException e) {
                continue;
            }
        }
        if (inputStream == null) {
            throw new IllegalArgumentException("Cannot find class \"" + classPath + "\" from class path");
        }

        final ClassReader classReader = new ClassReader(inputStream);
        final ClassNode classNode = new ClassNode();

        // Parse the class file
        classReader.accept(classNode, 0);

        final ArrayList<MethodStackInfo> stackInfo = new ArrayList<>();
        // Analyze each method
        for (final MethodNode method : classNode.methods) {
            if (methodName.isPresent() && !methodName.get().equals(method.name)) {
                continue;
            }
            final MethodStackInfo info = analyseMethod(classNode.name, method);
            stackInfo.add(info);
        }

        return stackInfo;
    }

    private List<String> getMethodParameters(final MethodNode method) {
        final Type[] argumentTypes = Type.getArgumentTypes(method.desc);
        return Arrays.stream(argumentTypes)
            .map(Type::getClassName)
            .collect(Collectors.toList());
    }

    private MethodStackInfo analyseMethod(final String ownerClass, final MethodNode methodNode) {
        if (methodNode.localVariables == null && methodNode.maxLocals != 0) {
            throw new RuntimeException("debug info needed for stack size analysis " + methodNode.name);
        }
        final ArrayList<LocalInfo> localsInfo = new ArrayList<>();

        int maxLocals = 0;
        int parametersSize = 0;
        if (methodNode.maxLocals != 0) {
            for (LocalVariableNode local : methodNode.localVariables) {
                if (local.name.equals("this")) {
                    parametersSize += 1;
                }
                if (local.desc.equals("I")) {
                    // Int takes two words
                    maxLocals += 2;
                    localsInfo.add(new LocalInfo(local.name, 2));
                } else {
                    maxLocals += 1;
                    localsInfo.add(new LocalInfo(local.name, 1));
                }
            }
        }
        final Type[] params = Type.getArgumentTypes(methodNode.desc);
        for (final Type param : params) {
            if (param.getClassName().equals(Integer.TYPE.getCanonicalName())) {
                // Int takes two words
                parametersSize += 2;
            } else {
                parametersSize += 1;
            }
        }

        final String methodType = Type.getReturnType(methodNode.desc).getClassName();
        final Method method = new Method(methodType, methodNode.name, getMethodParameters(methodNode));

        final CallSiteAnalysisResult analysisResult = computeAccurateStackDepthWithCallSites(ownerClass, methodNode);

        return new MethodStackInfo(
            method,
            analysisResult.mMaxStack,
            maxLocals,
            localsInfo,
            parametersSize,
            analysisResult.mCallSiteStackDepths
        );
    }

    private CallSiteAnalysisResult computeAccurateStackDepthWithCallSites(final String ownerClass,
                                                                          final MethodNode methodNode) {
        try {
            final Analyzer<BasicValue> analyzer = new Analyzer<>(new BasicInterpreter());
            final Frame<BasicValue>[] frames = analyzer.analyze(ownerClass, methodNode);

            // Find the maximum stack depth across all frames (all execution paths)
            final int maxStackDepth = Arrays.stream(frames)
                .filter(Objects::nonNull)
                .mapToInt(Frame::getStackSize)
                .max()
                .orElse(0);

            // Track stack depth at each method call site
            final Map<String, Integer> callSiteStackDepths = new HashMap<>();
            final InsnList instructions = methodNode.instructions;

            for (int i = 0; i < instructions.size(); i++) {
                final AbstractInsnNode insn = instructions.get(i);

                // Check if this is a method invocation instruction
                if (insn instanceof MethodInsnNode) {
                    final MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    final Frame<BasicValue> frame = frames[i];

                    if (frame != null) {
                        final int stackDepthAtCallSite = frame.getStackSize();

                        final String calleeKey = createMethodKeyFromInsn(methodInsn);

                        callSiteStackDepths.merge(calleeKey, stackDepthAtCallSite, Math::max);
                    }
                }
            }

            return new CallSiteAnalysisResult(maxStackDepth, callSiteStackDepths);
        } catch (AnalyzerException e) {
            System.err.println("Warning: Control flow analysis failed for " + ownerClass + "." + methodNode.name
                + ", using maxStack metadata: " + e.getMessage());
            return new CallSiteAnalysisResult(methodNode.maxStack, new HashMap<>());
        }
    }

    private String createMethodKeyFromInsn(final MethodInsnNode methodInsn) {
        // Convert JVM descriptor to Method.toString() format
        final Type returnType = Type.getReturnType(methodInsn.desc);
        final Type[] argTypes = Type.getArgumentTypes(methodInsn.desc);

        final String params = Arrays.stream(argTypes)
            .map(Type::getClassName)
            .collect(Collectors.joining(", "));

        return returnType.getClassName() + " " + methodInsn.name + "(" + params + ")";
    }

    private boolean isInClassPath(final String classPath) {
        for (final String pack : mPackages) {
            final int lastSlashIndex = classPath.lastIndexOf('/');

            if (lastSlashIndex == -1 && (pack.isEmpty() || pack.equals("."))) {
                return true;
            }

            final String fileDirectory = classPath.substring(0, lastSlashIndex);

            if (fileDirectory.equals(pack)) {
                return true;
            }
        }
        return false;
    }

    private Optional<MethodStackInfo> getMethodStackInfo(final Method method) {
        final List<MethodStackInfo> cachedResult = mAnalysedClasses.get(method.getClassPath());
        final List<MethodStackInfo> analysedClass;
        if (cachedResult == null) {
            try {
                analysedClass = analyseClass(method.getClassPath(), Optional.empty());
                mAnalysedClasses.put(method.getClassPath(), analysedClass);
            } catch (IOException e) {
                LOGGER.error("could not analyse \"{}\"", method, e);
                return Optional.empty();
            }
        } else {
            analysedClass = cachedResult;
        }

        return analysedClass.stream()
            .filter(info -> info.getMethod().matches(method))
            .findFirst();
    }

    private StackDepthInfo calculateStackDepth(final List<Method> path) {
        final List<MethodStackInfo> fullStack = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            final Method method = path.get(i);
            if (!isInClassPath(method.getClassPath())) {
                continue;
            }
            final Optional<MethodStackInfo> methodStackInfoOpt = getMethodStackInfo(method);
            if (methodStackInfoOpt.isEmpty()) {
                System.err.println("ERROR: could not analyse \"" + method + "\"");
                continue;
            }

            final MethodStackInfo methodStackInfo = methodStackInfoOpt.get();

            // Path-sensitive analysis: use call-site stack depth instead of method's max stack
            if (i < path.size() - 1) {
                final Method callee = path.get(i + 1);
                final int callSiteStackDepth = methodStackInfo.getStackDepthAtCallSite(callee.toString());
                fullStack.add(methodStackInfo.withMaxStack(callSiteStackDepth));
            } else {
                // Last method in path - use its max stack
                fullStack.add(methodStackInfo);
            }
        }
        return new StackDepthInfo(fullStack);
    }

    private boolean equalMethodStack(final List<MethodStackInfo> first, final List<MethodStackInfo> second) {
        if (first.size() != second.size()) {
            return false;
        }

        for (int i = 0; i < first.size(); i++) {
            if (!first.get(i).getMethod().matches(second.get(i).getMethod())) {
                return false;
            }
        }

        return true;
    }

    private boolean isDuplicateStackInfo(final StackDepthInfo stackDepthInfo,
                                         final List<StackDepthInfo> stackDepthInfos) {
        final List<MethodStackInfo> targetMethodStack = stackDepthInfo.getMethodStacks();
        final int targetSecondLast = targetMethodStack.size() - 2;

        for (final StackDepthInfo info : stackDepthInfos) {
            final List<MethodStackInfo> infoMethodStack = info.getMethodStacks();
            if (targetMethodStack.size() != infoMethodStack.size()) {
                continue;
            }

            if (targetMethodStack.size() == 1 && infoMethodStack.size() == 1) {
                if (targetMethodStack.get(0).getMethod().matches(infoMethodStack.get(0).getMethod())) {
                    return true;
                }
            } else {
                final int infoSecondLast = infoMethodStack.size() - 2;
                if (!targetMethodStack.get(targetSecondLast).getMethod()
                        .matches(infoMethodStack.get(infoSecondLast).getMethod())) {
                    return false;
                }
                if (equalMethodStack(targetMethodStack, infoMethodStack)) {
                    return true;
                }
            }
        }


        return false;
    }

    private void traverse(final CallGraphNode currentNode,
                          final List<Method> path,
                          final List<StackDepthInfo> stackDepthInfos) {
        path.add(currentNode.getSignature());

        final List<CallGraphNode> children = currentNode.getChildren();
        if (children.isEmpty()) {
            final StackDepthInfo stackDepthInfo = calculateStackDepth(path);
            if (!isDuplicateStackInfo(stackDepthInfo, stackDepthInfos)) {
                stackDepthInfos.add(stackDepthInfo);
            }
        } else {
            for (final CallGraphNode child : children) {
                traverse(child, new ArrayList<>(path), stackDepthInfos);
            }
        }
    }

    public List<StackDepthInfo> analyseCallGraph(final CallGraphNode callGraph) {
        final List<StackDepthInfo> stackDepthInfo = new ArrayList<>();

        traverse(callGraph, new ArrayList<>(), stackDepthInfo);

        stackDepthInfo.sort((o1, o2) -> Integer.compare(o2.getTotalDepth(), o1.getTotalDepth()));

        return stackDepthInfo;
    }
}
