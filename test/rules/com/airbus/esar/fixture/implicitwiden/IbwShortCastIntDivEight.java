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

package com.airbus.esar.fixture.implicitwiden;

/** {@code int test = someValue(); short test1 = (short) (test / 8);} */
public final class IbwShortCastIntDivEight {
    private static volatile short sSink;
    private static int sValue;

    private IbwShortCastIntDivEight() {
    }

    private static int someValue() {
        return sValue;
    }

    public static void entry(final int b) {
        sValue = b;
        final int test = someValue();
        final short test1 = (short) (test / 8);
        sSink = test1;
    }
}
