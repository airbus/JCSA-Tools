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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;

/**
 * Bounded powerset lattice for integer constant propagation.
 * Heros top is "no info" (meet identity). Unknown is over-bound / non-constant
 * (must be stored as Gen, not AllTop — Heros drops AllTop jump functions).
 * Bottom is unreachable.
 */
public final class ConstantLatticeValue {

    /** Max distinct constants retained before collapsing to unknown. */
    public static final int BOUND = 8;

    private static final ConstantLatticeValue TOP = new ConstantLatticeValue(null, false, true, false);
    private static final ConstantLatticeValue BOTTOM = new ConstantLatticeValue(null, true, false, false);
    private static final ConstantLatticeValue UNKNOWN = new ConstantLatticeValue(null, false, false, true);

    private final int[] mValues;
    private final boolean mBottom;
    private final boolean mTop;
    private final boolean mUnknown;

    private ConstantLatticeValue(final int[] values,
                                 final boolean bottom,
                                 final boolean top,
                                 final boolean unknown) {
        mValues = values;
        mBottom = bottom;
        mTop = top;
        mUnknown = unknown;
    }

    public static ConstantLatticeValue top() {
        return TOP;
    }

    public static ConstantLatticeValue bottom() {
        return BOTTOM;
    }

    public static ConstantLatticeValue unknown() {
        return UNKNOWN;
    }

    public static ConstantLatticeValue of(final int value) {
        return new ConstantLatticeValue(new int[] {value}, false, false, false);
    }

    public static ConstantLatticeValue of(final Collection<Integer> values) {
        Objects.requireNonNull(values, "values");
        if (values.isEmpty()) {
            return BOTTOM;
        }
        final TreeSet<Integer> sorted = new TreeSet<>(values);
        if (sorted.size() > BOUND) {
            return UNKNOWN;
        }
        final int[] arr = new int[sorted.size()];
        int i = 0;
        for (final Integer v : sorted) {
            arr[i++] = v.intValue();
        }
        return new ConstantLatticeValue(arr, false, false, false);
    }

    public boolean isTop() {
        return mTop;
    }

    public boolean isBottom() {
        return mBottom;
    }

    public boolean isUnknown() {
        return mUnknown;
    }

    public boolean isFinite() {
        return !mTop && !mBottom && !mUnknown;
    }

    /**
     * @return true when exactly one constant is known
     */
    public boolean isConstant() {
        return isFinite() && mValues.length == 1;
    }

    public OptionalInt asInt() {
        if (!isConstant()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(mValues[0]);
    }

    /**
     * @return known constants, or empty when top/bottom/unknown
     */
    public Set<Integer> asInts() {
        if (!isFinite()) {
            return Set.of();
        }
        final TreeSet<Integer> out = new TreeSet<>();
        for (final int v : mValues) {
            out.add(Integer.valueOf(v));
        }
        return Collections.unmodifiableSet(out);
    }

    public ConstantLatticeValue meet(final ConstantLatticeValue other) {
        Objects.requireNonNull(other, "other");
        if (mBottom || other.mBottom) {
            return BOTTOM;
        }
        if (mUnknown || other.mUnknown) {
            return UNKNOWN;
        }
        if (mTop) {
            return other;
        }
        if (other.mTop) {
            return this;
        }
        final TreeSet<Integer> union = new TreeSet<>();
        for (final int v : mValues) {
            union.add(Integer.valueOf(v));
        }
        for (final int v : other.mValues) {
            union.add(Integer.valueOf(v));
        }
        if (union.size() > BOUND) {
            return UNKNOWN;
        }
        if (union.size() == mValues.length) {
            boolean same = true;
            int i = 0;
            for (final Integer v : union) {
                if (mValues[i++] != v.intValue()) {
                    same = false;
                    break;
                }
            }
            if (same) {
                return this;
            }
        }
        return of(union);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConstantLatticeValue)) {
            return false;
        }
        final ConstantLatticeValue that = (ConstantLatticeValue) o;
        return mBottom == that.mBottom
                && mTop == that.mTop
                && mUnknown == that.mUnknown
                && Arrays.equals(mValues, that.mValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Boolean.valueOf(mBottom),
                Boolean.valueOf(mTop),
                Boolean.valueOf(mUnknown),
                Integer.valueOf(Arrays.hashCode(mValues)));
    }

    @Override
    public String toString() {
        if (mBottom) {
            return "bottom";
        }
        if (mTop) {
            return "top";
        }
        if (mUnknown) {
            return "unknown";
        }
        return Arrays.toString(mValues);
    }
}
