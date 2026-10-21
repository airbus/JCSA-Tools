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

import com.airbus.esar.analysis.callflow.InvokeSignaturePattern;
import com.airbus.esar.analysis.pairbalance.CallPairBalanceRule;
import com.airbus.esar.analysis.pairbalance.CallPairBalanceSpec;
import com.airbus.esar.common.JcConstants;

/**
 * Enforces that JCSystem.beginTransaction() commitTransaction() or abortTransaction() on every reachable path.
 */
public final class JcAtomicTransactionCorrectness extends CallPairBalanceRule {
    public static final String RULE_ID = "JcAtomicTransactionBalance";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.ERROR;
    public static final String MESSAGE_UNBALANCED = "javacard.transaction.unbalanced";

    private static final InvokeSignaturePattern BEGIN = InvokeSignaturePattern.staticCallIgnoringReturn(
        JcConstants.JCSYSTEM_CLASS,
        JcConstants.BEGIN_TRANSACTION
    );
    private static final InvokeSignaturePattern COMMIT = InvokeSignaturePattern.staticCallIgnoringReturn(
        JcConstants.JCSYSTEM_CLASS,
        JcConstants.COMMIT_TRANSACTION
    );
    private static final InvokeSignaturePattern ABORT = InvokeSignaturePattern.staticCallIgnoringReturn(
        JcConstants.JCSYSTEM_CLASS,
        JcConstants.ABORT_TRANSACTION
    );

    private static final CallPairBalanceSpec SPEC = CallPairBalanceSpec.of(
        RULE_ID,
        MESSAGE_UNBALANCED,
        List.of(BEGIN),
        List.of(COMMIT, ABORT)
    );

    public JcAtomicTransactionCorrectness() {
        super(SPEC);
    }

    @Override
    protected SarifResultLevel getSarifLevel() {
        return DEFAULT_SEVERITY;
    }
}
