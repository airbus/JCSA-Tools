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

package com.airbus.esar.detect;

import java.util.List;
import java.util.Set;

import sootup.core.types.ArrayType;
import sootup.core.types.PrimitiveType;
import com.airbus.esar.analysis.callflow.InvokeSignaturePattern;
import com.airbus.esar.analysis.typestate.TypestateRule;
import com.airbus.esar.analysis.typestate.TypestateSpec;
import com.airbus.esar.analysis.typestate.TypestateTransition;
import com.airbus.esar.common.JcConstants;

/**
 * Checks the Java Card APDU buffer protocol on every reachable path.
 *
 * Incoming: setIncomingAndReceive (once) -> receiveBytes / getIncomingLength / getOffsetCdata*
 * Outgoing: setOutgoing() or setOutgoingNoChaining() -> setOutgoingLength(len) -> sendBytes[/sendBytesLong]*
 *           OR setOutgoingAndSend(off, len)
 * waitExtension() is forbidden after setOutgoingNoChaining().
 */
public final class JcApduBufferState extends TypestateRule<JcApduBufferState.ApduState> {
    public static final String RULE_ID = "JcApduBufferState";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.ERROR;
    public static final String MESSAGE_OUT_OF_ORDER = "javacard.apdu.buffer.outOfOrder";
    public static final String MESSAGE_INCOMPLETE = "javacard.apdu.buffer.incomplete";

    /**
     * DFA states of the APDU incoming and outgoing buffer protocol.
     */
    public enum ApduState {
        /** No incoming or outgoing call made yet */
        NONE,
        /** Called setIncomingAndReceive() */
        INCOMING,
        /** Called setOutgoing() */
        OUTGOING,
        /** Called setOutgoingNoChaining() */
        OUTGOING_NO_CHAIN,
        /** Called setOutgoingLength(short) */
        LENGTH_KNOWN,
        /** Response fully sent */
        DONE,
        /** Protocol violation */
        ERROR
    }

    private static final InvokeSignaturePattern SET_INCOMING_AND_RECEIVE =
        InvokeSignaturePattern.instanceCallIgnoringReturn(
            JcConstants.APDU_CLASS,
            JcConstants.APDU_SET_INCOMING_AND_RECEIVE
        );

    private static final InvokeSignaturePattern RECEIVE_BYTES = InvokeSignaturePattern.instanceCallIgnoringReturn(
        JcConstants.APDU_CLASS,
        JcConstants.APDU_RECEIVE_BYTES,
        PrimitiveType.getShort()
    );

    private static final InvokeSignaturePattern GET_INCOMING_LENGTH =
        InvokeSignaturePattern.instanceCallIgnoringReturn(
            JcConstants.APDU_CLASS,
            JcConstants.APDU_GET_INCOMING_LENGTH
        );

    private static final InvokeSignaturePattern GET_OFFSET_CDATA = InvokeSignaturePattern.instanceCallIgnoringReturn(
        JcConstants.APDU_CLASS,
        JcConstants.APDU_GET_OFFSET_CDATA
    );

    private static final InvokeSignaturePattern WAIT_EXTENSION = InvokeSignaturePattern.staticCallIgnoringReturn(
        JcConstants.APDU_CLASS,
        JcConstants.APDU_WAIT_EXTENSION
    );

    private static final InvokeSignaturePattern SET_OUTGOING = InvokeSignaturePattern.instanceCallIgnoringReturn(
        JcConstants.APDU_CLASS,
        JcConstants.APDU_SET_OUTGOING
    );

    private static final InvokeSignaturePattern SET_OUTGOING_NO_CHAINING =
        InvokeSignaturePattern.instanceCallIgnoringReturn(
            JcConstants.APDU_CLASS,
            JcConstants.APDU_SET_OUTGOING_NO_CHAINING
        );

    private static final InvokeSignaturePattern SET_OUTGOING_LENGTH = InvokeSignaturePattern.instanceCallIgnoringReturn(
        JcConstants.APDU_CLASS,
        JcConstants.APDU_SET_OUTGOING_LENGTH,
        PrimitiveType.getShort()
    );

    private static final InvokeSignaturePattern SEND_BYTES = InvokeSignaturePattern.instanceCallIgnoringReturn(
        JcConstants.APDU_CLASS,
        JcConstants.APDU_SEND_BYTES,
        PrimitiveType.getShort(),
        PrimitiveType.getShort()
    );

    private static final InvokeSignaturePattern SEND_BYTES_LONG = InvokeSignaturePattern.instanceCallIgnoringReturn(
        JcConstants.APDU_CLASS,
        JcConstants.APDU_SEND_BYTES_LONG,
        new ArrayType(PrimitiveType.getByte(), 1),
        PrimitiveType.getShort(),
        PrimitiveType.getShort()
    );

    private static final InvokeSignaturePattern SET_OUTGOING_AND_SEND =
        InvokeSignaturePattern.instanceCallIgnoringReturn(
            JcConstants.APDU_CLASS,
            JcConstants.APDU_SET_OUTGOING_AND_SEND,
            PrimitiveType.getShort(),
            PrimitiveType.getShort()
        );

    private static final List<TypestateTransition<ApduState>> TRANSITIONS = List.of(
        // Incoming from NONE
        TypestateTransition.of(ApduState.NONE, SET_INCOMING_AND_RECEIVE, ApduState.INCOMING),
        // Incoming while receiving
        TypestateTransition.of(ApduState.INCOMING, RECEIVE_BYTES, ApduState.INCOMING),
        TypestateTransition.of(ApduState.INCOMING, GET_INCOMING_LENGTH, ApduState.INCOMING),
        TypestateTransition.of(ApduState.INCOMING, GET_OFFSET_CDATA, ApduState.INCOMING),
        // waitExtension before no-chaining outgoing
        TypestateTransition.of(ApduState.NONE, WAIT_EXTENSION, ApduState.NONE),
        TypestateTransition.of(ApduState.INCOMING, WAIT_EXTENSION, ApduState.INCOMING),
        TypestateTransition.of(ApduState.OUTGOING, WAIT_EXTENSION, ApduState.OUTGOING),
        TypestateTransition.of(ApduState.LENGTH_KNOWN, WAIT_EXTENSION, ApduState.LENGTH_KNOWN),
        // Outgoing from NONE or after incoming
        TypestateTransition.of(ApduState.NONE, SET_OUTGOING, ApduState.OUTGOING),
        TypestateTransition.of(ApduState.NONE, SET_OUTGOING_NO_CHAINING, ApduState.OUTGOING_NO_CHAIN),
        TypestateTransition.of(ApduState.NONE, SET_OUTGOING_AND_SEND, ApduState.DONE),
        TypestateTransition.of(ApduState.INCOMING, SET_OUTGOING, ApduState.OUTGOING),
        TypestateTransition.of(ApduState.INCOMING, SET_OUTGOING_NO_CHAINING, ApduState.OUTGOING_NO_CHAIN),
        TypestateTransition.of(ApduState.INCOMING, SET_OUTGOING_AND_SEND, ApduState.DONE),
        // Outgoing length and send
        TypestateTransition.of(ApduState.OUTGOING, SET_OUTGOING_LENGTH, ApduState.LENGTH_KNOWN),
        TypestateTransition.of(ApduState.OUTGOING_NO_CHAIN, SET_OUTGOING_LENGTH, ApduState.LENGTH_KNOWN),
        TypestateTransition.of(ApduState.LENGTH_KNOWN, SEND_BYTES, ApduState.DONE),
        TypestateTransition.of(ApduState.LENGTH_KNOWN, SEND_BYTES_LONG, ApduState.DONE),
        TypestateTransition.of(ApduState.DONE, SEND_BYTES, ApduState.DONE),
        TypestateTransition.of(ApduState.DONE, SEND_BYTES_LONG, ApduState.DONE)
    );

    private static final TypestateSpec<ApduState> SPEC = TypestateSpec.ofStrict(
        RULE_ID,
        MESSAGE_OUT_OF_ORDER,
        MESSAGE_INCOMPLETE,
        ApduState.NONE,
        ApduState.ERROR,
        Set.of(ApduState.NONE, ApduState.INCOMING, ApduState.DONE),
        TRANSITIONS
    );

    /**
     * Default constructor used by ServiceLoader-based registration.
     */
    public JcApduBufferState() {
        super(SPEC);
    }

    protected SarifResultLevel getSarifLevel() {
        return DEFAULT_SEVERITY;
    }
}
