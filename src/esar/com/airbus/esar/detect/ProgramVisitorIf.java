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

import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;

/** Rule callbacks after global analysis; BugReporterIf is passed in (context stays read-only). */
public interface ProgramVisitorIf {

    /**
     * @return stable rule identifier used in findings and rule configuration
     */
    String ruleId();

    /**
     * @return default SARIF severity when rule configuration does not override it
     */
    SarifResultLevel severity();

    /**
     * @param registry shared solvers for this traversal batch
     * @param context global analysis snapshot
     */
    default void initAnalysis(final SharedSolverRegistryIf registry, final AnalysisContextIf context) {
        // default: no-op
    }

    /**
     * Called once before any class or method for this visitor instance.
     *
     * @param context global analysis snapshot for this run
     * @param bugs thread-safe sink for this traversal (shared when multiple visitors run in one engine batch)
     */
    default void startAnalysis(final AnalysisContextIf context, final BugReporterIf bugs) {
        // default: no-op
    }

    /**
     * Called once per class from the analysis view before that class's methods are visited.
     *
     * @param clazz class being entered
     * @param context global analysis snapshot for this run
     * @param bugs thread-safe sink for this traversal
     */
    default void visitClass(final JavaSootClass clazz, final AnalysisContextIf context, final BugReporterIf bugs) {
        // default: no-op
    }

    /**
     * Called for each concrete method with a Jimple body selected by the traversal.
     *
     * @param method method whose body is supplied
     * @param body Jimple body of {@code method}
     * @param context global analysis snapshot for this run
     * @param bugs thread-safe sink for this traversal
     */
    default void visitMethod(final JavaSootMethod method, final Body body, final AnalysisContextIf context,
            final BugReporterIf bugs) {
        // default: no-op
    }

    /**
     * Called for each {@link Stmt} in {@code body.getStmtGraph()} immediately after {@link #visitMethod} for the same
     * {@code method} and {@code body}.
     *
     * @param method declaring method
     * @param body Jimple body
     * @param stmt current statement in CFG iteration order
     * @param context global analysis snapshot for this run
     * @param bugs thread-safe sink for this traversal
     */
    default void visitStmt(final JavaSootMethod method, final Body body, final Stmt stmt,
            final AnalysisContextIf context, final BugReporterIf bugs) {
        // default: no-op
    }

    /**
     * Called for each Jimple {@link Value} (expression, reference, or immediate) in {@code stmt}, in preorder over the
     * trees rooted at {@link Stmt#getUsesAndDefs()}, immediately after {@link #visitStmt} for the same {@code stmt}.
     *
     * @param method declaring method
     * @param body Jimple body
     * @param stmt statement that contains this value
     * @param value current node in the value tree
     * @param context global analysis snapshot for this run
     * @param bugs thread-safe sink for this traversal
     */
    default void visitExpr(final JavaSootMethod method, final Body body, final Stmt stmt, final Value value,
            final AnalysisContextIf context, final BugReporterIf bugs) {
        // default: no-op
    }

    /**
     * Called once after all classes and methods for this visitor instance.
     *
     * @param context global analysis snapshot for this run
     * @param bugs thread-safe sink for this traversal
     */
    default void endAnalysis(final AnalysisContextIf context, final BugReporterIf bugs) {
        // default: no-op
    }
}
