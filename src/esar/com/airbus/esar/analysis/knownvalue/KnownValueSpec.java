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

package com.airbus.esar.analysis.knownvalue;

import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.types.PrimitiveType;
import sootup.java.core.JavaSootField;

/**
 * Leaf acceptance rules for {@link CompileTimeKnownValue}.
 */
public enum KnownValueSpec {

    /** {@link IntConstant} and any {@code static final} field load. */
    INT {
        @Override
        boolean acceptsConstant(final Value value) {
            return value instanceof IntConstant;
        }

        @Override
        boolean acceptsStaticFinalField(final JavaSootField field) {
            return field.isStatic() && field.isFinal() && field.getType() instanceof PrimitiveType;
        }
    },

    /** Integral constants and {@code static final} primitive field loads. */
    INTEGRAL {
        @Override
        boolean acceptsConstant(final Value value) {
            return value instanceof IntConstant || value instanceof LongConstant;
        }

        @Override
        boolean acceptsStaticFinalField(final JavaSootField field) {
            return field.isStatic() && field.isFinal() && field.getType() instanceof PrimitiveType;
        }
    };

    abstract boolean acceptsConstant(Value value);

    abstract boolean acceptsStaticFinalField(JavaSootField field);
}
