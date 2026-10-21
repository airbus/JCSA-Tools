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

import javacardx.crypto.Cipher;

public final class BadOneShotCloseBeforeTry {

    private static volatile Object sLeak;

    private BadOneShotCloseBeforeTry() {
    }

    public static void entry() {
        Cipher.OneShot cipher = null;
        if (cipher != null) {
            cipher.close();
        }
        try {
            cipher = Cipher.OneShot.open(Cipher.CIPHER_RSA, Cipher.PAD_PKCS1);
        } finally {
            sLeak = cipher;
        }
    }
}
