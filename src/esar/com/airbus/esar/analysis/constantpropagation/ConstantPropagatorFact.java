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

package com.airbus.esar.analysis.constantpropagation;

import sootup.core.jimple.basic.Value;

/**
 * IDE fact: a {@link Value} (typically a local) whose constant int value is tracked.
 */
public final class ConstantPropagatorFact {

    private static final ConstantPropagatorFact ZERO = new ConstantPropagatorFact(null);

    private final Value mVariable;

    private ConstantPropagatorFact(final Value variable) {
        mVariable = variable;
    }

    public static ConstantPropagatorFact zero() {
        return ZERO;
    }

    public static ConstantPropagatorFact of(final Value variable) {
        return new ConstantPropagatorFact(variable);
    }

    public boolean isZero() {
        return mVariable == null;
    }

    public Value getVariable() {
        return mVariable;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConstantPropagatorFact)) {
            return false;
        }
        final ConstantPropagatorFact that = (ConstantPropagatorFact) o;
        if (isZero()) {
            return that.isZero();
        }
        if (that.isZero()) {
            return false;
        }
        return mVariable == that.mVariable || mVariable.equivTo(that.mVariable);
    }

    @Override
    public int hashCode() {
        if (isZero()) {
            return 0;
        }
        return System.identityHashCode(mVariable);
    }
}
