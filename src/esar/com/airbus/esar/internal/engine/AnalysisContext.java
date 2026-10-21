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

import java.util.List;
import java.util.Set;

import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.callgraph.CallGraph;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.views.View;
import sootup.java.core.views.JavaView;
import com.airbus.esar.detect.AnalysisContextIf;

final class AnalysisContext implements AnalysisContextIf {
    private final SootAnalytics mProgramAnalytics;
    private final List<String> mTraversalPackageBlacklist;

    AnalysisContext(final SootAnalytics programAnalytics, final List<String> traversalPackageBlacklist) {
        mProgramAnalytics = programAnalytics;
        mTraversalPackageBlacklist = TraversalPackageBlacklist.copyOf(traversalPackageBlacklist);
    }

    @Override
    public View getView() {
        return mProgramAnalytics.getView();
    }

    @Override
    public JavaView getJavaView() {
        return mProgramAnalytics.getView();
    }

    @Override
    public TypeHierarchy getTypeHierarchy() {
        return mProgramAnalytics.getTypeHierarchy();
    }

    @Override
    public CallGraph getCallGraph() {
        return mProgramAnalytics.getCallGraph();
    }

    @Override
    public List<MethodSignature> getEntryPoints() {
        return List.copyOf(mProgramAnalytics.getEntryPoints());
    }

    @Override
    public Set<MethodSignature> getMethodsReachableFromEntryPoints() {
        return Set.copyOf(mProgramAnalytics.getReachableMethods());
    }

    @Override
    public JimpleBasedInterproceduralCFG getInterproceduralCfg() {
        return mProgramAnalytics.getInterproceduralCfg();
    }

    @Override
    public List<String> getTraversalPackageBlacklist() {
        return mTraversalPackageBlacklist;
    }
}
