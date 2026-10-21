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

package com.airbus.esar.analysis.typestate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.expr.JNeExpr;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
/**
 * Carries a typestate (DFA state) with equalities on boolean locals (Jimple: int 0/1) learned along one path, so
 * correlated "if (cond)" / "if (cond)" patterns do not spuriously merge to incompatible DFA states at a
 * join. Only "(local != 0)" / "(local == 0)" -style if heads are modelled; other ifs are left unchanged.
 */
public final class TypestatePathBranching {

    private TypestatePathBranching() {
    }

    public static final class PathFact<S> {
        private final S mState;
        private final Map<Local, Boolean> mBools; // 0/1 (false/true) in Jimple; local must equal this

        public PathFact(final S state, final Map<Local, Boolean> bools) {
            mState = state;
            mBools = Map.copyOf(bools);
        }

        public S getState() {
            return mState;
        }

        public Map<Local, Boolean> getBools() {
            return mBools;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PathFact<?>)) {
                return false;
            }
            final PathFact<?> f = (PathFact<?>) o;
            return Objects.equals(mState, f.mState) && Objects.equals(mBools, f.mBools);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mState, mBools);
        }
    }

    public static <S> Set<PathFact<S>> filterIfSuccessor(final JIfStmt ifStmt,
                                                        final Stmt next,
                                                        final Set<PathFact<S>> afterNormal,
                                                        final StmtGraph<?> graph) {
        if (next == null || afterNormal.isEmpty()) {
            return afterNormal;
        }
        final List<Stmt> succ = graph.successors(ifStmt);
        if (succ.size() != 2) {
            return afterNormal;
        }
        int idx = -1;
        for (int i = 0; i < 2; i++) {
            if (succ.get(i) == next) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            return afterNormal;
        }
        final Optional<Local> lOpt = boolLocalFromNeOrEqToZero(ifStmt.getCondition());
        if (lOpt.isEmpty()) {
            return afterNormal;
        }
        final Local l = lOpt.get();
        final boolean mustBeTrue;
        if (ifStmt.getCondition() instanceof JNeExpr) {
            // graph.successors order matches JIfStmt TRUE (1) / FALSE (0): (L != 0) true on TRUE index
            mustBeTrue = idx == JIfStmt.TRUE_BRANCH_IDX;
        } else {
            mustBeTrue = idx != JIfStmt.TRUE_BRANCH_IDX;
        }
        return constrainL(afterNormal, l, mustBeTrue);
    }

    private static boolean isIntZero(final sootup.core.jimple.basic.Immediate imm) {
        if (imm instanceof IntConstant) {
            return ((IntConstant) imm).getValue() == 0;
        }
        return false;
    }

    static Optional<Local> boolLocalFromNeOrEqToZero(final AbstractConditionExpr c) {
        if (c instanceof JNeExpr ne) {
            if (isIntZero(ne.getOp2()) && ne.getOp1() instanceof Local) {
                return Optional.of((Local) ne.getOp1());
            }
            if (isIntZero(ne.getOp1()) && ne.getOp2() instanceof Local) {
                return Optional.of((Local) ne.getOp2());
            }
        } else if (c instanceof JEqExpr eq) {
            if (isIntZero(eq.getOp2()) && eq.getOp1() instanceof Local) {
                return Optional.of((Local) eq.getOp1());
            }
            if (isIntZero(eq.getOp1()) && eq.getOp2() instanceof Local) {
                return Optional.of((Local) eq.getOp2());
            }
        }
        return Optional.empty();
    }

    private static <S> Set<PathFact<S>> constrainL(final Set<PathFact<S>> in, final Local l, final boolean mustBeTrue) {
        final Set<PathFact<S>> out = new HashSet<>();
        for (final PathFact<S> f : in) {
            final Boolean known = f.getBools().get(l);
            if (known != null && known.booleanValue() != mustBeTrue) {
                continue;
            }
            final Map<Local, Boolean> m = new HashMap<>(f.mBools);
            m.put(l, Boolean.valueOf(mustBeTrue));
            out.add(new PathFact<>(f.getState(), m));
        }
        return out;
    }
}
