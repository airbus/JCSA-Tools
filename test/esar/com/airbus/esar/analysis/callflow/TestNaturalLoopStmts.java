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

package com.airbus.esar.analysis.callflow;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;
import com.airbus.esar.fixture.naturalloop.NlLoopShapes;
import com.airbus.esar.fixture.naturalloop.NlWhileLoopBody;
import com.airbus.esar.testing.EsarAnalysisInput;

class TestNaturalLoopStmts {

    private static JavaSootClass sShapes;
    private static JavaSootClass sWhileBody;

    @BeforeAll
    static void loadFixtures() {
        sShapes = loadClass(NlLoopShapes.class);
        sWhileBody = loadClass(NlWhileLoopBody.class);
    }

    @Test
    void testWhileLoopHasNaturalLoopStmts() {
        assertHasProperLoop(bodyOf(sShapes, "whileLoop"));
    }

    @Test
    void testForLoopHasNaturalLoopStmts() {
        assertHasProperLoop(bodyOf(sShapes, "forLoop"));
    }

    @Test
    void testDoWhileLoopHasNaturalLoopStmts() {
        assertHasProperLoop(bodyOf(sShapes, "doWhileLoop"));
    }

    @Test
    void testNestedLoopsHaveNaturalLoopStmts() {
        final Body body = bodyOf(sShapes, "nestedLoops");
        final Set<Stmt> inLoop = NaturalLoopStmts.of(body);
        assertFalse(inLoop.isEmpty(), "nested loops should contribute natural-loop stmts");
        assertTrue(inLoop.size() < body.getStmts().size(),
                "prologue/epilogue stmts should remain outside loops");
        // Nested loops cover more of the CFG than a single while over similar bounds.
        final Set<Stmt> single = NaturalLoopStmts.of(bodyOf(sShapes, "whileLoop"));
        assertTrue(inLoop.size() > single.size(),
                "nested loops should mark more stmts than a single whileLoop");
    }

    @Test
    void testIfElseOnlyHasNoNaturalLoopStmts() {
        assertTrue(NaturalLoopStmts.of(bodyOf(sShapes, "ifElseOnly")).isEmpty());
    }

    @Test
    void testStraightLineHasNoNaturalLoopStmts() {
        assertTrue(NaturalLoopStmts.of(bodyOf(sShapes, "straightLine")).isEmpty());
    }

    @Test
    void testLoopThenStraightKeepsPostLoopOutside() {
        final Body body = bodyOf(sShapes, "loopThenStraight");
        final Set<Stmt> inLoop = NaturalLoopStmts.of(body);
        assertFalse(inLoop.isEmpty());
        assertTrue(inLoop.size() < body.getStmts().size(),
                "stmts after the loop must not all be classified as in-loop");
    }

    @Test
    void testLegacyWhileFixtureStillHolds() {
        assertHasProperLoop(bodyOf(sWhileBody, "withLoop"));
        assertTrue(NaturalLoopStmts.of(bodyOf(sWhileBody, "noLoop")).isEmpty());
    }

    private static void assertHasProperLoop(final Body body) {
        final Set<Stmt> inLoop = NaturalLoopStmts.of(body);
        assertFalse(inLoop.isEmpty(), "expected natural-loop stmts");
        assertTrue(inLoop.size() < body.getStmts().size(),
                "not every stmt in the method is inside a loop");
        for (final Stmt stmt : inLoop) {
            assertTrue(body.getStmts().contains(stmt), "loop stmt must belong to the method body");
        }
    }

    private static JavaSootClass loadClass(final Class<?> fixture) {
        final JavaView view = new JavaView(EsarAnalysisInput.of(fixture).getInputLocations());
        final ClassType classType = view.getIdentifierFactory().getClassType(fixture.getName());
        return view.getClass(classType).orElseThrow();
    }

    private static Body bodyOf(final JavaSootClass clazz, final String methodName) {
        final JavaSootMethod method = clazz.getMethodsByName(methodName).stream()
                .filter(JavaSootMethod::hasBody)
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing method " + methodName));
        return method.getBody();
    }
}
