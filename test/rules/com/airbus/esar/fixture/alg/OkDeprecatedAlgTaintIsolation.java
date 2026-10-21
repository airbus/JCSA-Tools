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

package com.airbus.esar.fixture.alg;

import javacard.security.RandomData;
import javacard.security.Signature;
import javacardx.crypto.Cipher;

/** Non-deprecated APIs for rules sharing {@code JC_DEPRECATED_ALG_TAINT}. */
public final class OkDeprecatedAlgTaintIsolation {

    private OkDeprecatedAlgTaintIsolation() {
    }

    public static void entryCipherGetInstanceDesEcb() {
        Cipher.getInstance(Cipher.ALG_DES_ECB_NOPAD, false);
    }

    public static void entryCipherGetInstanceThreeArg() {
        Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, Cipher.CIPHER_AES_ECB, false);
    }

    public static void entryCipherOneShotSm4Ecb() {
        Cipher.OneShot cipher = null;
        try {
            cipher = Cipher.OneShot.open(Cipher.CIPHER_SM4_ECB, Cipher.PAD_NULL);
        } finally {
            if (cipher != null) {
                cipher.close();
            }
        }
    }

    public static void entryRandomDataTrng() {
        RandomData.getInstance(RandomData.ALG_TRNG);
    }

    public static void entryRandomDataOneShotKeygen() {
        RandomData.OneShot random = null;
        try {
            random = RandomData.OneShot.open(RandomData.ALG_KEYGENERATION);
        } finally {
            if (random != null) {
                random.close();
            }
        }
    }

    public static void entrySignatureRsaShaPkcs1() {
        Signature.getInstance(Signature.ALG_RSA_SHA_PKCS1, false);
    }

    public static void entryAllOkCalls() {
        entryCipherGetInstanceDesEcb();
        entryCipherGetInstanceThreeArg();
        entryCipherOneShotSm4Ecb();
        entryRandomDataTrng();
        entryRandomDataOneShotKeygen();
        entrySignatureRsaShaPkcs1();
    }
}
