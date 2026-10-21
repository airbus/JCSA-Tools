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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.FieldSignature;
import sootup.java.core.JavaSootMethod;
import com.airbus.esar.analysis.callflow.ForwardStmtWorklist;
import com.airbus.esar.analysis.callflow.NaturalLoopStmts;

/**
 * Warns when a field is read at least {@code threshold} times on some execution path within one method body,
 * or when any field read occurs inside a loop. Field writes are ignored.
 * Instance reads are counted per (field, base local); static reads per field only.
 * Frequent-access reports anchor on the earliest source line among reads of that key.
 */
public final class JcHotField implements ProgramVisitorIf {

    public static final String RULE_ID = "JcHotField";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.WARNING;
    public static final String MESSAGE_FREQUENT = "javacard.hotfield.frequentAccess";
    public static final String MESSAGE_LOOP = "javacard.hotfield.loopAccess";

    private static final Map<FieldReadKey, Integer> EMPTY_COUNTS = Map.of();

    private static final Comparator<Optional<SourceRegion>> BY_SOURCE_REGION = Comparator
            .comparing((Optional<SourceRegion> r) -> r.map(SourceRegion::getFirstLine).orElse(Integer.MAX_VALUE))
            .thenComparing(r -> r.map(SourceRegion::getFirstCol).filter(c -> c >= 0).orElse(Integer.MAX_VALUE));

    private final int mThreshold;

    public JcHotField(final int threshold) {
        this.mThreshold = threshold;
    }

    public JcHotField() {
        this.mThreshold = 5;
    }


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
        final List<Stmt> stmts = body.getStmts();
        final Set<Stmt> inLoop = NaturalLoopStmts.of(body);
        final Map<FieldReadKey, Stmt> firstRead = new LinkedHashMap<>();
        final Set<FieldReadKey> loopReported = new LinkedHashSet<>();

        for (final Stmt stmt : stmts) {
            collectFieldReads(stmt, key -> firstRead.merge(key, stmt, JcHotField::earlierSource));
        }

        for (final Stmt stmt : stmts) {
            collectFieldReads(stmt, key -> {
                if (inLoop.contains(stmt) && loopReported.add(key)) {
                    bugs.report(RULE_ID, DEFAULT_SEVERITY,
                            RuleMessageRef.of(MESSAGE_LOOP), method, stmt);
                }
            });
        }

        final Map<FieldReadKey, Integer> maxPerPath = computeMaxReadCountsPerPath(body, inLoop);
        for (final Map.Entry<FieldReadKey, Integer> e : maxPerPath.entrySet()) {
            if (e.getValue() >= mThreshold) {
                bugs.report(RULE_ID, DEFAULT_SEVERITY,
                        RuleMessageRef.of(MESSAGE_FREQUENT, e.getValue()),
                        method, firstRead.get(e.getKey()));
            }
        }
    }

    private static Stmt earlierSource(final Stmt a, final Stmt b) {
        return BY_SOURCE_REGION.compare(StmtSourceLocations.regionForStmt(a),
                StmtSourceLocations.regionForStmt(b)) <= 0 ? a : b;
    }

    private static Map<FieldReadKey, Integer> computeMaxReadCountsPerPath(final Body body,
                                                                            final Set<Stmt> inLoop) {
        final StmtGraph<?> graph = body.getStmtGraph();
        Stmt entry = graph.getStartingStmt();
        if (entry == null && !body.getStmts().isEmpty()) {
            entry = body.getStmts().get(0);
        }
        if (entry == null) {
            return Map.of();
        }
        final Map<FieldReadKey, Integer> maxCounts = new LinkedHashMap<>();
        final ForwardStmtWorklist.Transfer<Map<FieldReadKey, Integer>> transfer =
                new ForwardStmtWorklist.Transfer<>() {
                    @Override
                    public Map<FieldReadKey, Integer> normal(final Stmt stmt,
                                                             final Map<FieldReadKey, Integer> in) {
                        final Map<FieldReadKey, Integer> out = transferReads(stmt, in, inLoop);
                        mergeMax(maxCounts, out);
                        return out;
                    }

                    @Override
                    public Map<FieldReadKey, Integer> exception(final Stmt stmt,
                                                                final Map<FieldReadKey, Integer> in) {
                        return in;
                    }
                };
        ForwardStmtWorklist.runFromSeeds(graph, Map.of(entry, EMPTY_COUNTS), transfer,
                JcHotField::joinMaxIfImproves,
                (tail, fact) -> mergeMax(maxCounts, fact),
                ForwardStmtWorklist.DEFAULT_STEP_CAP,
                body);
        return maxCounts;
    }

    private static Map<FieldReadKey, Integer> transferReads(final Stmt stmt,
                                                            final Map<FieldReadKey, Integer> in,
                                                            final Set<Stmt> inLoop) {
        if (inLoop.contains(stmt)) {
            return in;
        }
        if (!(stmt instanceof final JAssignStmt assign)) {
            return in;
        }
        final FieldReadKey key = fieldReadKey(assign.getRightOp());
        if (key == null) {
            return in;
        }
        return incrementCount(in, key);
    }

    private static Map<FieldReadKey, Integer> incrementCount(final Map<FieldReadKey, Integer> in,
                                                             final FieldReadKey key) {
        if (in.isEmpty()) {
            final Map<FieldReadKey, Integer> single = new LinkedHashMap<>();
            single.put(key, 1);
            return single;
        }
        final Map<FieldReadKey, Integer> out = new LinkedHashMap<>(in);
        out.merge(key, 1, Integer::sum);
        return out;
    }

    private static Map<FieldReadKey, Integer> joinMaxIfImproves(
            final Map<FieldReadKey, Integer> existing,
            final Map<FieldReadKey, Integer> proposed) {
        if (proposed == null) {
            return null;
        }
        if (existing == null) {
            return proposed.isEmpty() ? EMPTY_COUNTS : new LinkedHashMap<>(proposed);
        }
        if (proposed.isEmpty()) {
            return null;
        }
        Map<FieldReadKey, Integer> merged = null;
        final Set<FieldReadKey> keys = new LinkedHashSet<>();
        keys.addAll(existing.keySet());
        keys.addAll(proposed.keySet());
        for (final FieldReadKey key : keys) {
            final int cur = existing.getOrDefault(key, 0);
            final int next = Math.max(cur, proposed.getOrDefault(key, 0));
            if (next > cur) {
                if (merged == null) {
                    merged = new LinkedHashMap<>(existing);
                }
                merged.put(key, next);
            }
        }
        return merged;
    }

    private static void mergeMax(final Map<FieldReadKey, Integer> into,
                                 final Map<FieldReadKey, Integer> fact) {
        for (final Map.Entry<FieldReadKey, Integer> e : fact.entrySet()) {
            into.merge(e.getKey(), e.getValue(), Math::max);
        }
    }

    // In 3-address Jimple, field reads on the RHS of JAssignStmt.
    private static void collectFieldReads(final Stmt stmt, final Consumer<FieldReadKey> out) {
        if (!(stmt instanceof final JAssignStmt assign)) {
            return;
        }
        final FieldReadKey key = fieldReadKey(assign.getRightOp());
        if (key != null) {
            out.accept(key);
        }
    }

    private static FieldReadKey fieldReadKey(final Object rightOp) {
        if (rightOp instanceof final JInstanceFieldRef inst) {
            return FieldReadKey.instance(inst.getFieldSignature(), inst.getBase());
        }
        if (rightOp instanceof final JStaticFieldRef st) {
            return FieldReadKey.staticField(st.getFieldSignature());
        }
        if (rightOp instanceof JFieldRef) {
            return FieldReadKey.staticField(((JFieldRef) rightOp).getFieldSignature());
        }
        return null;
    }

    /**
     * Instance field read: signature plus Jimple base local. Static field read: signature only.
     */
    private static final class FieldReadKey {
        private final FieldSignature mSig;
        private final Local mBase;

        private FieldReadKey(final FieldSignature sig, final Local base) {
            mSig = sig;
            mBase = base;
        }

        private static FieldReadKey instance(final FieldSignature sig, final Local base) {
            return new FieldReadKey(sig, base);
        }

        private static FieldReadKey staticField(final FieldSignature sig) {
            return new FieldReadKey(sig, null);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof FieldReadKey)) {
                return false;
            }
            final FieldReadKey k = (FieldReadKey) o;
            return Objects.equals(mSig, k.mSig) && Objects.equals(mBase, k.mBase);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mSig, mBase);
        }
    }
}
