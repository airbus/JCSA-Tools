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

package com.airbus.esar.analysis.constantpropagation;

import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

import heros.InterproceduralCFG;

import sootup.analysis.interprocedural.ide.JimpleIDESolver;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaSootMethod;
import com.airbus.esar.detect.AnalysisContextIf;

/**
 * Builds and runs the shared integer constant-propagation IDE solver.
 */
public final class ConstantPropagationSolverFactory {

    private ConstantPropagationSolverFactory() {
    }

    /**
     * @param context global analysis snapshot
     * @return solved IDE instance
     * @throws IllegalStateException when entry points or seeds are insufficient
     */
    public static JimpleIDESolver<
            ConstantPropagatorFact,
            ConstantLatticeValue,
            InterproceduralCFG<Stmt, SootMethod>> buildSolver(final AnalysisContextIf context) {
        final List<MethodSignature> entryPoints = context.getEntryPoints();
        if (entryPoints.isEmpty()) {
            throw new IllegalStateException("Constant propagation IDE requires at least one entry point");
        }
        final boolean anyResolvedEntry = entryPoints.stream()
                .anyMatch(sig -> context.getJavaView().getMethod(sig).filter(JavaSootMethod::hasBody).isPresent());
        if (!anyResolvedEntry) {
            throw new IllegalStateException(
                    "No entry point with a resolvable Jimple body for constant propagation IDE");
        }
        final ConstantPropagationIdeProblem problem = new ConstantPropagationIdeProblem(
                context.getInterproceduralCfg(), context.getJavaView(), entryPoints);
        if (problem.initialSeeds().isEmpty()) {
            throw new IllegalStateException("Constant propagation IDE has no initial seeds");
        }
        final JimpleIDESolver<
                ConstantPropagatorFact,
                ConstantLatticeValue,
                InterproceduralCFG<Stmt, SootMethod>> solver = new JimpleIDESolver<>(problem);
        solver.solve();
        return solver;
    }

    /**
     * @param solver solved constant-propagation IDE
     * @param stmt program point before the value is read
     * @param local local whose constant int value is requested
     * @return known constant when exactly one, or empty otherwise
     */
    public static OptionalInt constantIntAt(final JimpleIDESolver<
            ConstantPropagatorFact,
            ConstantLatticeValue,
            InterproceduralCFG<Stmt, SootMethod>> solver,
            final Stmt stmt,
            final Local local) {
        final ConstantLatticeValue lattice = solver.resultAt(stmt, ConstantPropagatorFact.of(local));
        if (lattice == null) {
            return OptionalInt.empty();
        }
        return lattice.asInt();
    }

    /**
     * @param solver solved constant-propagation IDE
     * @param stmt program point before the value is read
     * @param local local whose possible constant ints are requested
     * @return known constants (bounded powerset), or empty when top/bottom/unknown
     */
    public static Set<Integer> constantIntsAt(final JimpleIDESolver<
            ConstantPropagatorFact,
            ConstantLatticeValue,
            InterproceduralCFG<Stmt, SootMethod>> solver,
            final Stmt stmt,
            final Local local) {
        final ConstantLatticeValue lattice = latticeAt(solver, stmt, local);
        if (lattice == null) {
            return Set.of();
        }
        return lattice.asInts();
    }

    /**
     * @param solver solved constant-propagation IDE
     * @param stmt program point before the value is read
     * @param local local to query
     * @return lattice value, or null when absent
     */
    public static ConstantLatticeValue latticeAt(final JimpleIDESolver<
            ConstantPropagatorFact,
            ConstantLatticeValue,
            InterproceduralCFG<Stmt, SootMethod>> solver,
            final Stmt stmt,
            final Local local) {
        return solver.resultAt(stmt, ConstantPropagatorFact.of(local));
    }
}
