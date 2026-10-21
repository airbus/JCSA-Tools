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
import java.util.Map;

import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.PrimitiveType;
import sootup.java.core.JavaSootMethod;
import com.airbus.esar.analysis.callflow.ForwardStmtWorklist;
import com.airbus.esar.analysis.signext.SignExtensionEval;
import com.airbus.esar.analysis.signext.SignExtensionFact;
import com.airbus.esar.analysis.signext.SignExtensionPollution;
import com.airbus.esar.analysis.signext.SignExtensionSinks;
import com.airbus.esar.analysis.signext.SignExtensionValueInfo;

/**
 * Warns when byte sign-extension pollution reaches a wider primitive consumer without a
 * sanitizing mask. Forward dataflow over pollution bit-masks; reports at consumption sinks only.
 */
public final class JcImplicitIntegralWidening implements ProgramVisitorIf {

    public static final String RULE_ID = "JcImplicitIntegralWidening";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.WARNING;
    public static final String MESSAGE_IMPLICIT_WIDEN = "javacard.integral.implicitWiden.assignment";


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
        analyseMethod(method, body, bugs);
    }

    private static void analyseMethod(final JavaSootMethod method,
                                      final Body body,
                                      final BugReporterIf bugs) {
        final StmtGraph<?> graph = body.getStmtGraph();
        final Stmt entry = graph.getStartingStmt();
        if (entry == null) {
            return;
        }
        final Map<Stmt, SignExtensionFact> seeds = new HashMap<>();
        seeds.put(entry, SignExtensionFact.EMPTY);
        final Map<Stmt, SignExtensionFact> fixpointIn = ForwardStmtWorklist.runFromSeeds(graph, seeds,
                new ForwardStmtWorklist.Transfer<SignExtensionFact>() {
                    @Override
                    public SignExtensionFact normal(final Stmt stmt, final SignExtensionFact in) {
                        return transfer(stmt, in);
                    }

                    @Override
                    public SignExtensionFact exception(final Stmt stmt, final SignExtensionFact in) {
                        return in;
                    }
                },
                SignExtensionFact::joinIfImproves,
                (tail, after) -> {
                },
                ForwardStmtWorklist.DEFAULT_STEP_CAP,
                body);

        for (final Stmt stmt : body.getStmts()) {
            final SignExtensionFact in = fixpointIn.get(stmt);
            if (in != null) {
                SignExtensionSinks.reportStmt(method, stmt, in,
                        (m, s) -> report(m, s, bugs));
            }
        }
    }

    private static SignExtensionFact transfer(final Stmt stmt, final SignExtensionFact in) {
        if (stmt instanceof final JIdentityStmt identity) {
            if (identity.getLeftOp() instanceof final Local local
                    && local.getType().equals(PrimitiveType.getByte())) {
                return in.withLocalAssign(local,
                        SignExtensionValueInfo.ofSignExt(SignExtensionPollution.BYTE_WIDEN));
            }
            return in;
        }
        if (!(stmt instanceof JAssignStmt assign)) {
            return in;
        }
        if (!(assign.getLeftOp() instanceof Local local)) {
            return in;
        }
        return in.withLocalAssign(local, SignExtensionEval.assignLocalInfo(local, assign.getRightOp(), in));
    }

    private static void report(final JavaSootMethod method,
                               final Stmt stmt,
                               final BugReporterIf bugs) {
        bugs.report(RULE_ID, DEFAULT_SEVERITY, RuleMessageRef.of(MESSAGE_IMPLICIT_WIDEN), method, stmt);
    }
}
