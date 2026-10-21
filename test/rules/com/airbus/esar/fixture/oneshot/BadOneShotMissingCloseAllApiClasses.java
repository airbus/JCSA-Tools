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

public final class BadOneShotMissingCloseAllApiClasses {

    private static volatile Object sLeak;

    private BadOneShotMissingCloseAllApiClasses() {
    }

    public static void badRandomDataMissingClose() {
        RandomData.OneShot random = null;
        try {
            random = RandomData.OneShot.open(RandomData.ALG_TRNG);
        } finally {
            sLeak = random;
        }
    }

    public static void badMessageDigestMissingClose() {
        MessageDigest.OneShot digest = null;
        try {
            digest = MessageDigest.OneShot.open(MessageDigest.ALG_SHA);
        } finally {
            sLeak = digest;
        }
    }

    public static void badInitializedMessageDigestMissingClose() {
        InitializedMessageDigest.OneShot digest = null;
        try {
            digest = InitializedMessageDigest.OneShot.open(MessageDigest.ALG_SHA);
        } finally {
            sLeak = digest;
        }
    }

    public static void badSignatureMissingClose() {
        Signature.OneShot sig = null;
        try {
            sig = Signature.OneShot.open(MessageDigest.ALG_SHA, Signature.SIG_CIPHER_RSA, Cipher.PAD_PKCS1);
        } finally {
            sLeak = sig;
        }
    }

    public static void badCipherMissingClose() {
        Cipher.OneShot cipher = null;
        try {
            cipher = Cipher.OneShot.open(AEADCipher.CIPHER_AES_GCM, Cipher.PAD_NULL);
        } finally {
            sLeak = cipher;
        }
    }

    public static void badDerivationFunctionMissingClose() {
        DerivationFunction.OneShot kdf = null;
        try {
            kdf = DerivationFunction.OneShot.open((byte) DerivationFunction.ALG_KDF_COUNTER_MODE);
        } finally {
            sLeak = kdf;
        }
    }
}
