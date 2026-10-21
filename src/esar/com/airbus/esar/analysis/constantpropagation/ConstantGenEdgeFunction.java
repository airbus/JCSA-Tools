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

import heros.EdgeFunction;
import heros.edgefunc.AllTop;
import heros.edgefunc.EdgeIdentity;

/**
 * Edge function that maps every incoming lattice value to one generated constant.
 */
public final class ConstantGenEdgeFunction implements EdgeFunction<ConstantLatticeValue> {

    private final ConstantLatticeValue mValue;

    private ConstantGenEdgeFunction(final ConstantLatticeValue value) {
        mValue = value;
    }

    public static ConstantGenEdgeFunction of(final ConstantLatticeValue value) {
        return new ConstantGenEdgeFunction(value);
    }

    @Override
    public ConstantLatticeValue computeTarget(final ConstantLatticeValue source) {
        return mValue;
    }

    @Override
    public EdgeFunction<ConstantLatticeValue> composeWith(final EdgeFunction<ConstantLatticeValue> second) {
        if (second instanceof EdgeIdentity) {
            return this;
        }
        return second;
    }

    @Override
    public EdgeFunction<ConstantLatticeValue> meetWith(final EdgeFunction<ConstantLatticeValue> other) {
        if (other instanceof EdgeIdentity) {
            return this;
        }
        if (other instanceof ConstantGenEdgeFunction otherGen) {
            final ConstantLatticeValue met = mValue.meet(otherGen.mValue);
            if (met.isTop()) {
                return new AllTop<>(ConstantLatticeValue.top());
            }
            if (met.equals(mValue)) {
                return this;
            }
            return ConstantGenEdgeFunction.of(met);
        }
        return new AllTop<>(ConstantLatticeValue.top());
    }

    @Override
    public boolean equalTo(final EdgeFunction<ConstantLatticeValue> other) {
        return other instanceof ConstantGenEdgeFunction gen && mValue.equals(gen.mValue);
    }
}
