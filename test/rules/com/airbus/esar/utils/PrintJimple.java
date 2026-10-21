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

package com.airbus.esar.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import javacard.security.RandomData;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.Local;
import sootup.core.model.Body;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;
import com.airbus.esar.testing.ClassCodeSource;
import com.airbus.esar.testing.EsarAnalysisInput;

/**
 * Prints SootUp Jimple for every concrete method in one or more classes.
 * <p>
 * The SootUp classpath is set up the same way as the rule tests: the class's own
 * bytecode root plus the Java Card API jars (when available on the JVM classpath).
 * <p>
 * Usage: {@code ant print-jimple -Dclasses=com.airbus.esar.fixture.implicitwiden.IbwTernaryWidenToInt}
 */
public final class PrintJimple {

    private PrintJimple() {
    }

    public static void main(final String[] args) throws ClassNotFoundException {
        if (args.length == 0) {
            System.err.println("Usage: PrintJimple <ClassName> [<ClassName>...]");
            System.exit(1);
        }
        for (final String className : args) {
            printClass(className);
        }
    }

    private static void printClass(final String className) throws ClassNotFoundException {
        final Class<?> clazz = Class.forName(className);
        final EsarAnalysisInput cp = EsarAnalysisInput.of(clazz);
        addJcApiJars(cp);
        final List<AnalysisInputLocation> locations = cp.getInputLocations();
        final JavaView view = new JavaView(locations);
        final JavaSootClass sootClass = view
                .getClass(view.getIdentifierFactory().getClassType(className))
                .orElseThrow(() -> new IllegalArgumentException("Class not found in view: " + className));

        System.out.println("=== " + className + " ===");
        sootClass.getMethods().stream()
                .filter(JavaSootMethod::isConcrete)
                .filter(JavaSootMethod::hasBody)
                .sorted(Comparator.comparing(m -> m.getSignature().toString()))
                .forEach(PrintJimple::printMethod);
    }

    private static void addJcApiJars(final EsarAnalysisInput cp) {
        final Path jar = ClassCodeSource.rootPath(RandomData.class);
        if (jar == null || !Files.isRegularFile(jar)) {
            return;
        }
        final Path parent = jar.getParent();
        if (parent != null) {
            cp.addJarsInDirectory(parent);
        }
    }

    private static void printMethod(final JavaSootMethod method) {
        System.out.println();
        System.out.println("  " + method.getSignature());
        final Body body = method.getBody();
        body.getLocals().stream()
                .sorted(Comparator.comparing(Local::getName))
                .forEach(l -> System.out.println("    " + l.getType() + " " + l.getName() + ";"));
        System.out.println();
        body.getStmts().forEach(s -> System.out.println("    " + s));
    }
}
