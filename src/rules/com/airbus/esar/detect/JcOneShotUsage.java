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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.jimple.common.stmt.JThrowStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.util.printer.JimpleTryTraps;
import sootup.java.core.JavaSootMethod;

/**
 * OneShot.open must be inside a try block and paired with close in finally.
 */
public final class JcOneShotUsage implements ProgramVisitorIf {
    public static final String RULE_ID = "JcOneShotUsage";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.ERROR;
    public static final String MESSAGE_INVALID_USAGE = "javacard.oneShot.invalidUsage";
    private static final String FINALLY_EXCEPTION = "java.lang.Throwable";


    @Override
    public String ruleId() {
        return RULE_ID;
    }

    @Override
    public SarifResultLevel severity() {
        return DEFAULT_SEVERITY;
    }

    @Override
    public void visitMethod(final JavaSootMethod method,
                            final Body body,
                            final AnalysisContextIf context,
                            final BugReporterIf bugs) {
        if (!context.isMethodInEntryPointReachabilitySlice(method.getSignature())) {
            return;
        }

        final List<Trap> traps = JimpleTryTraps.collect(body.getStmtGraph());
        final List<Stmt> stmts = body.getStmts();
        final Map<Stmt, Integer> stmtIndex = indexStmts(stmts);

        for (final Stmt stmt : stmts) {
            if (!(stmt instanceof JAssignStmt assign)) {
                continue;
            }
            if (!(assign.getRightOp() instanceof JStaticInvokeExpr openExpr) || !isOneShotOpen(openExpr)) {
                continue;
            }
            if (!(assign.getLeftOp() instanceof Local receiver)) {
                continue;
            }

            final Optional<Trap> finallyTrap = findFinallyTrapContaining(stmt, traps, stmtIndex);
            if (finallyTrap.isEmpty() || !hasCloseInFinally(receiver, finallyTrap.get(), stmts, stmtIndex)) {
                bugs.report(RULE_ID, DEFAULT_SEVERITY, RuleMessageRef.of(MESSAGE_INVALID_USAGE), method, stmt);
            }
        }
    }

    private static Map<Stmt, Integer> indexStmts(final List<Stmt> stmts) {
        final Map<Stmt, Integer> index = new HashMap<>();
        for (int i = 0; i < stmts.size(); i++) {
            index.put(stmts.get(i), i);
        }
        return index;
    }

    private static Optional<Trap> findFinallyTrapContaining(final Stmt open,
                                                            final List<Trap> traps,
                                                            final Map<Stmt, Integer> stmtIndex) {
        final Integer openIdx = stmtIndex.get(open);
        if (openIdx == null) {
            return Optional.empty();
        }
        Optional<Trap> innermost = Optional.empty();
        int smallestSpan = Integer.MAX_VALUE;
        for (final Trap trap : traps) {
            if (!FINALLY_EXCEPTION.equals(trap.getExceptionType().getFullyQualifiedName())) {
                continue;
            }
            final Integer begin = stmtIndex.get(trap.getBeginStmt());
            final Integer end = stmtIndex.get(trap.getEndStmt());
            if (begin == null || end == null || begin > openIdx || openIdx > end) {
                continue;
            }
            final int span = end - begin;
            if (span < smallestSpan) {
                smallestSpan = span;
                innermost = Optional.of(trap);
            }
        }
        return innermost;
    }

    private static boolean hasCloseInFinally(final Local receiver,
                                             final Trap finallyTrap,
                                             final List<Stmt> stmts,
                                             final Map<Stmt, Integer> stmtIndex) {
        final Integer endIdx = stmtIndex.get(finallyTrap.getEndStmt());
        final Integer handlerIdx = stmtIndex.get(finallyTrap.getHandlerStmt());
        if (endIdx == null || handlerIdx == null) {
            return false;
        }
        final Integer normalEndIdx = firstMatchingIndex(stmts, stmtIndex, endIdx, true,
                JcOneShotUsage::isNormalFinallyEnd);
        final Integer throwIdx = firstMatchingIndex(stmts, stmtIndex, handlerIdx, false, JThrowStmt.class::isInstance);
        if (normalEndIdx == null || throwIdx == null) {
            return false;
        }
        boolean normalClose = false;
        boolean exceptionClose = false;
        for (final Stmt stmt : stmts) {
            if (!isOneShotCloseOn(stmt, receiver)) {
                continue;
            }
            final Integer closeIdx = stmtIndex.get(stmt);
            if (closeIdx == null) {
                continue;
            }
            if (closeIdx > endIdx && closeIdx < normalEndIdx) {
                normalClose = true;
            }
            if (closeIdx >= handlerIdx && closeIdx < throwIdx) {
                exceptionClose = true;
            }
        }
        return normalClose && exceptionClose;
    }

    private static boolean isNormalFinallyEnd(final Stmt stmt) {
        return stmt instanceof JGotoStmt
                || stmt instanceof JReturnStmt
                || stmt instanceof JReturnVoidStmt;
    }

    private static Integer firstMatchingIndex(final List<Stmt> stmts,
                                              final Map<Stmt, Integer> stmtIndex,
                                              final int bound,
                                              final boolean strictlyAfter,
                                              final Predicate<Stmt> match) {
        for (final Stmt stmt : stmts) {
            if (!match.test(stmt)) {
                continue;
            }
            final Integer idx = stmtIndex.get(stmt);
            if (idx == null) {
                continue;
            }
            if (strictlyAfter ? idx > bound : idx >= bound) {
                return idx;
            }
        }
        return null;
    }

    private static boolean isOneShotOpen(final JStaticInvokeExpr expr) {
        final MethodSignature sig = expr.getMethodSignature();
        return "open".equals(sig.getName()) && isOneShotType(sig);
    }

    private static boolean isOneShotCloseOn(final Stmt stmt, final Local receiver) {
        if (!(stmt instanceof JInvokeStmt invokeStmt)) {
            return false;
        }
        final Optional<AbstractInvokeExpr> invOpt = invokeStmt.getInvokeExpr();
        if (invOpt.isEmpty() || !(invOpt.get() instanceof AbstractInstanceInvokeExpr inst)) {
            return false;
        }
        final MethodSignature sig = inst.getMethodSignature();
        return "close".equals(sig.getName())
                && sig.getParameterCount() == 0
                && isOneShotType(sig)
                && inst.getBase().equivTo(receiver);
    }

    private static boolean isOneShotType(final MethodSignature sig) {
        return sig.getDeclClassType().getFullyQualifiedName().endsWith("$OneShot");
    }
}
