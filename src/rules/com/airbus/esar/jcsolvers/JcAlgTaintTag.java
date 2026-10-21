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

package com.airbus.esar.jcsolvers;

/**
 * Labels for primitive-taint IFDS. Each enum maps to one field or literal in JcAlgTaintSpecs and its JC byte value.
 */
public enum JcAlgTaintTag {
    // Random data
    ALG_PSEUDO_RANDOM(1),
    ALG_SECURE_RANDOM(2),

    // Cipher
    ALG_AES_BLOCK_192_CBC_NOPAD(18),
    ALG_AES_BLOCK_192_ECB_NOPAD(19),
    ALG_AES_BLOCK_256_CBC_NOPAD(20),
    ALG_AES_BLOCK_256_ECB_NOPAD(21),
    ALG_RSA_ISO14888(9),
    ALG_RSA_ISO9796(11),

    // Signature
    ALG_AES_MAC_192_NOPAD(35),
    ALG_AES_MAC_256_NOPAD(36);

    private final int mJcLiteralByte;

    JcAlgTaintTag(final int jcLiteralByte) {
        mJcLiteralByte = jcLiteralByte;
    }

    /**
     * @return JC byte literal for this tag
     */
    public int getLiteral() {
        return mJcLiteralByte;
    }

    /**
     * @return true for {@code javacard.security.RandomData} algorithm bytes (including numeric 1/2 mapping).
     */
    public boolean isRandomDataAlgorithm() {
        return this == ALG_PSEUDO_RANDOM || this == ALG_SECURE_RANDOM;
    }

    /**
     * @return true for deprecated Cipher algorithm constants in specs.
     */
    public boolean isDeprecatedCipherAlgorithm() {
        return switch (this) {
            case ALG_AES_BLOCK_192_CBC_NOPAD, ALG_AES_BLOCK_192_ECB_NOPAD,
                 ALG_AES_BLOCK_256_CBC_NOPAD, ALG_AES_BLOCK_256_ECB_NOPAD,
                 ALG_RSA_ISO14888, ALG_RSA_ISO9796 -> true;
            default -> false;
        };
    }

    /**
     * @return true for deprecated Signature algorithm constants in specs.
     */
    public boolean isDeprecatedSignatureAlgorithm() {
        return this == ALG_AES_MAC_192_NOPAD || this == ALG_AES_MAC_256_NOPAD;
    }
}
