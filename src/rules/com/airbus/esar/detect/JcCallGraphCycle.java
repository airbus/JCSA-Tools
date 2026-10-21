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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import sootup.callgraph.CallGraph;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

/**
 * Notes methods that participate in multi-node strongly connected component of the CHA call graph
 */
public final class JcCallGraphCycle implements ProgramVisitorIf {

    public static final String RULE_ID = "JcCallGraphCycle";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.WARNING;
    public static final String MESSAGE_CALL_GRAPH_CYCLE = "javacard.recursion.callGraphCycle";


    @Override
    public String ruleId() {
        return RULE_ID;
    }

    @Override
    public SarifResultLevel severity() {
        return DEFAULT_SEVERITY;
    }

    @Override
    public void startAnalysis(final AnalysisContextIf context, final BugReporterIf bugs) {
        final CallGraph cg = context.getCallGraph();
        final Set<MethodSignature> nodes = cg.getMethodSignatures();
        if (nodes.size() < 2) {
            return;
        }
        final Map<MethodSignature, List<MethodSignature>> adj = new HashMap<>();
        for (final MethodSignature v : nodes) {
            final List<MethodSignature> succ = new ArrayList<>();
            for (final MethodSignature t : cg.callTargetsFrom(v)) {
                if (!nodes.contains(t)) {
                    continue;
                }
                succ.add(t);
            }
            adj.put(v, succ);
        }
        final List<Set<MethodSignature>> multiNodeSccs = TarjanMultiNodeScc.find(adj, nodes);
        if (multiNodeSccs.isEmpty()) {
            return;
        }
        final JavaView view = context.getJavaView();
        for (final Set<MethodSignature> scc : multiNodeSccs) {
            final List<MethodSignature> reportable = scc.stream()
                    .filter(sig -> !context.isTraversalPackageBlacklisted(sig))
                    .collect(Collectors.toList());
            if (reportable.isEmpty()) {
                continue;
            }
            final String cycleText = scc.stream()
                    .map(MethodSignature::toString)
                    .sorted()
                    .collect(Collectors.joining(", "));
            for (final MethodSignature sig : reportable) {
                final Optional<JavaSootMethod> m = view.getMethod(sig);
                if (m.isEmpty() || !m.get().hasBody()) {
                    continue;
                }
                bugs.report(RULE_ID, DEFAULT_SEVERITY,
                        RuleMessageRef.of(MESSAGE_CALL_GRAPH_CYCLE, cycleText), m.get());
            }
        }
    }

    private static final class TarjanMultiNodeScc {

        private TarjanMultiNodeScc() {
        }

        private static final class State {
            private final Map<MethodSignature, Integer> mIndex = new HashMap<>();
            private final Map<MethodSignature, Integer> mLowlink = new HashMap<>();
            private final Deque<MethodSignature> mStack = new ArrayDeque<>();
            private final Set<MethodSignature> mOnStack = new HashSet<>();
            private final List<Set<MethodSignature>> mSccs = new ArrayList<>();
            private final int[] mNextIndex = {0};
        }

        private static List<Set<MethodSignature>> find(final Map<MethodSignature, List<MethodSignature>> adj,
                                                       final Set<MethodSignature> nodes) {
            final State s = new State();
            for (final MethodSignature v : nodes) {
                if (s.mIndex.containsKey(v)) {
                    continue;
                }
                strongConnect(v, adj, s);
            }
            return s.mSccs;
        }

        private static void strongConnect(final MethodSignature v,
                                          final Map<MethodSignature, List<MethodSignature>> adj,
                                          final State s) {
            s.mIndex.put(v, s.mNextIndex[0]);
            s.mLowlink.put(v, s.mNextIndex[0]);
            s.mNextIndex[0]++;
            s.mStack.push(v);
            s.mOnStack.add(v);

            for (final MethodSignature w : adj.getOrDefault(v, Collections.emptyList())) {
                if (!s.mIndex.containsKey(w)) {
                    strongConnect(w, adj, s);
                    s.mLowlink.put(v, Math.min(s.mLowlink.get(v), s.mLowlink.get(w)));
                } else if (s.mOnStack.contains(w)) {
                    s.mLowlink.put(v, Math.min(s.mLowlink.get(v), s.mIndex.get(w)));
                }
            }

            if (Objects.equals(s.mLowlink.get(v), s.mIndex.get(v))) {
                final Set<MethodSignature> scc = new HashSet<>();
                while (true) {
                    final MethodSignature w = s.mStack.pop();
                    s.mOnStack.remove(w);
                    scc.add(w);
                    if (w.equals(v)) {
                        break;
                    }
                }
                if (scc.size() >= 2) {
                    s.mSccs.add(scc);
                }
            }
        }
    }
}
