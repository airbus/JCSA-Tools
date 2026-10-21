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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import sootup.core.jimple.common.stmt.Stmt;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import com.airbus.esar.detect.BugReporterIf;
import com.airbus.esar.detect.EsarFinding;
import com.airbus.esar.detect.RuleMessageRef;
import com.airbus.esar.detect.SarifResultLevel;
import com.airbus.esar.internal.i18n.ResolvedRuleMessage;
import com.airbus.esar.internal.i18n.RuleMessageResolverIf;

/**
 * Thread-safe BugReporterIf; atomic append; snapshot after traversal.
 */
public final class ConcurrentBugReporter implements BugReporterIf {

    private final RuleMessageResolverIf mResolver;
    private final Map<String, SarifResultLevel> mSeverityOverrides;
    private final ConcurrentLinkedQueue<EsarFinding> mQueue = new ConcurrentLinkedQueue<>();

    public ConcurrentBugReporter(final RuleMessageResolverIf resolver) {
        this(resolver, Map.of());
    }

    /**
     * @param resolver resolves rule message text
     * @param severityOverrides rule id to configured severity (empty when none)
     */
    public ConcurrentBugReporter(final RuleMessageResolverIf resolver,
                                 final Map<String, SarifResultLevel> severityOverrides) {
        mResolver = Objects.requireNonNull(resolver, "resolver");
        mSeverityOverrides = Map.copyOf(Objects.requireNonNull(severityOverrides, "severityOverrides"));
    }

    @Override
    public void report(final String ruleId,
                       final SarifResultLevel level,
                       final RuleMessageRef message,
                       final JavaSootClass clazz) {
        Objects.requireNonNull(ruleId, "ruleId");
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(message, "message");
        final ResolvedRuleMessage resolved = mResolver.resolve(message);
        mQueue.add(new EsarFinding(
                ruleId,
                resolveLevel(ruleId, level),
                resolved.getMessageId(),
                resolved.getText(),
                clazz.getType().getFullyQualifiedName(),
                Optional.empty(),
                Optional.empty()));
    }

    @Override
    public void report(final String ruleId,
                       final SarifResultLevel level,
                       final RuleMessageRef message,
                       final JavaSootMethod method) {
        Objects.requireNonNull(ruleId, "ruleId");
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(method, "method");
        final ResolvedRuleMessage resolved = mResolver.resolve(message);
        mQueue.add(new EsarFinding(
                ruleId,
                resolveLevel(ruleId, level),
                resolved.getMessageId(),
                resolved.getText(),
                method.getDeclClassType().getFullyQualifiedName(),
                Optional.of(method.getSignature().toString()),
                FindingLocations.regionForMethod(method)));
    }

    @Override
    public void report(final String ruleId,
                       final SarifResultLevel level,
                       final RuleMessageRef message,
                       final JavaSootMethod method,
                       final Stmt stmt) {
        Objects.requireNonNull(ruleId, "ruleId");
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(stmt, "stmt");
        final ResolvedRuleMessage resolved = mResolver.resolve(message);
        mQueue.add(new EsarFinding(
                ruleId,
                resolveLevel(ruleId, level),
                resolved.getMessageId(),
                resolved.getText(),
                method.getDeclClassType().getFullyQualifiedName(),
                Optional.of(method.getSignature().toString()),
                FindingLocations.regionForStmt(stmt)));
    }

    @Override
    public void report(final EsarFinding finding) {
        Objects.requireNonNull(finding, "finding");
        final SarifResultLevel resolved = resolveLevel(finding.getRuleId(), finding.getLevel());
        if (resolved == finding.getLevel()) {
            mQueue.add(finding);
        } else {
            mQueue.add(finding.withLevel(resolved));
        }
    }

    /**
     * @return an unmodifiable snapshot of findings recorded so far (order is insertion order)
     */
    public List<EsarFinding> snapshot() {
        return List.copyOf(new ArrayList<>(mQueue));
    }

    private SarifResultLevel resolveLevel(final String ruleId, final SarifResultLevel reported) {
        return mSeverityOverrides.getOrDefault(ruleId, reported);
    }
}
