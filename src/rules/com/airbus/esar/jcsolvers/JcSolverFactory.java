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

package com.airbus.esar.jcsolvers;

import java.util.List;

import heros.InterproceduralCFG;

import sootup.analysis.interprocedural.ifds.JimpleIFDSSolver;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaSootMethod;

import com.airbus.esar.analysis.primitivetaint.PrimitiveSourceTrackingSpec;
import com.airbus.esar.analysis.primitivetaint.PrimitiveTaintIfdsProblem;
import com.airbus.esar.analysis.primitivetaint.TrackedPrimitiveFact;
import com.airbus.esar.detect.AnalysisContextIf;

/**
 * Builds Java Card primitive-taint IFDS solvers from AnalysisContextIf.
 */
public final class JcSolverFactory {

    private JcSolverFactory() {
    }

    /**
     * buildPrimitiveTaintSolver with JcAlgTaintSpecs.programUnion().
     *
     * @param context global analysis snapshot
     * @return running IFDS solver
     * @throws IllegalStateException when entry points, resolvable bodies, or IFDS seeds are insufficient
     */
    public static JimpleIFDSSolver<
            TrackedPrimitiveFact<JcAlgTaintTag>, InterproceduralCFG<Stmt, SootMethod>> buildPrimitiveTaintSolver(
            final AnalysisContextIf context) {
        return buildPrimitiveTaintSolver(context, JcAlgTaintSpecs.programUnion());
    }

    /**
     * @param context global analysis snapshot (view, entry points, call graph, ICFG)
     * @param spec merged field/literal to JcAlgTaintTag map
     * @return running IFDS solver
     * @throws IllegalStateException when prerequisites are not met
     */
    public static JimpleIFDSSolver<
            TrackedPrimitiveFact<JcAlgTaintTag>, InterproceduralCFG<Stmt, SootMethod>> buildPrimitiveTaintSolver(
            final AnalysisContextIf context,
            final PrimitiveSourceTrackingSpec<JcAlgTaintTag> spec) {
        final List<MethodSignature> entryPoints = context.getEntryPoints();
        if (entryPoints.isEmpty()) {
            throw new IllegalStateException("Java Card primitive taint IFDS requires at least one entry point");
        }
        final boolean anyResolvedEntry = entryPoints.stream()
                .anyMatch(sig -> context.getJavaView().getMethod(sig).filter(JavaSootMethod::hasBody).isPresent());
        if (!anyResolvedEntry) {
            throw new IllegalStateException(
                    "No entry point with a resolvable Jimple body for Java Card primitive taint IFDS");
        }
        final PrimitiveTaintIfdsProblem<JcAlgTaintTag> problem = new PrimitiveTaintIfdsProblem<>(
                context.getInterproceduralCfg(), context.getJavaView(), entryPoints, spec);
        if (problem.initialSeeds().isEmpty()) {
            throw new IllegalStateException("Java Card primitive taint IFDS has no initial seeds");
        }
        final JimpleIFDSSolver<TrackedPrimitiveFact<JcAlgTaintTag>, InterproceduralCFG<Stmt, SootMethod>> solver =
                new JimpleIFDSSolver<>(problem);
        solver.solve("");
        return solver;
    }
}
