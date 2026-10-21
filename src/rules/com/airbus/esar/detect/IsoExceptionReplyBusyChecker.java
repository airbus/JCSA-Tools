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
import com.airbus.esar.common.JcConstants;
import com.airbus.esar.soot.JimpleValues;

/**
 * Detects {@code ISOException.throwIt} calls with the REPLY_BUSY status word.
 */
final class IsoExceptionReplyBusyChecker {

    private IsoExceptionReplyBusyChecker() {
    }

    static boolean isReplyBusyThrow(final JimpleIDESolver<
            ConstantPropagatorFact,
            ConstantLatticeValue,
            InterproceduralCFG<Stmt, SootMethod>> solver,
            final Stmt stmt,
            final AbstractInvokeExpr throwIt) {
        if (throwIt.getArgCount() < 1) {
            return false;
        }
        final Value statusWord = JimpleValues.unwrapCastChain(throwIt.getArg(0));
        if (statusWord instanceof IntConstant ic) {
            return isReplyBusySw(ic.getValue());
        }
        if (statusWord instanceof Local local) {
            for (final int value : ConstantPropagationSolverFactory.constantIntsAt(solver, stmt, local)) {
                if (isReplyBusySw(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isReplyBusySw(final int value) {
        return (short) value == (short) JcConstants.REPLY_BUSY_SW;
    }
}
