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

package com.airbus.esar.analysis.primitivetaint;

import java.util.Objects;

import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.constant.FloatConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.signatures.FieldSignature;

/**
 * IFDS fact: a Value may carry a tracked primitive labelled by L.
 *
 * @param <L> label type (consistent equals/hashCode)
 */
public final class TrackedPrimitiveFact<L> {

    private static final TrackedPrimitiveFact<?> ZERO = new TrackedPrimitiveFact<>(null, null);

    private final L mTag;
    private final Value mCarrier;

    private TrackedPrimitiveFact(final L tag, final Value carrier) {
        mTag = tag;
        mCarrier = carrier;
    }

    @SuppressWarnings("unchecked")
    public static <L> TrackedPrimitiveFact<L> zero() {
        return (TrackedPrimitiveFact<L>) ZERO;
    }

    public static <L> TrackedPrimitiveFact<L> of(final L tag, final Value carrier) {
        return new TrackedPrimitiveFact<>(tag, carrier);
    }

    public boolean isZero() {
        return mCarrier == null;
    }

    public L getTag() {
        return mTag;
    }

    /**
     * @return the Value carrying the tainted primitive, or {@code null} for the zero fact — callers must check
     *     {@link #isZero()} before use
     */
    public Value getCarrier() {
        return mCarrier;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TrackedPrimitiveFact)) {
            return false;
        }
        final TrackedPrimitiveFact<?> that = (TrackedPrimitiveFact<?>) o;
        if (isZero()) {
            return that.isZero();
        }
        if (that.isZero()) {
            return false;
        }
        if (!Objects.equals(mTag, that.mTag)) {
            return false;
        }
        return carriersEqual(mCarrier, that.mCarrier);
    }

    @Override
    public int hashCode() {
        if (isZero()) {
            return 0;
        }
        return Objects.hash(mTag, carrierHash(mCarrier));
    }

    private static int carrierHash(final Value v) {
        if (v instanceof IntConstant) {
            return Integer.hashCode(((IntConstant) v).getValue());
        }
        if (v instanceof LongConstant) {
            return Long.hashCode(((LongConstant) v).getValue());
        }
        if (v instanceof FloatConstant) {
            return Float.floatToIntBits(((FloatConstant) v).getValue());
        }
        if (v instanceof DoubleConstant) {
            return Long.hashCode(Double.doubleToLongBits(((DoubleConstant) v).getValue()));
        }
        if (v instanceof JStaticFieldRef) {
            final JStaticFieldRef r = (JStaticFieldRef) v;
            return Objects.hash(r.getFieldSignature().getName(),
                    r.getFieldSignature().getDeclClassType().getFullyQualifiedName());
        }
        return System.identityHashCode(v);
    }

    public static boolean carriersEqual(final Value a, final Value b) {
        if (a == b) {
            return true;
        }
        if (a instanceof IntConstant && b instanceof IntConstant) {
            return ((IntConstant) a).getValue() == ((IntConstant) b).getValue();
        }
        if (a instanceof LongConstant && b instanceof LongConstant) {
            return ((LongConstant) a).getValue() == ((LongConstant) b).getValue();
        }
        if (a instanceof FloatConstant && b instanceof FloatConstant) {
            return Float.compare(((FloatConstant) a).getValue(), ((FloatConstant) b).getValue()) == 0;
        }
        if (a instanceof DoubleConstant && b instanceof DoubleConstant) {
            return Double.compare(((DoubleConstant) a).getValue(), ((DoubleConstant) b).getValue()) == 0;
        }
        if (a instanceof JStaticFieldRef && b instanceof JStaticFieldRef) {
            final FieldSignature fa = ((JStaticFieldRef) a).getFieldSignature();
            final FieldSignature fb = ((JStaticFieldRef) b).getFieldSignature();
            return fa.equals(fb);
        }
        return a.equivTo(b);
    }
}
