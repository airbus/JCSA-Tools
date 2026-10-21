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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.frontend.inputlocation.JavaSourcePathAnalysisInputLocation;

public final class EsarAnalysisInput {
    private final List<AnalysisInputLocation> mRoots;

    private EsarAnalysisInput(final List<AnalysisInputLocation> roots) {
        mRoots = roots;
    }

    public static EsarAnalysisInput empty() {
        return new EsarAnalysisInput(new ArrayList<>());
    }

    public static EsarAnalysisInput of(final Class<?> clazz) {
        return empty().addClassPath(clazz);
    }

    /**
     * Adds the classpath root that contains the given loaded class (output directory or JAR), deduplicated.
     *
     * @param clazz any class from that root (e.g. a fixture or library type)
     * @return this instance for chaining
     */
    public EsarAnalysisInput addClassPath(final Class<?> clazz) {
        addRootClassPath(ClassCodeSource.rootPath(clazz));
        return this;
    }

    public EsarAnalysisInput addClassPath(final Path path) {
        addRootClassPath(path);
        return this;
    }

    /**
     * NOTE: source paths not really supported right now
     *
     * @param path path to sources
     *
     * @return EsarAnalysisInput
     */
    public EsarAnalysisInput addSourcePath(final Path path) {
        addRootSourcePath(path);
        return this;
    }

    /**
     * Adds every {@code *.jar} in the given directory (non-recursive).
     *
     * @param directory directory to scan
     * @return this instance for chaining
     */
    public EsarAnalysisInput addJarsInDirectory(final Path directory) {
        final Path dir = requireExists(directory);
        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".jar")).sorted().forEach(this::addRootClassPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public List<AnalysisInputLocation> getInputLocations() {
        return List.copyOf(mRoots);
    }

    private void addRootClassPath(final Path path) {
        mRoots.add(new JavaClassPathAnalysisInputLocation(path.toAbsolutePath().normalize().toString()));
    }

    private void addRootSourcePath(final Path path) {
        mRoots.add(new JavaSourcePathAnalysisInputLocation(path.toAbsolutePath().normalize().toString()));
    }

    private static Path requireExists(final Path path) {
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Classpath entry does not exist: " + path);
        }
        return path;
    }
}
