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

package com.airbus.esar.analysis.primitivetaint;

import java.util.HashSet;
import java.util.Set;

import heros.InterproceduralCFG;

import sootup.analysis.interprocedural.ifds.JimpleIFDSSolver;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;

/**
 * IFDS TrackedPrimitiveFact lookup at invoke sites (statement and CFG successors).
 */
public final class IfdsInvokeFactSupport {

    private IfdsInvokeFactSupport() {
    }

    /**
     * @param <L> label type on facts
     * @param solver shared primitive-taint solver
     * @param stmt statement carrying the invoke
     * @param body enclosing method body
     * @return union of facts at {@code stmt} and at immediate CFG successors (IFDS may attach slightly after the
     *     invoke)
     */
    public static <L> Set<TrackedPrimitiveFact<L>> collectFactsAtInvoke(
            final JimpleIFDSSolver<TrackedPrimitiveFact<L>, InterproceduralCFG<Stmt, SootMethod>> solver,
            final Stmt stmt,
            final Body body) {
        final Set<TrackedPrimitiveFact<L>> facts = new HashSet<>(solver.ifdsResultsAt(stmt));
        for (final Stmt succ : body.getStmtGraph().successors(stmt)) {
            facts.addAll(solver.ifdsResultsAt(succ));
        }
        return facts;
    }

    /**
     * @param fact IFDS fact
     * @param target unwrapped argument value at the invoke
     * @return true when the fact’s carrier denotes the same source as {@code target}
     */
    public static boolean carrierMatchesArgument(final TrackedPrimitiveFact<?> fact, final Value target) {
        final Value c = fact.getCarrier();
        if (target instanceof Local && c instanceof Local) {
            return c == target;
        }
        return TrackedPrimitiveFact.carriersEqual(c, target);
    }
}
