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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import heros.InterproceduralCFG;

import sootup.analysis.interprocedural.ide.JimpleIDESolver;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.types.PrimitiveType;
import sootup.java.core.JavaSootMethod;
import com.airbus.esar.analysis.constantpropagation.ConstantLatticeValue;
import com.airbus.esar.analysis.constantpropagation.ConstantPropagationSolverFactory;
import com.airbus.esar.analysis.constantpropagation.ConstantPropagatorFact;
import com.airbus.esar.common.Iso78164StatusWord;
import com.airbus.esar.common.JcConstants;
import com.airbus.esar.jcsolvers.JcSharedSolvers;
import com.airbus.esar.soot.InvokeMatchers;
import com.airbus.esar.soot.StaticInvokeSignature;

/**
 * Flags {@code ISOException.throwIt} calls whose SW1-SW2 is not valid per ISO/IEC 7816-4.
 */
public final class JcIsoExceptionInvalidSw
        implements ProgramVisitorIf, ConfigurableDetector<JcIsoExceptionInvalidSw.Config> {

    public static final String RULE_ID = "JcIsoExceptionInvalidSw";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.ERROR;
    public static final String MESSAGE_INVALID_SW1 = "javacard.isoException.invalidSw1";
    public static final String MESSAGE_INVALID_SW2 = "javacard.isoException.invalidSw2";
    public static final String MESSAGE_UNKNOWN_SW = "javacard.isoException.unknownSw";

    /**
     * Typed YAML settings for this rule.
     */
    public static final class Config {
        private List<Integer> mAllowedCustomSws = List.of();

        public List<Integer> getAllowedCustomSws() {
            return mAllowedCustomSws;
        }

        public void setAllowedCustomSws(final List<Integer> value) {
            if (value == null) {
                mAllowedCustomSws = List.of();
            } else {
                mAllowedCustomSws = List.copyOf(value);
            }
        }
    }

    private static final StaticInvokeSignature ISO_EXCEPTION_THROW_IT = StaticInvokeSignature.ignoringReturn(
            JcConstants.ISO_EXCEPTION_CLASS,
            JcConstants.THROW_IT,
            PrimitiveType.getShort());

    private JimpleIDESolver<
            ConstantPropagatorFact, ConstantLatticeValue, InterproceduralCFG<Stmt, SootMethod>> mSolver;

    private Set<Integer> mAllowedCustomSws = Set.of();


    @Override
    public String ruleId() {
        return RULE_ID;
    }

    @Override
    public SarifResultLevel severity() {
        return DEFAULT_SEVERITY;
    }

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    @Override
    public void configure(final Config config) {
        mAllowedCustomSws = config.getAllowedCustomSws().stream()
                .map(sw -> sw & 0xFFFF)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void initAnalysis(final SharedSolverRegistryIf registry, final AnalysisContextIf context) {
        mSolver = registry.lazyRegister(
                JcSharedSolvers.JC_CONSTANT_PROPAGATION,
                () -> ConstantPropagationSolverFactory.buildSolver(context));
    }

    @Override
    public void visitMethod(final JavaSootMethod method,
                            final Body body,
                            final AnalysisContextIf context,
                            final BugReporterIf bugs) {
        if (!context.isMethodInEntryPointReachabilitySlice(method.getSignature())) {
            return;
        }
        for (final Stmt stmt : body.getStmts()) {
            final AbstractInvokeExpr expr = InvokeMatchers.getMatchingStaticInvoke(stmt, ISO_EXCEPTION_THROW_IT);
            if (expr == null) {
                continue;
            }
            if (IsoExceptionStatusWordChecker.isUnknownAt(mSolver, stmt, expr)) {
                bugs.report(RULE_ID, DEFAULT_SEVERITY, RuleMessageRef.of(MESSAGE_UNKNOWN_SW), method, stmt);
                continue;
            }
            final var violation = IsoExceptionStatusWordChecker.violationAt(mSolver, stmt, expr);
            if (violation.isEmpty()) {
                continue;
            }
            final Iso78164StatusWord.Violation v = violation.get();
            if (mAllowedCustomSws.contains(v.statusWord() & 0xFFFF)) {
                continue;
            }
            final String formatted = Iso78164StatusWord.formatStatusWord(v.statusWord());
            if (v.kind() == Iso78164StatusWord.ViolationKind.INVALID_SW1) {
                bugs.report(RULE_ID, DEFAULT_SEVERITY,
                        RuleMessageRef.of(MESSAGE_INVALID_SW1,
                                String.format("0x%02X", Iso78164StatusWord.sw1Of(v.statusWord()))),
                        method, stmt);
            } else {
                bugs.report(RULE_ID, DEFAULT_SEVERITY,
                        RuleMessageRef.of(MESSAGE_INVALID_SW2, formatted), method, stmt);
            }
        }
    }
}
