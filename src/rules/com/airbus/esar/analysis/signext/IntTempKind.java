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

/** Classifies an {@code int} temp for {@code (byte)} cast safety when sign-ext bits are clear. */
public enum IntTempKind {

    BOTTOM,
    NARROW,
    RE_WIDEN_UNSAFE,
    MOD_UNSAFE;

    public static IntTempKind join(final IntTempKind existing, final IntTempKind proposed) {
        if (existing == null || existing == BOTTOM) {
            return proposed == null ? BOTTOM : proposed;
        }
        if (proposed == null || proposed == BOTTOM) {
            return existing;
        }
        return existing.ordinal() > proposed.ordinal() ? existing : proposed;
    }
}
