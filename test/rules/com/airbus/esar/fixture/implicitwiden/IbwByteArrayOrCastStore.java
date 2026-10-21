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
 * {@code buffer[0] = (byte) (buffer[1] | buffer[2]);} — byte array OR with explicit cast to byte.
 */
public final class IbwByteArrayOrCastStore {

    private IbwByteArrayOrCastStore() {
    }

    public static void entry(final byte[] buffer) {
        buffer[0] = (byte) (buffer[1] | buffer[2]);
    }
}
