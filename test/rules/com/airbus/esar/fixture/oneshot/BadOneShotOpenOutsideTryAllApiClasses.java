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

import javacard.security.InitializedMessageDigest;
import javacard.security.MessageDigest;
import javacard.security.RandomData;
import javacard.security.Signature;
import javacardx.crypto.AEADCipher;
import javacardx.crypto.Cipher;
import javacardx.security.derivation.DerivationFunction;

public final class BadOneShotOpenOutsideTryAllApiClasses {

    private static volatile short sNoop;

    private BadOneShotOpenOutsideTryAllApiClasses() {
    }

    public static void badRandomDataOpenOutsideTry() {
        RandomData.OneShot random = null;
        random = RandomData.OneShot.open(RandomData.ALG_TRNG);
        try {
            sNoop = 0;
        } finally {
            if (random != null) {
                random.close();
            }
        }
    }

    public static void badMessageDigestOpenOutsideTry() {
        MessageDigest.OneShot digest = null;
        digest = MessageDigest.OneShot.open(MessageDigest.ALG_SHA);
        try {
            sNoop = 0;
        } finally {
            if (digest != null) {
                digest.close();
            }
        }
    }

    public static void badInitializedMessageDigestOpenOutsideTry() {
        InitializedMessageDigest.OneShot digest = null;
        digest = InitializedMessageDigest.OneShot.open(MessageDigest.ALG_SHA);
        try {
            sNoop = 0;
        } finally {
            if (digest != null) {
                digest.close();
            }
        }
    }

    public static void badSignatureOpenOutsideTry() {
        Signature.OneShot sig = null;
        sig = Signature.OneShot.open(MessageDigest.ALG_SHA, Signature.SIG_CIPHER_RSA, Cipher.PAD_PKCS1);
        try {
            sNoop = 0;
        } finally {
            if (sig != null) {
                sig.close();
            }
        }
    }

    public static void badCipherOpenOutsideTry() {
        Cipher.OneShot cipher = null;
        cipher = Cipher.OneShot.open(AEADCipher.CIPHER_AES_GCM, Cipher.PAD_NULL);
        try {
            sNoop = 0;
        } finally {
            if (cipher != null) {
                cipher.close();
            }
        }
    }

    public static void badDerivationFunctionOpenOutsideTry() {
        DerivationFunction.OneShot kdf = null;
        kdf = DerivationFunction.OneShot.open((byte) DerivationFunction.ALG_KDF_COUNTER_MODE);
        try {
            sNoop = 0;
        } finally {
            if (kdf != null) {
                kdf.close();
            }
        }
    }
}
