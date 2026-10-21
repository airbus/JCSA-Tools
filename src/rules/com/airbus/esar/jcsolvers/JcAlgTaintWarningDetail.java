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

package com.airbus.esar.jcsolvers;

import java.util.Optional;

import sootup.core.jimple.common.constant.IntConstant;

import com.airbus.esar.analysis.primitivetaint.TrackedPrimitiveFact;

/**
 * Algorithm string for rule messages (ALG_*), optional (numeric N) when carrier is the spec literal.
 */
public final class JcAlgTaintWarningDetail {

    private JcAlgTaintWarningDetail() {
    }

    /**
     * @param f fact from IFDS query
     * @return message fragment for RuleMessageRef, or empty
     */
    public static Optional<String> messageForFact(final TrackedPrimitiveFact<JcAlgTaintTag> f) {
        final JcAlgTaintTag tag = f.getTag();
        final String base = tag.name();
        if (!(f.getCarrier() instanceof IntConstant)) {
            return Optional.of(base);
        }
        final int v = ((IntConstant) f.getCarrier()).getValue();
        if (v == tag.getLiteral()) {
            return Optional.of(base + " (numeric " + v + ")");
        }
        return Optional.of(base);
    }
}
