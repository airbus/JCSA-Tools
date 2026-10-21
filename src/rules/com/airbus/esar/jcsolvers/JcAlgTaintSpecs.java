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

import com.airbus.esar.analysis.primitivetaint.PrimitiveSourceTrackingSpec;
import com.airbus.esar.analysis.primitivetaint.PrimitiveSourceTrackingSpec.Field;
import com.airbus.esar.analysis.primitivetaint.PrimitiveSourceTrackingSpec.IntLiteral;

/**
 * Declarative primitive sources for the shared Java Card algorithm-taint IFDS (see JcSolverFactory).
 */
public final class JcAlgTaintSpecs {

    private static final String RANDOM_DATA = "javacard.security.RandomData";
    private static final String CIPHER = "javacardx.crypto.Cipher";
    private static final String SIGNATURE = "javacard.security.Signature";

    private JcAlgTaintSpecs() {
    }

    /**
     * @return merged spec for rules sharing JcSharedSolvers.JC_DEPRECATED_ALG_TAINT
     */
    public static PrimitiveSourceTrackingSpec<JcAlgTaintTag> programUnion() {
        return PrimitiveSourceTrackingSpec.merge(
                PrimitiveSourceTrackingSpec.merge(randomDataAlgorithms(), deprecatedCipherAlgorithms()),
                deprecatedSignatureAlgorithms());
    }

    public static PrimitiveSourceTrackingSpec<JcAlgTaintTag> randomDataAlgorithms() {
        final JcAlgTaintTag pseudo = JcAlgTaintTag.ALG_PSEUDO_RANDOM;
        final JcAlgTaintTag secure = JcAlgTaintTag.ALG_SECURE_RANDOM;
        return PrimitiveSourceTrackingSpec.of(
                Field.of(RANDOM_DATA, pseudo.name(), pseudo),
                Field.of(RANDOM_DATA, secure.name(), secure),
                IntLiteral.of(pseudo.getLiteral(), pseudo),
                IntLiteral.of(secure.getLiteral(), secure));
    }

    public static PrimitiveSourceTrackingSpec<JcAlgTaintTag> deprecatedCipherAlgorithms() {
        final JcAlgTaintTag aes192cbc = JcAlgTaintTag.ALG_AES_BLOCK_192_CBC_NOPAD;
        final JcAlgTaintTag aes192ecb = JcAlgTaintTag.ALG_AES_BLOCK_192_ECB_NOPAD;
        final JcAlgTaintTag aes256cbc = JcAlgTaintTag.ALG_AES_BLOCK_256_CBC_NOPAD;
        final JcAlgTaintTag aes256ecb = JcAlgTaintTag.ALG_AES_BLOCK_256_ECB_NOPAD;
        final JcAlgTaintTag rsa14888 = JcAlgTaintTag.ALG_RSA_ISO14888;
        final JcAlgTaintTag rsa9796 = JcAlgTaintTag.ALG_RSA_ISO9796;
        return PrimitiveSourceTrackingSpec.of(
                Field.of(CIPHER, aes192cbc.name(), aes192cbc),
                Field.of(CIPHER, aes192ecb.name(), aes192ecb),
                Field.of(CIPHER, aes256cbc.name(), aes256cbc),
                Field.of(CIPHER, aes256ecb.name(), aes256ecb),
                Field.of(CIPHER, rsa14888.name(), rsa14888),
                Field.of(CIPHER, rsa9796.name(), rsa9796),
                IntLiteral.of(aes192cbc.getLiteral(), aes192cbc),
                IntLiteral.of(aes192ecb.getLiteral(), aes192ecb),
                IntLiteral.of(aes256cbc.getLiteral(), aes256cbc),
                IntLiteral.of(aes256ecb.getLiteral(), aes256ecb),
                IntLiteral.of(rsa14888.getLiteral(), rsa14888),
                IntLiteral.of(rsa9796.getLiteral(), rsa9796));
    }

    public static PrimitiveSourceTrackingSpec<JcAlgTaintTag> deprecatedSignatureAlgorithms() {
        final JcAlgTaintTag mac192 = JcAlgTaintTag.ALG_AES_MAC_192_NOPAD;
        final JcAlgTaintTag mac256 = JcAlgTaintTag.ALG_AES_MAC_256_NOPAD;
        return PrimitiveSourceTrackingSpec.of(
                Field.of(SIGNATURE, mac192.name(), mac192),
                Field.of(SIGNATURE, mac256.name(), mac256),
                IntLiteral.of(mac192.getLiteral(), mac192),
                IntLiteral.of(mac256.getLiteral(), mac256));
    }
}
