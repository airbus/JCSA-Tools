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

package com.airbus.esar.analysis.signext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;

/** Whether sign-extended vs zero-extended byte semantics change {@code switch} dispatch. */
public final class SwitchOutcome {

    private static final int SIGNED_BYTE_MIN = -128;
    private static final int SIGNED_BYTE_MAX = 127;

    private SwitchOutcome() {
    }

    /**
     * @param sw switch statement
     * @param key switch key expression
     * @param in incoming pollution facts
     * @return {@code false} when every byte lane selects the same case under both promotion models
     */
    public static boolean pollutionMatters(final JSwitchStmt sw,
                                           final Value key,
                                           final SignExtensionFact in) {
        if (!SignExtensionEval.expressesByteDerivedPollution(key, in)) {
            return true;
        }
        final List<Integer> labels = caseLabels(sw);
        if (labels.stream().allMatch(SwitchOutcome::inSignedByteRange)) {
            return false;
        }
        return bucketsDiffer(labels, 8);
    }

    private static List<Integer> caseLabels(final JSwitchStmt sw) {
        final List<Integer> labels = new ArrayList<>();
        for (final IntConstant label : sw.getValues()) {
            labels.add(label.getValue());
        }
        return labels;
    }

    private static boolean inSignedByteRange(final int label) {
        return label >= SIGNED_BYTE_MIN && label <= SIGNED_BYTE_MAX;
    }

    private static boolean bucketsDiffer(final List<Integer> labels, final int bitWidth) {
        final int maxUnsigned = (1 << bitWidth) - 1;
        for (int unsigned = 0; unsigned <= maxUnsigned; unsigned++) {
            final int signed = signExtend(unsigned, bitWidth);
            if (!Objects.equals(matchedLabel(signed, labels), matchedLabel(unsigned, labels))) {
                return true;
            }
        }
        return false;
    }

    private static Integer matchedLabel(final int key, final List<Integer> labels) {
        for (final Integer label : labels) {
            if (key == label) {
                return label;
            }
        }
        return null;
    }

    private static int signExtend(final int unsigned, final int bitWidth) {
        if (bitWidth == 8) {
            return (byte) unsigned;
        }
        return (short) unsigned;
    }
}
