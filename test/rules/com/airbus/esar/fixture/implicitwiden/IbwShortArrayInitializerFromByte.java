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
 * {@code short[]} created with an array initializer whose element is a {@code byte},
 * implicitly widened to {@code short}. Should be reported like {@link IbwByteToShort}.
 */
public final class IbwShortArrayInitializerFromByte {
    private static volatile short sSink;

    private IbwShortArrayInitializerFromByte() {
    }

    public static void entry(final byte b) {
        final short[] arr = {b};
        sSink = arr[0];
    }
}
