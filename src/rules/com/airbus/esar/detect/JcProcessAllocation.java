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
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import sootup.callgraph.CallGraph;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JNewMultiArrayExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;
import com.airbus.esar.common.JcTypeUtils;

/**
 * Reports object allocations reachable in the CHA call graph rooted at Applet.process(APDU).
 * Constructors and class initializers are not entered; the {@code new} itself is enough.
 * On the JavaCard platform heap allocation during APDU processing is forbidden.
 */
public final class JcProcessAllocation implements ProgramVisitorIf {
    public static final String RULE_ID = "JcProcessAllocation";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.WARNING;
    public static final String MESSAGE_PROCESS_ALLOCATION = "javacard.process.allocation";

    private ClassType mAppletClassType = null;


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
        mAppletClassType = context.getJavaView()
                .getIdentifierFactory()
                .getClassType(JcTypeUtils.APPLET_FQN);
    }

    @Override
    public void endAnalysis(final AnalysisContextIf context, final BugReporterIf bugs) {
        mAppletClassType = null;
    }

    @Override
    public void visitClass(final JavaSootClass clazz,
                           final AnalysisContextIf context,
                           final BugReporterIf bugs) {
        if (mAppletClassType == null) {
            return;
        }
        final JavaView view = context.getJavaView();
        if (!JcTypeUtils.isConcreteApplet(view, clazz, mAppletClassType)) {
            return;
        }

        final MethodSignature processSig = view.getIdentifierFactory().getMethodSignature(
                clazz.getType(),
                "process",
                "void",
                List.of("javacard.framework.APDU")
        );

        checkProcessCallGraph(processSig, view, context, bugs);
    }

    private static void checkProcessCallGraph(final MethodSignature processSig,
                                              final JavaView view,
                                              final AnalysisContextIf context,
                                              final BugReporterIf bugs) {
        final CallGraph cg = context.getCallGraph();
        final Deque<MethodSignature> queue = new ArrayDeque<>();
        final Set<MethodSignature> visited = new HashSet<>();
        queue.add(processSig);

        while (!queue.isEmpty()) {
            final MethodSignature sig = queue.removeFirst();
            if (isInitializer(sig)) {
                continue;
            }
            if (context.isTraversalPackageBlacklisted(sig)) {
                continue;
            }
            if (!visited.add(sig)) {
                continue;
            }

            final Optional<JavaSootMethod> methodOpt = view.getMethod(sig);
            if (methodOpt.isPresent() && methodOpt.get().hasBody()) {
                checkBodyForAllocations(methodOpt.get(), bugs);
            }

            if (cg.containsMethod(sig)) {
                for (final MethodSignature callee : cg.callTargetsFrom(sig)) {
                    if (!isInitializer(callee) && !context.isTraversalPackageBlacklisted(callee)) {
                        queue.addLast(callee);
                    }
                }
            }
        }
    }

    private static boolean isInitializer(final MethodSignature sig) {
        final String name = sig.getName();
        return "<init>".equals(name) || "<clinit>".equals(name);
    }

    private static void checkBodyForAllocations(final JavaSootMethod method,
                                                final BugReporterIf bugs) {
        final Body body = method.getBody();
        for (final Stmt stmt : body.getStmtGraph()) {
            stmt.getUsesAndDefs().forEach(value -> reportIfAllocation(value, method, stmt, bugs));
        }
    }

    private static void reportIfAllocation(final Value value,
                                           final JavaSootMethod method,
                                           final Stmt stmt,
                                           final BugReporterIf bugs) {
        switch (value) {
        case JNewExpr val:
            bugs.report(RULE_ID, DEFAULT_SEVERITY,
                    RuleMessageRef.of(MESSAGE_PROCESS_ALLOCATION,
                            val.getType().toString()),
                    method, stmt);
            break;
        case JNewArrayExpr val:
            bugs.report(RULE_ID, DEFAULT_SEVERITY,
                    RuleMessageRef.of(MESSAGE_PROCESS_ALLOCATION,
                            val.getBaseType() + "[]"),
                    method, stmt);
            break;
        case JNewMultiArrayExpr val:
            bugs.report(RULE_ID, DEFAULT_SEVERITY,
                    RuleMessageRef.of(MESSAGE_PROCESS_ALLOCATION,
                            val.getBaseType() + "[]..."),
                    method, stmt);
            break;
        default:
            break;
        }
    }
}
