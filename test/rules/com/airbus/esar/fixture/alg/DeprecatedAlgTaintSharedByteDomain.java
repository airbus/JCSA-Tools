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

/**
 * Same byte value, different JC API domain: shared IFDS may tag a literal with one rule's label,
 * but only the matching sink rule must report.
 */
public final class DeprecatedAlgTaintSharedByteDomain {

    private DeprecatedAlgTaintSharedByteDomain() {
    }

    /** Byte 1: {@code RandomData.ALG_PSEUDO_RANDOM} vs {@code Cipher.ALG_DES_CBC_NOPAD}. */
    public static void entryCipherGetInstanceDesCbcByteOne() {
        Cipher.getInstance(Cipher.ALG_DES_CBC_NOPAD, false);
    }

    /** Byte 35: {@code Signature.ALG_AES_MAC_192_NOPAD} literal at Cipher sink. */
    public static void entryCipherGetInstanceLiteralThirtyFive() {
        Cipher.getInstance((byte) 35, false);
    }

    /** Byte 1 at RandomData sink: deprecated pseudo-random, not a Cipher algorithm. */
    public static void entryRandomDataGetInstancePseudoByteOne() {
        RandomData.getInstance(RandomData.ALG_PSEUDO_RANDOM);
    }

    /** Byte 35 at Signature sink: deprecated MAC, not a Cipher algorithm. */
    public static void entrySignatureGetInstanceMac192ByteThirtyFive() {
        Signature.getInstance(Signature.ALG_AES_MAC_192_NOPAD, false);
    }

    public static void entryMixedDeprecatedCipherWithOkRandomAndSignature() {
        Cipher.getInstance(Cipher.ALG_AES_BLOCK_192_ECB_NOPAD, false);
        RandomData.getInstance(RandomData.ALG_TRNG);
        Signature.getInstance(Signature.ALG_RSA_SHA_PKCS1, false);
    }
}
