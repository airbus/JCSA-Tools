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

package com.airbus.esar.fixture.comptime;

import javacard.security.Checksum;
import javacard.security.InitializedMessageDigest;
import javacard.security.Key;
import javacard.security.KeyAgreement;
import javacard.security.KeyBuilder;
import javacard.security.KeyPair;
import javacard.security.MessageDigest;
import javacard.security.NamedParameterSpec;
import javacard.security.RandomData;
import javacard.security.Signature;
import javacardx.crypto.AEADCipher;
import javacardx.crypto.Cipher;
import javacardx.security.cert.CertificateParser;
import javacardx.security.derivation.DerivationFunction;

public final class OkComptimeCryptoAlgorithms {

    private static final Key EC_DOMAIN_KEY =
            KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PARAMETERS, KeyBuilder.LENGTH_EC_FP_256, false);

    private OkComptimeCryptoAlgorithms() {
    }

    public static void okRandomDataGetInstance() {
        RandomData.getInstance(RandomData.ALG_TRNG);
    }

    public static void okRandomDataOneShot() {
        RandomData.OneShot random = null;
        try {
            random = RandomData.OneShot.open(RandomData.ALG_TRNG);
        } finally {
            if (random != null) {
                random.close();
            }
        }
    }

    public static void okMessageDigestGetInstance() {
        MessageDigest.getInstance(MessageDigest.ALG_SHA, false);
    }

    public static void okMessageDigestOneShot() {
        MessageDigest.OneShot digest = null;
        try {
            digest = MessageDigest.OneShot.open(MessageDigest.ALG_SHA);
        } finally {
            if (digest != null) {
                digest.close();
            }
        }
    }

    public static void okInitializedMessageDigestGetInstance() {
        MessageDigest.getInitializedMessageDigestInstance(MessageDigest.ALG_SHA, false);
    }

    public static void okInitializedMessageDigestOneShot() {
        InitializedMessageDigest.OneShot digest = null;
        try {
            digest = InitializedMessageDigest.OneShot.open(MessageDigest.ALG_SHA);
        } finally {
            if (digest != null) {
                digest.close();
            }
        }
    }

    public static void okSignatureGetInstance() {
        Signature.getInstance(Signature.ALG_RSA_SHA_PKCS1, false);
    }

    public static void okSignatureThreeArgGetInstance() {
        Signature.getInstance(MessageDigest.ALG_SHA, Signature.SIG_CIPHER_RSA, Cipher.PAD_PKCS1, false);
    }

    public static void okSignatureOneShot() {
        Signature.OneShot sig = null;
        try {
            sig = Signature.OneShot.open(MessageDigest.ALG_SHA, Signature.SIG_CIPHER_RSA, Cipher.PAD_PKCS1);
        } finally {
            if (sig != null) {
                sig.close();
            }
        }
    }

    public static void okDerivationFunctionGetInstance() {
        DerivationFunction.getInstance(DerivationFunction.ALG_KDF_COUNTER_MODE, false);
    }

    public static void okDerivationFunctionOneShot() {
        DerivationFunction.OneShot kdf = null;
        try {
            kdf = DerivationFunction.OneShot.open((byte) DerivationFunction.ALG_KDF_COUNTER_MODE);
        } finally {
            if (kdf != null) {
                kdf.close();
            }
        }
    }

    public static void okKeyBuilderBuildKey() {
        KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
    }

    public static void okKeyBuilderTwoByteBuildKey() {
        KeyBuilder.buildKey(KeyBuilder.ALG_TYPE_AES, KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
    }

    public static void okKeyBuilderSharedDomain() {
        KeyBuilder.buildKeyWithSharedDomain(
                KeyBuilder.ALG_TYPE_AES, KeyBuilder.TYPE_AES, EC_DOMAIN_KEY, false);
    }

    public static void okKeyBuilderBuildXECKey() {
        KeyBuilder.buildXECKey(
                NamedParameterSpec.getInstance(NamedParameterSpec.X25519), KeyBuilder.ATTR_PUBLIC, false);
    }

    public static void okKeyPairConstructor() {
        new KeyPair(KeyPair.ALG_RSA, KeyBuilder.LENGTH_RSA_1024);
    }

    public static void okChecksumGetInstance() {
        Checksum.getInstance(Checksum.ALG_ISO3309_CRC16, false);
    }

    public static void okKeyAgreementGetInstance() {
        KeyAgreement.getInstance(KeyAgreement.ALG_EC_SVDP_DH, false);
    }

    public static void okCertificateParserGetInstance() {
        CertificateParser.getInstance(CertificateParser.TYPE_X509_DER);
    }

    public static void okNamedParameterSpecGetInstance() {
        NamedParameterSpec.getInstance(NamedParameterSpec.X25519);
    }

    public static void okAEADCipherGetInstance() {
        Cipher.getInstance(AEADCipher.ALG_AES_GCM, false);
    }

    public static void okAEADCipherThreeArgGetInstance() {
        Cipher.getInstance(AEADCipher.CIPHER_AES_GCM, Cipher.PAD_NULL, false);
    }

    public static void okAEADCipherOneShot() {
        Cipher.OneShot cipher = null;
        try {
            cipher = Cipher.OneShot.open(AEADCipher.CIPHER_AES_GCM, Cipher.PAD_NULL);
        } finally {
            if (cipher != null) {
                cipher.close();
            }
        }
    }
}
