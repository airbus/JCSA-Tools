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

package com.airbus.esar.fixture.comptime;

import javacard.security.RandomData;

/**
 * Stress: {@code JcCompileTimeUncertainty} treats any {@code JStaticFieldRef} as “compile-time known”. The field
 * identity is fixed in the constant pool, but the <em>value</em> is assigned from a parameter at runtime — so a
 * certification-style “value is fixed in the CAP” reading is wrong. The detector is expected to stay <em>silent</em>
 * here (false negative vs. that stricter reading).
 */
public final class ComptimeMutableStaticField {

    private static byte sRuntimeAlgorithmSlot;

    private ComptimeMutableStaticField() {
    }

    public static void entryMutableStatic(final byte fromApdu) {
        sRuntimeAlgorithmSlot = fromApdu;
        RandomData.getInstance(sRuntimeAlgorithmSlot);
    }
}
