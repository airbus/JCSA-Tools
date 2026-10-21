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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.PrimitiveType;
import sootup.java.core.JavaSootMethod;
import com.airbus.esar.analysis.knownvalue.CompileTimeKnownInt;
import com.airbus.esar.soot.JimpleValues;
import com.airbus.esar.common.JcConstants;
import com.airbus.esar.soot.InvokeMatchers;
import com.airbus.esar.soot.StaticInvokeSignature;

/**
 * Flags monitored arguments that are not compile-time known at the call.
 */
public final class JcCompileTimeUncertainty implements ProgramVisitorIf {
    public static final String RULE_ID = "JcCompileTimeUncertainty";
    private static final SarifResultLevel DEFAULT_SEVERITY = SarifResultLevel.WARNING;
    public static final String MESSAGE_RANDOM_DATA = "javacard.compileTimeUncertainty.randomData";
    public static final String MESSAGE_CIPHER = "javacard.compileTimeUncertainty.cipher";
    public static final String MESSAGE_MESSAGE_DIGEST = "javacard.compileTimeUncertainty.messageDigest";
    public static final String MESSAGE_INITIALIZED_MESSAGE_DIGEST =
            "javacard.compileTimeUncertainty.initializedMessageDigest";
    public static final String MESSAGE_SIGNATURE = "javacard.compileTimeUncertainty.signature";
    public static final String MESSAGE_DERIVATION_FUNCTION = "javacard.compileTimeUncertainty.derivationFunction";
    public static final String MESSAGE_KEY_BUILDER = "javacard.compileTimeUncertainty.keyBuilder";
    public static final String MESSAGE_KEY_PAIR = "javacard.compileTimeUncertainty.keyPair";
    public static final String MESSAGE_CHECKSUM = "javacard.compileTimeUncertainty.checksum";
    public static final String MESSAGE_KEY_AGREEMENT = "javacard.compileTimeUncertainty.keyAgreement";
    public static final String MESSAGE_CERTIFICATE_PARSER = "javacard.compileTimeUncertainty.certificateParser";
    public static final String MESSAGE_NAMED_PARAMETER_SPEC = "javacard.compileTimeUncertainty.namedParameterSpec";

    private record MonitoredCall(String messageId, int byteArgIndex, StaticInvokeSignature... invokeShapes) {

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof final MonitoredCall other)) {
                return false;
            }
            return Objects.equals(messageId, other.messageId)
                    && byteArgIndex == other.byteArgIndex
                    && Arrays.equals(invokeShapes, other.invokeShapes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(messageId, byteArgIndex, Arrays.hashCode(invokeShapes));
        }

        @Override
        public String toString() {
            return "MonitoredCall[messageId=" + messageId
                    + ", byteArgIndex=" + byteArgIndex
                    + ", invokeShapes=" + Arrays.toString(invokeShapes) + "]";
        }
    }

    private static final List<MonitoredCall> MONITORED_CALLS = List.of(
            new MonitoredCall(
                    MESSAGE_RANDOM_DATA,
                    0,
                    StaticInvokeSignature.of(
                            JcConstants.RANDOM_DATA_CLASS,
                            "getInstance",
                            JcConstants.RANDOM_DATA_CLASS,
                            PrimitiveType.getByte()),
                    StaticInvokeSignature.of(
                            JcConstants.RANDOM_DATA_ONESHOT_CLASS,
                            "open",
                            JcConstants.RANDOM_DATA_ONESHOT_CLASS,
                            PrimitiveType.getByte())),
            new MonitoredCall(
                    MESSAGE_CIPHER,
                    0,
                    StaticInvokeSignature.of(
                            JcConstants.CIPHER_CLASS,
                            "getInstance",
                            JcConstants.CIPHER_CLASS,
                            PrimitiveType.getByte(),
                            PrimitiveType.getBoolean()),
                    StaticInvokeSignature.of(
                            JcConstants.CIPHER_CLASS,
                            "getInstance",
                            JcConstants.CIPHER_CLASS,
                            PrimitiveType.getByte(),
                            PrimitiveType.getByte(),
                            PrimitiveType.getBoolean()),
                    StaticInvokeSignature.of(
                            JcConstants.CIPHER_ONESHOT_CLASS,
                            "open",
                            JcConstants.CIPHER_ONESHOT_CLASS,
                            PrimitiveType.getByte(),
                            PrimitiveType.getByte())),
            new MonitoredCall(
                    MESSAGE_MESSAGE_DIGEST,
                    0,
                    StaticInvokeSignature.of(
                            JcConstants.MESSAGE_DIGEST_CLASS,
                            "getInstance",
                            JcConstants.MESSAGE_DIGEST_CLASS,
                            PrimitiveType.getByte(),
                            PrimitiveType.getBoolean()),
                    StaticInvokeSignature.of(
                            JcConstants.MESSAGE_DIGEST_ONESHOT_CLASS,
                            "open",
                            JcConstants.MESSAGE_DIGEST_ONESHOT_CLASS,
                            PrimitiveType.getByte())),
            new MonitoredCall(
                    MESSAGE_INITIALIZED_MESSAGE_DIGEST,
                    0,
                    StaticInvokeSignature.of(
                            JcConstants.MESSAGE_DIGEST_CLASS,
                            "getInitializedMessageDigestInstance",
                            JcConstants.INITIALIZED_MESSAGE_DIGEST_CLASS,
                            PrimitiveType.getByte(),
                            PrimitiveType.getBoolean()),
                    StaticInvokeSignature.of(
                            JcConstants.INITIALIZED_MESSAGE_DIGEST_ONESHOT_CLASS,
                            "open",
                            JcConstants.INITIALIZED_MESSAGE_DIGEST_ONESHOT_CLASS,
                            PrimitiveType.getByte())),
            new MonitoredCall(
                    MESSAGE_SIGNATURE,
                    0,
                    StaticInvokeSignature.of(
                            JcConstants.SIGNATURE_CLASS,
                            "getInstance",
                            JcConstants.SIGNATURE_CLASS,
                            PrimitiveType.getByte(),
                            PrimitiveType.getBoolean()),
                    StaticInvokeSignature.of(
                            JcConstants.SIGNATURE_CLASS,
                            "getInstance",
                            JcConstants.SIGNATURE_CLASS,
                            PrimitiveType.getByte(),
                            PrimitiveType.getByte(),
                            PrimitiveType.getByte(),
                            PrimitiveType.getBoolean()),
                    StaticInvokeSignature.of(
                            JcConstants.SIGNATURE_ONESHOT_CLASS,
                            "open",
                            JcConstants.SIGNATURE_ONESHOT_CLASS,
                            PrimitiveType.getByte(),
                            PrimitiveType.getByte(),
                            PrimitiveType.getByte())),
            new MonitoredCall(
                    MESSAGE_DERIVATION_FUNCTION,
                    0,
                    StaticInvokeSignature.of(
                            JcConstants.DERIVATION_FUNCTION_CLASS,
                            "getInstance",
                            JcConstants.DERIVATION_FUNCTION_CLASS,
                            PrimitiveType.getShort(),
                            PrimitiveType.getBoolean()),
                    StaticInvokeSignature.of(
                            JcConstants.DERIVATION_FUNCTION_ONESHOT_CLASS,
                            "open",
                            JcConstants.DERIVATION_FUNCTION_ONESHOT_CLASS,
                            PrimitiveType.getByte())),
            new MonitoredCall(
                    MESSAGE_KEY_BUILDER,
                    0,
                    StaticInvokeSignature.ignoringReturn(
                            JcConstants.KEY_BUILDER_CLASS,
                            "buildKey",
                            PrimitiveType.getByte(),
                            PrimitiveType.getShort(),
                            PrimitiveType.getBoolean()),
                    StaticInvokeSignature.ignoringReturn(
                            JcConstants.KEY_BUILDER_CLASS,
                            "buildKey",
                            PrimitiveType.getByte(),
                            PrimitiveType.getByte(),
                            PrimitiveType.getShort(),
                            PrimitiveType.getBoolean()),
                    StaticInvokeSignature.ignoringReturn(
                            JcConstants.KEY_BUILDER_CLASS,
                            "buildKeyWithSharedDomain",
                            PrimitiveType.getByte(),
                            PrimitiveType.getByte(),
                            InvokeMatchers.classType(JcConstants.KEY_CLASS),
                            PrimitiveType.getBoolean())),
            new MonitoredCall(
                    MESSAGE_KEY_PAIR,
                    0,
                    StaticInvokeSignature.ignoringReturn(
                            JcConstants.KEY_PAIR_CLASS,
                            "<init>",
                            PrimitiveType.getByte(),
                            PrimitiveType.getShort())),
            new MonitoredCall(
                    MESSAGE_CHECKSUM,
                    0,
                    StaticInvokeSignature.of(
                            JcConstants.CHECKSUM_CLASS,
                            "getInstance",
                            JcConstants.CHECKSUM_CLASS,
                            PrimitiveType.getByte(),
                            PrimitiveType.getBoolean())),
            new MonitoredCall(
                    MESSAGE_KEY_AGREEMENT,
                    0,
                    StaticInvokeSignature.of(
                            JcConstants.KEY_AGREEMENT_CLASS,
                            "getInstance",
                            JcConstants.KEY_AGREEMENT_CLASS,
                            PrimitiveType.getByte(),
                            PrimitiveType.getBoolean())),
            new MonitoredCall(
                    MESSAGE_CERTIFICATE_PARSER,
                    0,
                    StaticInvokeSignature.of(
                            JcConstants.CERTIFICATE_PARSER_CLASS,
                            "getInstance",
                            JcConstants.CERTIFICATE_PARSER_CLASS,
                            PrimitiveType.getShort())),
            new MonitoredCall(
                    MESSAGE_NAMED_PARAMETER_SPEC,
                    0,
                    StaticInvokeSignature.of(
                            JcConstants.NAMED_PARAMETER_SPEC_CLASS,
                            "getInstance",
                            JcConstants.NAMED_PARAMETER_SPEC_CLASS,
                            PrimitiveType.getShort())));


    @Override
    public String ruleId() {
        return RULE_ID;
    }

    @Override
    public SarifResultLevel severity() {
        return DEFAULT_SEVERITY;
    }

    @Override
    public void visitMethod(final JavaSootMethod method,
                            final Body body,
                            final AnalysisContextIf context,
                            final BugReporterIf bugs) {
        if (!context.isMethodInEntryPointReachabilitySlice(method.getSignature())) {
            return;
        }

        for (final Stmt stmt : body.getStmtGraph()) {
            for (final MonitoredCall spec : MONITORED_CALLS) {
                final AbstractInvokeExpr expr = InvokeMatchers.getMatchingInvoke(stmt, spec.invokeShapes());
                if (expr == null) {
                    continue;
                }
                final Value u = JimpleValues.unwrapCastChain(expr.getArg(spec.byteArgIndex()));
                if (CompileTimeKnownInt.isCompileTimeKnown(u, stmt, body, context)) {
                    continue;
                }

                bugs.report(RULE_ID, DEFAULT_SEVERITY, RuleMessageRef.of(spec.messageId()), method, stmt);
                break;
            }
        }
    }
}
