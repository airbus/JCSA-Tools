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

package com.airbus.esar.internal.engine;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import com.airbus.esar.detect.AnalysisContextIf;
import com.airbus.esar.detect.EsarFinding;
import com.airbus.esar.detect.ProgramVisitorIf;
import com.airbus.esar.detect.SarifResultLevel;
import com.airbus.esar.internal.i18n.RuleMessageResolverIf;

/**
 * Global SootUp setup (view, hierarchy, optional CG/ICFG) and ProgramVisitorIf traversal (per class, per
 * method, then each Stmt via ProgramVisitorIf.visitStmt, then each Jimple Value in that stmt
 * via ProgramVisitorIf.visitExpr).
 *
 * Optional traversal FQN whitelist: when non-null, only classes whose fully qualified name is in the set are
 * visited. A null whitelist visits every resolvable class that is not package-blacklisted. An empty set visits no
 * classes. Used by tests to speed debugging.
 *
 * Package blacklist: classes whose FQN starts with a listed prefix are never visited (defaults cover JDK / JCRE).
 */
public final class AnalysisEngine {
    private final List<AnalysisInputLocation> mInputLocations;
    private final List<MethodSignature> mEntryPoints;
    private final Set<String> mTraversalClassFqnWhitelist;
    private final List<String> mTraversalPackageBlacklist;
    private final SharedSolverRegistry mSharedSolvers;

    private boolean mPrepared;
    private AnalysisContext mContext;
    private SootAnalytics mAnalytics;
    private List<EsarFinding> mFindingsLastTraverse = List.of();
    private List<DetectorTiming> mDetectorTimingsLastTraverse = List.of();
    private final RuleMessageResolverIf mMessageResolver;

    /**
     * @param analysisLocations classpath or other SootUp input locations
     * @param entryPointSignatures method signatures used as call-graph roots when non-empty (optional for traversal)
     * @param messageResolver resolves rule messages during traverse
     */
    public AnalysisEngine(final List<? extends AnalysisInputLocation> analysisLocations,
                          final List<MethodSignature> entryPointSignatures,
                          final RuleMessageResolverIf messageResolver) {
        this(analysisLocations, entryPointSignatures, messageResolver, null, TraversalPackageBlacklist.defaults());
    }

    /**
     * @param analysisLocations classpath or other SootUp input locations
     * @param entryPointSignatures method signatures used as call-graph roots when non-empty (optional for traversal)
     * @param messageResolver resolves rule messages during traverse
     * @param traversalClassFqnWhitelist when non-null, only these declaring-type FQNs (and exact matches for nested
     *     names) are traversed; when null, all non-blacklisted classes in the view are traversed. Empty set means no
     *     class is visited.
     */
    public AnalysisEngine(final List<? extends AnalysisInputLocation> analysisLocations,
                          final List<MethodSignature> entryPointSignatures,
                          final RuleMessageResolverIf messageResolver,
                          final Set<String> traversalClassFqnWhitelist) {
        this(analysisLocations,
                entryPointSignatures,
                messageResolver,
                traversalClassFqnWhitelist,
                TraversalPackageBlacklist.defaults());
    }

    /**
     * @param analysisLocations classpath or other SootUp input locations
     * @param entryPointSignatures method signatures used as call-graph roots when non-empty (optional for traversal)
     * @param messageResolver resolves rule messages during traverse
     * @param traversalClassFqnWhitelist when non-null, only these declaring-type FQNs are traversed after blacklist
     * @param traversalPackageBlacklist package prefixes excluded from traversal (empty disables blacklist)
     */
    public AnalysisEngine(final List<? extends AnalysisInputLocation> analysisLocations,
                          final List<MethodSignature> entryPointSignatures,
                          final RuleMessageResolverIf messageResolver,
                          final Set<String> traversalClassFqnWhitelist,
                          final List<String> traversalPackageBlacklist) {
        mInputLocations = new ArrayList<>(analysisLocations);
        mEntryPoints = new ArrayList<>(entryPointSignatures);
        mMessageResolver = Objects.requireNonNull(messageResolver, "messageResolver");
        if (traversalClassFqnWhitelist == null) {
            mTraversalClassFqnWhitelist = null;
        } else {
            mTraversalClassFqnWhitelist = Collections.unmodifiableSet(traversalClassFqnWhitelist);
        }
        mTraversalPackageBlacklist = TraversalPackageBlacklist.copyOf(traversalPackageBlacklist);
        mSharedSolvers = new SharedSolverRegistry();
    }

    private void runTraversal(final ProgramVisitorIf visitor, final ConcurrentBugReporter bugs) {
        final AnalysisContextIf ctx = mContext;
        visitor.startAnalysis(ctx, bugs);
        final List<JavaSootClass> classes = ctx.getJavaView().getClasses()
                .sorted(Comparator.comparing(c -> c.getType().getFullyQualifiedName()))
                .filter(this::shouldTraverseClass)
                .collect(Collectors.toList());
        for (JavaSootClass clazz : classes) {
            visitor.visitClass(clazz, ctx, bugs);
            final List<JavaSootMethod> methods = clazz.getMethods().stream()
                    .filter(JavaSootMethod::isConcrete)
                    .filter(JavaSootMethod::hasBody)
                    .sorted(Comparator.comparing(m -> m.getSignature().toString()))
                    .collect(Collectors.toList());
            for (JavaSootMethod method : methods) {
                final Body body = method.getBody();
                visitor.visitMethod(method, body, ctx, bugs);
                for (Stmt stmt : body.getStmtGraph()) {
                    visitor.visitStmt(method, body, stmt, ctx, bugs);
                    visitExprTree(visitor, method, body, stmt, ctx, bugs);
                }
            }
        }
        visitor.endAnalysis(ctx, bugs);
    }

    private boolean shouldTraverseClass(final JavaSootClass clazz) {
        final String fqn = clazz.getType().getFullyQualifiedName();
        if (TraversalPackageBlacklist.matches(fqn, mTraversalPackageBlacklist)) {
            return false;
        }
        if (mTraversalClassFqnWhitelist == null) {
            return true;
        }
        return mTraversalClassFqnWhitelist.contains(fqn);
    }

    private static void visitExprTree(final ProgramVisitorIf visitor, final JavaSootMethod method, final Body body,
            final Stmt stmt, final AnalysisContextIf ctx, final ConcurrentBugReporter bugs) {
        stmt.getUsesAndDefs().forEach(root -> visitExprNode(visitor, method, body, stmt, root, ctx, bugs));
    }

    private static void visitExprNode(final ProgramVisitorIf visitor, final JavaSootMethod method, final Body body,
            final Stmt stmt, final Value value, final AnalysisContextIf ctx, final ConcurrentBugReporter bugs) {
        visitor.visitExpr(method, body, stmt, value, ctx, bugs);
        value.getUses().forEach(child -> visitExprNode(visitor, method, body, stmt, child, ctx, bugs));
    }

    /** Builds view, hierarchy, optional call graph, and analysis context. Repeated calls rebuild the snapshot. */
    public synchronized void prepare() {
        mAnalytics = new SootAnalytics(mInputLocations, mEntryPoints);
        mContext = new AnalysisContext(mAnalytics, mTraversalPackageBlacklist);
        mFindingsLastTraverse = List.of();
        mDetectorTimingsLastTraverse = List.of();
        mPrepared = true;
    }

    /** @return context from the last prepare() */
    public synchronized AnalysisContextIf getContext() {
        if (!mPrepared) {
            throw new IllegalStateException("Call prepare() before getContext().");
        }
        return mContext;
    }

    /** @return findings from the last traverse (empty until then) */
    public synchronized List<EsarFinding> getFindings() {
        return mFindingsLastTraverse;
    }

    /**
     * @return per-detector CPU timings from the last traverse (empty until then); sorted by total CPU descending
     */
    public synchronized List<DetectorTiming> getDetectorTimings() {
        return mDetectorTimingsLastTraverse;
    }

    /**
     * Requires prepare().
     *
     * @param visitors callbacks to invoke
     */
    public void traverse(final ProgramVisitorIf... visitors) {
        traverse(List.of(visitors), Map.of());
    }

    /**
     * Requires prepare().
     *
     * @param visitors callbacks to invoke
     */
    public synchronized void traverse(final Iterable<? extends ProgramVisitorIf> visitors) {
        traverse(visitors, Map.of());
    }

    /**
     * Requires prepare().
     *
     * @param visitors callbacks to invoke
     * @param severityOverrides rule id to configured severity (empty when none)
     */
    public synchronized void traverse(final Iterable<? extends ProgramVisitorIf> visitors,
                                      final Map<String, SarifResultLevel> severityOverrides) {
        if (!mPrepared) {
            throw new IllegalStateException("Call prepare() before traverse().");
        }

        mSharedSolvers.clear();

        final ThreadCpuTimer cpuTimer = ThreadCpuTimer.create();
        final List<ProgramVisitorIf> visitorList = new ArrayList<>();
        visitors.forEach(visitorList::add);

        // Order of detectors must say same or diagnostics will show incorrect results
        final List<Long> initCpuNanos = new ArrayList<>(visitorList.size());
        for (final ProgramVisitorIf visitor : visitorList) {
            final long start = cpuTimer.cpuTimeNanos();
            visitor.initAnalysis(mSharedSolvers, mContext);
            initCpuNanos.add(cpuTimer.cpuTimeNanos() - start);
        }

        final ConcurrentBugReporter bugs =
                new ConcurrentBugReporter(mMessageResolver, severityOverrides);
        final List<DetectorTiming> timings = new ArrayList<>(visitorList.size());
        for (int i = 0; i < visitorList.size(); i++) {
            final ProgramVisitorIf visitor = visitorList.get(i);
            final long start = cpuTimer.cpuTimeNanos();
            runTraversal(visitor, bugs);
            final long traverseNanos = cpuTimer.cpuTimeNanos() - start;
            timings.add(DetectorTiming.of(visitor.ruleId(), initCpuNanos.get(i), traverseNanos));
        }

        timings.sort(Comparator.comparingLong(DetectorTiming::getTotalCpuNanos).reversed());
        mDetectorTimingsLastTraverse = List.copyOf(timings);
        mFindingsLastTraverse = bugs.snapshot();
    }

    /**
     * Reads current-thread CPU time when the JVM supports it; otherwise falls back to wall time.
     */
    private static final class ThreadCpuTimer {
        private final ThreadMXBean mBean;
        private final boolean mCpuTime;

        private ThreadCpuTimer(final ThreadMXBean bean, final boolean cpuTime) {
            mBean = bean;
            mCpuTime = cpuTime;
        }

        private static ThreadCpuTimer create() {
            final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            if (bean.isCurrentThreadCpuTimeSupported()) {
                if (!bean.isThreadCpuTimeEnabled()) {
                    bean.setThreadCpuTimeEnabled(true);
                }
                return new ThreadCpuTimer(bean, true);
            }
            return new ThreadCpuTimer(bean, false);
        }

        private long cpuTimeNanos() {
            if (mCpuTime) {
                return mBean.getCurrentThreadCpuTime();
            }
            return System.nanoTime();
        }
    }
}
