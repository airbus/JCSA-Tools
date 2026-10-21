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

package com.airbus.esar.analysis.signext;

import java.util.ArrayList;
import java.util.List;

import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractBinopExpr;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.java.core.JavaSootMethod;

/** Consumption sinks for sign-extension pollution: at most one report per statement. */
public final class SignExtensionSinks {

    @FunctionalInterface
    public interface Reporter {
        void report(JavaSootMethod method, Stmt stmt);
    }

    /** One consumption site {@code (e, τ, κ)}. */
    private sealed interface SinkContext {
        Value expr();

        Type dest();

        boolean violates(SignExtensionFact in);

        record Store(Value expr, Type dest, LValue lhs, Value rhs) implements SinkContext {
            @Override
            public boolean violates(final SignExtensionFact in) {
                if (!SignExtensionPollution.widensFromNarrow(dest)) {
                    return false;
                }
                if (SignExtensionLocals.isAssignToTrackingTemp(lhs, rhs)) {
                    return false;
                }
                if (!SignExtensionLocals.isConsumptionStore(lhs, rhs)) {
                    return false;
                }
                if (rhs instanceof final AbstractInvokeExpr invoke) {
                    if (invoke.getType().equals(PrimitiveType.getByte())
                            || invoke.getType().equals(PrimitiveType.getShort())) {
                        return SignExtensionEval.pollutesAtConsumer(rhs, in, dest);
                    }
                    return false;
                }
                return SignExtensionEval.pollutesAtConsumer(rhs, in, dest);
            }
        }

        record Return(Value expr, Type dest) implements SinkContext {
            @Override
            public boolean violates(final SignExtensionFact in) {
                return SignExtensionEval.pollutesAtConsumer(expr, in, dest);
            }
        }

        record InvokeArg(Value expr, Type dest) implements SinkContext {
            @Override
            public boolean violates(final SignExtensionFact in) {
                return SignExtensionEval.pollutesAtInvokeArg(expr, in, dest);
            }
        }

        record Compare(Value expr,
                       Type dest,
                       AbstractConditionExpr condition,
                       Value other) implements SinkContext {
            @Override
            public boolean violates(final SignExtensionFact in) {
                if (!SignExtensionEval.comparisonPollutionMatters(condition, expr, other)) {
                    return false;
                }
                if (expr instanceof final Local local
                        && SignExtensionEval.isOrderedInRangeLiteralCompare(condition, local, other)
                        && !SignExtensionEval.crossNarrowIntegralCompare(local, other)) {
                    return false;
                }
                if (SignExtensionEval.pollutesAtConsumer(expr, in, dest, true, other)) {
                    return true;
                }
                return SignExtensionEval.isTypedNarrowLoadLocal(expr, in)
                        && SignExtensionEval.isUnsignedByteLiteralCompareTrap(condition, expr, other);
            }
        }

        record Switch(Value expr, Type dest, JSwitchStmt sw) implements SinkContext {
            @Override
            public boolean violates(final SignExtensionFact in) {
                if (!SignExtensionEval.pollutesAtConsumer(expr, in, dest)) {
                    return false;
                }
                return SwitchOutcome.pollutionMatters(sw, expr, in);
            }
        }
    }

    private SignExtensionSinks() {
    }

    public static void reportStmt(final JavaSootMethod method,
                                  final Stmt stmt,
                                  final SignExtensionFact in,
                                  final Reporter report) {
        for (final SinkContext sink : extract(method, stmt)) {
            if (sink.violates(in)) {
                report.report(method, stmt);
                return;
            }
        }
    }

    private static List<SinkContext> extract(final JavaSootMethod method, final Stmt stmt) {
        return switch (stmt) {
            case final JAssignStmt assign -> extractAssign(assign);
            case final JReturnStmt ret -> extractReturn(method, ret);
            case final JInvokeStmt inv -> {
                final AbstractInvokeExpr invoke = inv.getInvokeExpr().orElse(null);
                yield invoke == null ? List.of() : extractValueSinks(invoke);
            }
            case final JIfStmt iff -> extractValueSinks(iff.getCondition());
            case final JSwitchStmt sw -> extractSwitch(sw);
            default -> List.of();
        };
    }

    private static List<SinkContext> extractAssign(final JAssignStmt assign) {
        final LValue lhs = assign.getLeftOp();
        final Value rhs = assign.getRightOp();
        if (rhs instanceof AbstractInvokeExpr && SignExtensionLocals.isAssignToTrackingTemp(lhs, rhs)) {
            return List.of();
        }
        final List<SinkContext> out = new ArrayList<>(extractValueSinks(rhs));
        out.add(new SinkContext.Store(rhs, lhs.getType(), lhs, rhs));
        return out;
    }

    private static List<SinkContext> extractReturn(final JavaSootMethod method, final JReturnStmt ret) {
        final Value op = ret.getOp();
        final List<SinkContext> out = new ArrayList<>(extractValueSinks(op));
        final Type retType = method.getSignature().getType();
        if (SignExtensionPollution.widensFromNarrow(retType)) {
            out.add(new SinkContext.Return(op, retType));
        }
        return out;
    }

    private static List<SinkContext> extractSwitch(final JSwitchStmt sw) {
        final Value key = sw.getKey();
        final Type keyType = SignExtensionEval.primitiveTypeOf(key);
        if (keyType != null && SignExtensionEval.isNarrowIntegral(keyType)) {
            return List.of();
        }
        return List.of(new SinkContext.Switch(key, PrimitiveType.getInt(), sw));
    }

    private static List<SinkContext> extractValueSinks(final Value value) {
        return switch (value) {
            case final AbstractInvokeExpr invoke -> extractInvokeArgs(invoke);
            case final AbstractBinopExpr binop -> {
                final List<SinkContext> out = new ArrayList<>();
                if (binop instanceof final AbstractConditionExpr condition) {
                    addCompare(out, condition, binop.getOp1(), binop.getOp2());
                    addCompare(out, condition, binop.getOp2(), binop.getOp1());
                }
                out.addAll(extractValueSinks(binop.getOp1()));
                out.addAll(extractValueSinks(binop.getOp2()));
                yield out;
            }
            default -> {
                final List<SinkContext> out = new ArrayList<>();
                for (final Value use : value.getUses().toList()) {
                    out.addAll(extractValueSinks(use));
                }
                yield out;
            }
        };
    }

    private static List<SinkContext> extractInvokeArgs(final AbstractInvokeExpr invoke) {
        final List<SinkContext> out = new ArrayList<>();
        final var params = invoke.getMethodSignature().getParameterTypes();
        for (int i = 0; i < invoke.getArgCount(); i++) {
            final Type param = params.get(i);
            if (SignExtensionPollution.widensFromNarrow(param)) {
                out.add(new SinkContext.InvokeArg(invoke.getArg(i), param));
            }
        }
        return out;
    }

    private static void addCompare(final List<SinkContext> out,
                                   final AbstractConditionExpr condition,
                                   final Value op,
                                   final Value other) {
        final Type dest = SignExtensionEval.comparisonWidenDest(op, other);
        if (dest != null) {
            out.add(new SinkContext.Compare(op, dest, condition, other));
        }
    }
}
