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

import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import heros.InterproceduralCFG;

import sootup.analysis.interprocedural.ifds.JimpleIFDSSolver;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import com.airbus.esar.soot.JimpleValues;

/**
 * IFDS lookup over TrackedPrimitiveFact: filter by tag, match carrier to unwrapped invoke arg, fold duplicates with
 * accumulate, map chosen fact to optional warning text. Caller supplies predicates and mappers.
 *
 * @param <L> label type on facts
 */
public final class PrimitiveTaintIfdsQuery<L> {

    private final Predicate<L> mTagFilter;
    private final BiFunction<TrackedPrimitiveFact<L>, TrackedPrimitiveFact<L>, TrackedPrimitiveFact<L>> mAccumulate;
    private final Function<TrackedPrimitiveFact<L>, Optional<String>> mWarningDetail;

    /**
     * @param tagFilter which fact tags participate (e.g. deprecated family for a given API)
     * @param accumulate fold function; first argument is null on the first matching fact
     * @param warningDetail maps a chosen fact to message text, or empty to suppress warning for that fact
     */
    public PrimitiveTaintIfdsQuery(
            final Predicate<L> tagFilter,
            final BiFunction<TrackedPrimitiveFact<L>, TrackedPrimitiveFact<L>, TrackedPrimitiveFact<L>> accumulate,
            final Function<TrackedPrimitiveFact<L>, Optional<String>> warningDetail) {
        mTagFilter = tagFilter;
        mAccumulate = accumulate;
        mWarningDetail = warningDetail;
    }

    /**
     * @param solver program primitive-taint solver
     * @param stmt invoke statement
     * @param body enclosing body
     * @param arg argument at the invoke
     * @return warning message fragment, or empty when no matching fact or {@code warningDetail} is empty
     */
    public Optional<String> query(
            final JimpleIFDSSolver<TrackedPrimitiveFact<L>, InterproceduralCFG<Stmt, SootMethod>> solver,
            final Stmt stmt,
            final Body body,
            final Immediate arg) {
        final Set<TrackedPrimitiveFact<L>> facts = IfdsInvokeFactSupport.collectFactsAtInvoke(solver, stmt, body);
        final Value target = JimpleValues.unwrapCastChain(arg);
        TrackedPrimitiveFact<L> chosen = null;
        for (final TrackedPrimitiveFact<L> f : facts) {
            if (f.isZero()
                    || !mTagFilter.test(f.getTag())
                    || !IfdsInvokeFactSupport.carrierMatchesArgument(f, target)) {
                continue;
            }
            chosen = mAccumulate.apply(chosen, f);
        }
        if (chosen == null) {
            return Optional.empty();
        }
        return mWarningDetail.apply(chosen);
    }
}
