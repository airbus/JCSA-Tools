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

/**
 * Like {@link IbwUshr1ThenMask0x7f} but the mask is on the {@code return}. Suppresses the shift line
 * via forward mask on return. Should be silent.
 */
public final class IbwUshr1ThenReturnMask0x7f {
    private IbwUshr1ThenReturnMask0x7f() {
    }

    public static int entry(final byte b) {
        final int t = b >>> 1;
        return t & 0x7f;
    }
}
