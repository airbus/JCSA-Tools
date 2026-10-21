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

package com.airbus.esar.internal.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.java.core.views.JavaView;

/**
 * This class computes and holds all of the general SootUp analysis done for the whole program
 */
final class SootAnalytics {
    private final List<AnalysisInputLocation> mInputLocations;
    private final List<MethodSignature> mEntryPoints;
    private final JavaView mView;
    private final TypeHierarchy mTypeHierarchy;
    private final CallGraph mCallGraph;
    private final Set<MethodSignature> mReachableMethods;
    private JimpleBasedInterproceduralCFG mInterproceduralCfg;

    SootAnalytics(final List<AnalysisInputLocation> inputLocations,
                  final List<MethodSignature> entryPoints) {
        mInputLocations = inputLocations;
        mEntryPoints = entryPoints;
        mView = new JavaView(mInputLocations);
        mTypeHierarchy = mView.getTypeHierarchy();
        mCallGraph = buildCallGraph();
        mReachableMethods = computeMethodsReachableFromEntryPoints();
        mInterproceduralCfg = new JimpleBasedInterproceduralCFG(mCallGraph, mView, false, false);
    }

    private CallGraph buildCallGraph() {
        // Use CHA as it's suitable for Java Card applications and is sound
        final ClassHierarchyAnalysisAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(mView);
        final CallGraph cg = cha.initialize(new ArrayList<>(mEntryPoints));
        return cg;
    }

    private Set<MethodSignature> computeMethodsReachableFromEntryPoints() {
        final CallGraph cg = mCallGraph;
        final ArrayDeque<MethodSignature> queue = new ArrayDeque<>(mEntryPoints);
        final Set<MethodSignature> seen = new HashSet<>();
        while (!queue.isEmpty()) {
            final MethodSignature m = queue.removeFirst();
            if (!seen.add(m)) {
                continue;
            }
            for (final MethodSignature tgt : cg.callTargetsFrom(m)) {
                queue.addLast(tgt);
            }
        }
        return seen.isEmpty() ? Set.of() : Set.copyOf(seen);
    }

    public List<AnalysisInputLocation> getInputLocations() {
        return mInputLocations;
    }

    public List<MethodSignature> getEntryPoints() {
        return mEntryPoints;
    }

    public JavaView getView() {
        return mView;
    }

    public TypeHierarchy getTypeHierarchy() {
        return mTypeHierarchy;
    }

    public CallGraph getCallGraph() {
        return mCallGraph;
    }

    public Set<MethodSignature> getReachableMethods() {
        return mReachableMethods;
    }

    public JimpleBasedInterproceduralCFG getInterproceduralCfg() {
        return mInterproceduralCfg;
    }
}
