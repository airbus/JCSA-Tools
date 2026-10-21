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

package com.airbus.esar.fixture.oneshot;

import javacard.security.CryptoException;
import javacardx.crypto.Cipher;

public final class OkOneShotCipherWithCatch {

    private static volatile Object sLeak;

    private OkOneShotCipherWithCatch() {
    }

    public static void entry() {
        Cipher.OneShot enc = null;
        try {
            enc = Cipher.OneShot.open(Cipher.CIPHER_RSA, Cipher.PAD_PKCS1);
        } catch (CryptoException e) {
            sLeak = e;
        } finally {
            if (enc != null) {
                enc.close();
                enc = null;
            }
        }
    }
}
