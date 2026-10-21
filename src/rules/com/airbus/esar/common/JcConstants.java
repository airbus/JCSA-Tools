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

package com.airbus.esar.common;

public final class JcConstants {
    private JcConstants() {
        // NOP
    }

    public static final String RANDOM_DATA_CLASS = "javacard.security.RandomData";
    public static final String RANDOM_DATA_ONESHOT_CLASS = "javacard.security.RandomData$OneShot";
    public static final String CIPHER_CLASS = "javacardx.crypto.Cipher";
    public static final String CIPHER_ONESHOT_CLASS = "javacardx.crypto.Cipher$OneShot";
    public static final String MESSAGE_DIGEST_CLASS = "javacard.security.MessageDigest";
    public static final String MESSAGE_DIGEST_ONESHOT_CLASS = "javacard.security.MessageDigest$OneShot";
    public static final String INITIALIZED_MESSAGE_DIGEST_CLASS = "javacard.security.InitializedMessageDigest";
    public static final String INITIALIZED_MESSAGE_DIGEST_ONESHOT_CLASS =
            "javacard.security.InitializedMessageDigest$OneShot";
    public static final String SIGNATURE_CLASS = "javacard.security.Signature";
    public static final String SIGNATURE_ONESHOT_CLASS = "javacard.security.Signature$OneShot";
    public static final String DERIVATION_FUNCTION_CLASS = "javacardx.security.derivation.DerivationFunction";
    public static final String DERIVATION_FUNCTION_ONESHOT_CLASS =
            "javacardx.security.derivation.DerivationFunction$OneShot";
    public static final String KEY_BUILDER_CLASS = "javacard.security.KeyBuilder";
    public static final String KEY_PAIR_CLASS = "javacard.security.KeyPair";
    public static final String CHECKSUM_CLASS = "javacard.security.Checksum";
    public static final String KEY_AGREEMENT_CLASS = "javacard.security.KeyAgreement";
    public static final String CERTIFICATE_PARSER_CLASS = "javacardx.security.cert.CertificateParser";
    public static final String NAMED_PARAMETER_SPEC_CLASS = "javacard.security.NamedParameterSpec";
    public static final String KEY_CLASS = "javacard.security.Key";
    public static final String JCSYSTEM_CLASS = "javacard.framework.JCSystem";
    public static final String BEGIN_TRANSACTION = "beginTransaction";
    public static final String COMMIT_TRANSACTION = "commitTransaction";
    public static final String ABORT_TRANSACTION = "abortTransaction";

    public static final String APDU_CLASS = "javacard.framework.APDU";
    public static final String APDU_SET_OUTGOING = "setOutgoing";
    public static final String APDU_SET_OUTGOING_NO_CHAINING = "setOutgoingNoChaining";
    public static final String APDU_SET_OUTGOING_LENGTH = "setOutgoingLength";
    public static final String APDU_SEND_BYTES = "sendBytes";
    public static final String APDU_SEND_BYTES_LONG = "sendBytesLong";
    public static final String APDU_SET_OUTGOING_AND_SEND = "setOutgoingAndSend";
    public static final String APDU_SET_INCOMING_AND_RECEIVE = "setIncomingAndReceive";
    public static final String APDU_RECEIVE_BYTES = "receiveBytes";
    public static final String APDU_GET_INCOMING_LENGTH = "getIncomingLength";
    public static final String APDU_GET_OFFSET_CDATA = "getOffsetCdata";
    public static final String APDU_WAIT_EXTENSION = "waitExtension";

    public static final String CARD_RUNTIME_EXCEPTION_CLASS =
            "javacard.framework.CardRuntimeException";
    public static final String ISO_EXCEPTION_CLASS = "javacard.framework.ISOException";
    public static final String ISO7816_CLASS = "javacard.framework.ISO7816";
    public static final String THROW_IT = "throwIt";
    public static final String REPLY_BUSY_FIELD = "REPLY_BUSY";
    /** UICC / ETSI TS 102 241 REPLY_BUSY status word. */
    public static final int REPLY_BUSY_SW = 0x9300;
}
