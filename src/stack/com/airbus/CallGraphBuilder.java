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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraph.Call;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.core.views.JavaView;

public class CallGraphBuilder {
    private final List<AnalysisInputLocation> mInputLocations;

    public CallGraphBuilder(final List<AnalysisInputLocation> inputLocations) {
        mInputLocations = inputLocations;
    }

    private CallGraphNode buildCallGraphTree(final MethodSignature method,
                                             final CallGraph cg,
                                             final Set<MethodSignature> visited) {
        if (visited.contains(method)) {
            return null;
        }
        visited.add(method);

        final Method methodSignature = new Method(method);
        final CallGraphNode node = new CallGraphNode(methodSignature);
        final Set<Call> calls = cg.callsFrom(method);
        for (final Call call : calls) {
            final CallGraphNode childNode = buildCallGraphTree(call.getTargetMethodSignature(), cg, visited);
            if (childNode != null) {
                node.addChild(childNode);
            }
        }
        return node;
    }

    public CallGraphNode buildCallGraph(final String fullClassType, final Method methodSignature) {
        final JavaView view = new JavaView(mInputLocations);

        final ClassType classType = view.getIdentifierFactory().getClassType(fullClassType);

        final CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view);

        final MethodSignature entryMethodSignature =
            view.getIdentifierFactory()
                .getMethodSignature(
                    classType,
                    methodSignature.getName(),
                    methodSignature.getReturnType(),
                    methodSignature.getParameters()
        );

        final CallGraph cg = cha.initialize(Collections.singletonList(entryMethodSignature));

        return buildCallGraphTree(entryMethodSignature, cg, new HashSet<>());
    }
}
