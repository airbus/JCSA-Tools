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

import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

/** Local roles for tracking temps vs consumption sinks. */
final class SignExtensionLocals {

    private SignExtensionLocals() {
    }

    static boolean isAssignToTrackingTemp(final LValue lhs, final Value rhs) {
        if (!(lhs instanceof Local local)) {
            return false;
        }
        final Type t = local.getType();
        return t.equals(PrimitiveType.getInt())
                || t.equals(PrimitiveType.getShort())
                || t.equals(PrimitiveType.getChar())
                || t.equals(PrimitiveType.getLong())
                || t.equals(PrimitiveType.getFloat())
                || t.equals(PrimitiveType.getDouble());
    }

    static boolean isStagingAtConsumer(final Local local) {
        final Type type = local.getType();
        return type.equals(PrimitiveType.getInt())
                || type.equals(PrimitiveType.getLong())
                || type.equals(PrimitiveType.getFloat())
                || type.equals(PrimitiveType.getDouble());
    }

    static boolean isConsumptionStore(final LValue lhs, final Value rhs) {
        if (lhs instanceof JFieldRef || lhs instanceof JArrayRef) {
            return true;
        }
        if (lhs instanceof final Local local) {
            final Type t = local.getType();
            if (t.equals(PrimitiveType.getShort()) || t.equals(PrimitiveType.getChar())) {
                return SignExtensionEval.isShortCharWidenRhs(local, rhs);
            }
        }
        return false;
    }
}
