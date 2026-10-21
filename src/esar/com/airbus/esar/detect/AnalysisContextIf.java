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

package com.airbus.esar.detect;

import java.util.List;
import java.util.Set;

import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.callgraph.CallGraph;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.views.JavaView;

/** Read-only snapshot from the global phase before visitors run. */
public interface AnalysisContextIf {

    /** @return underlying SootUp view */
    View getView();

    /**
     * @return the Java-oriented view built from analysis input locations
     */
    JavaView getJavaView();

    /**
     * @return the resolved class and interface hierarchy for the analyzed classpath
     */
    TypeHierarchy getTypeHierarchy();

    /**
     * Present when the engine was configured with at least one entry point and a call graph was built.
     *
     * @return the CHA-style call graph, or empty when no entry points were configured
     */
    CallGraph getCallGraph();

    /**
     * @return the configured entry method signatures, in engine order
     */
    List<MethodSignature> getEntryPoints();

    /**
     * Reachable methods from entry points (BFS). Empty when there is no call graph.
     *
     * @return unmodifiable set
     */
    Set<MethodSignature> getMethodsReachableFromEntryPoints();

    /**
     * When the reachable set is empty, always true. Otherwise true only if signature is in that set.
     *
     * @param signature method under visit
     * @return whether the method is in the entry-point slice
     */
    default boolean isMethodInEntryPointReachabilitySlice(final MethodSignature signature) {
        final Set<MethodSignature> reachable = getMethodsReachableFromEntryPoints();
        return reachable.isEmpty() || reachable.contains(signature);
    }

    /**
     * Interprocedural control-flow graph over Jimple, shared across the whole program slice implied by the call graph.
     * Absent when no call graph is available.
     *
     * @return lazy-built interprocedural CFG, or empty when no call graph exists
     */
    JimpleBasedInterproceduralCFG getInterproceduralCfg();

    /**
     * Package prefixes excluded from engine traversal and from detector call-graph descent into platform code.
     *
     * @return unmodifiable prefix list (may be empty)
     */
    List<String> getTraversalPackageBlacklist();

    /**
     * @param classType declaring class
     * @return true when the class FQN matches a traversal package blacklist prefix
     */
    default boolean isTraversalPackageBlacklisted(final ClassType classType) {
        return classType != null && isTraversalPackageBlacklisted(classType.getFullyQualifiedName());
    }

    /**
     * @param signature method whose declaring class is checked
     * @return true when the declaring class matches a traversal package blacklist prefix
     */
    default boolean isTraversalPackageBlacklisted(final MethodSignature signature) {
        return signature != null && isTraversalPackageBlacklisted(signature.getDeclClassType());
    }

    /**
     * @param classFqn fully qualified class name
     * @return true when {@code classFqn} matches a traversal package blacklist prefix
     */
    default boolean isTraversalPackageBlacklisted(final String classFqn) {
        final List<String> prefixes = getTraversalPackageBlacklist();
        if (classFqn == null || prefixes == null || prefixes.isEmpty()) {
            return false;
        }
        for (final String prefix : prefixes) {
            if (prefix != null && !prefix.isEmpty() && classFqn.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
