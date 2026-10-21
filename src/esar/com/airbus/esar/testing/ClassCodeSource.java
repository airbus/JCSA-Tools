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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Classpath root (classes dir or jar) for a loaded class.
 */
public final class ClassCodeSource {
    private ClassCodeSource() {
    }

    /**
     * @param clazz any class loaded from the classpath entry to include
     * @return absolute path to the containing directory or JAR file
     */
    public static Path rootPath(final Class<?> clazz) {
        try {
            if (clazz.getProtectionDomain() != null && clazz.getProtectionDomain().getCodeSource() != null) {
                final URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
                if (location != null) {
                    return Paths.get(location.toURI()).toAbsolutePath().normalize();
                }
            }
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Bad code source URI for " + clazz.getName(), e);
        }
        return rootPathFromClassResource(clazz);
    }

    private static Path rootPathFromClassResource(final Class<?> clazz) {
        try {
            final String internalName = clazz.getName().replace('.', '/') + ".class";
            final ClassLoader loader =
                    clazz.getClassLoader() != null ? clazz.getClassLoader() : ClassLoader.getSystemClassLoader();
            final URL url = loader.getResource(internalName);
            if (url == null) {
                throw new IllegalStateException("Cannot resolve .class resource for " + clazz.getName());
            }
            if ("file".equals(url.getProtocol())) {
                final Path classFile = Paths.get(url.toURI()).toAbsolutePath().normalize();
                final String pkg = clazz.getPackageName();
                final int packageSegments = pkg.isEmpty() ? 0 : pkg.split("\\.").length;
                Path root = classFile;
                for (int i = 0; i < packageSegments + 1; i++) {
                    root = root.getParent();
                    if (root == null) {
                        throw new IllegalStateException("Cannot infer output root for " + clazz.getName());
                    }
                }
                return root;
            }
            if ("jar".equals(url.getProtocol())) {
                final String path = url.getPath();
                final int sep = path.indexOf('!');
                if (sep <= 0) {
                    throw new IllegalStateException("Unexpected jar URL: " + url);
                }
                final URI jarUri = URI.create(path.substring(0, sep));
                return Paths.get(jarUri).toAbsolutePath().normalize();
            }
            throw new IllegalStateException("Unsupported URL protocol for " + clazz.getName() + ": " + url);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
