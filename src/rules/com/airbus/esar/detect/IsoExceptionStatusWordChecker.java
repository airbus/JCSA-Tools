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

import java.util.Optional;
import java.util.Set;

import heros.InterproceduralCFG;

import sootup.analysis.interprocedural.ide.JimpleIDESolver;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import com.airbus.esar.analysis.constantpropagation.ConstantLatticeValue;
import com.airbus.esar.analysis.constantpropagation.ConstantPropagationSolverFactory;
import com.airbus.esar.analysis.constantpropagation.ConstantPropagatorFact;
import com.airbus.esar.common.Iso78164StatusWord;
import com.airbus.esar.soot.JimpleValues;

/**
 * Resolves {@code ISOException.throwIt} status words and checks them against ISO/IEC 7816-4.
 */
final class IsoExceptionStatusWordChecker {

    private IsoExceptionStatusWordChecker() {
    }

    static Optional<Iso78164StatusWord.Violation> violationAt(final JimpleIDESolver<
            ConstantPropagatorFact,
            ConstantLatticeValue,
            InterproceduralCFG<Stmt, SootMethod>> solver,
            final Stmt stmt,
            final AbstractInvokeExpr throwIt) {
        for (final int statusWord : statusWordsAt(solver, stmt, throwIt)) {
            final Optional<Iso78164StatusWord.Violation> violation = Iso78164StatusWord.findViolation(statusWord);
            if (violation.isPresent()) {
                return violation;
            }
        }
        return Optional.empty();
    }

    static boolean isUnknownAt(final JimpleIDESolver<
            ConstantPropagatorFact,
            ConstantLatticeValue,
            InterproceduralCFG<Stmt, SootMethod>> solver,
            final Stmt stmt,
            final AbstractInvokeExpr throwIt) {
        if (throwIt.getArgCount() < 1) {
            return false;
        }
        final Value statusWord = JimpleValues.unwrapCastChain(throwIt.getArg(0));
        if (!(statusWord instanceof Local local)) {
            return false;
        }
        final ConstantLatticeValue lattice = ConstantPropagationSolverFactory.latticeAt(solver, stmt, local);
        return lattice != null && lattice.isUnknown();
    }

    private static Set<Integer> statusWordsAt(final JimpleIDESolver<
            ConstantPropagatorFact,
            ConstantLatticeValue,
            InterproceduralCFG<Stmt, SootMethod>> solver,
            final Stmt stmt,
            final AbstractInvokeExpr throwIt) {
        if (throwIt.getArgCount() < 1) {
            return Set.of();
        }
        final Value statusWord = JimpleValues.unwrapCastChain(throwIt.getArg(0));
        if (statusWord instanceof IntConstant ic) {
            return Set.of(Integer.valueOf(ic.getValue()));
        }
        if (statusWord instanceof Local local) {
            return ConstantPropagationSolverFactory.constantIntsAt(solver, stmt, local);
        }
        return Set.of();
    }
}
