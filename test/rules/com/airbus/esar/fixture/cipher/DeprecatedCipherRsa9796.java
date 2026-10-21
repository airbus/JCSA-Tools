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

package com.airbus.esar.fixture.cipher;

import javacardx.crypto.Cipher;

public final class DeprecatedCipherRsa9796 {

    private DeprecatedCipherRsa9796() {
    }

    public static void entryRsa9796() {
        Cipher.getInstance(Cipher.ALG_RSA_ISO9796, false);
    }
}
