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

import java.util.Objects;
import java.util.Optional;

/**
 * One reported issue, suitable for later SARIF export. Immutable.
 */
public final class EsarFinding {

    private final String mRuleId;
    private final SarifResultLevel mLevel;
    private final String mMessageId;
    private final String mMessage;
    private final String mClassSignature;
    private final Optional<String> mMethodSignature;
    private final Optional<SourceRegion> mRegion;

    /**
     * @param ruleId stable rule identifier
     * @param level SARIF-aligned severity
     * @param messageId stable message key (SARIF {@code message.id})
     * @param message resolved user-visible text
     * @param classSignature class signature
     * @param methodSignature declaring method, if any (logical location)
     * @param region source line/column bounds when known
     */
    public EsarFinding(final String ruleId,
                       final SarifResultLevel level,
                       final String messageId,
                       final String message,
                       final String classSignature,
                       final Optional<String> methodSignature,
                       final Optional<SourceRegion> region) {
        mRuleId = Objects.requireNonNull(ruleId, "ruleId");
        mLevel = Objects.requireNonNull(level, "level");
        mMessageId = Objects.requireNonNull(messageId, "messageId");
        mMessage = Objects.requireNonNull(message, "message");
        mClassSignature = Objects.requireNonNull(classSignature, "classSignature");
        mMethodSignature = Objects.requireNonNull(methodSignature, "methodSignature");
        mRegion = Objects.requireNonNull(region, "region");
    }

    public String getRuleId() {
        return mRuleId;
    }

    public SarifResultLevel getLevel() {
        return mLevel;
    }

    /**
     * @return stable message id (matches RuleMessageRef key when keys are unique)
     */
    public String getMessageId() {
        return mMessageId;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getClassSignature() {
        return mClassSignature;
    }

    public Optional<String> getMethodSignature() {
        return mMethodSignature;
    }

    public Optional<SourceRegion> getRegion() {
        return mRegion;
    }

    /**
     * @param level replacement SARIF severity
     * @return finding with the given level
     */
    public EsarFinding withLevel(final SarifResultLevel level) {
        if (mLevel == level) {
            return this;
        }
        return new EsarFinding(
                mRuleId,
                level,
                mMessageId,
                mMessage,
                mClassSignature,
                mMethodSignature,
                mRegion);
    }
}
