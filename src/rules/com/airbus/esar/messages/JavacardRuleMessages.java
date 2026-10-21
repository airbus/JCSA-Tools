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

package com.airbus.esar.messages;

import java.util.ListResourceBundle;

/**
 * All Java Card rule message patterns for this JAR (one bundle per artifact). Keys must be globally unique across every
 * rule module you load in a single analysis run.
 *
 * ACSAP refers to the Master's Thesis "A practical approach to static code analysis for the Java Card platform"
 */
public final class JavacardRuleMessages extends ListResourceBundle {

    @Override
    protected Object[][] getContents() {
        return new Object[][] {
            {"javacard.randomData.deprecatedAlgorithm",
                "RandomData.getInstance/open(byte) uses deprecated algorithm {0} (ACSAP 11)"},
            {"javacard.cipher.deprecatedAlgorithm",
                "Cipher.getInstance uses deprecated algorithm {0} (ACSAP 11)"},
            {"javacard.signature.deprecatedAlgorithm",
                "Signature.getInstance(byte, …) uses deprecated algorithm {0} (ACSAP 11)"},
            {"javacard.compileTimeUncertainty.randomData",
                "RandomData.getInstance(byte) byte argument is undecidable (ACSAP 15)"},
            {"javacard.compileTimeUncertainty.cipher",
                "Cipher.getInstance(byte, …) byte argument is undecidable (ACSAP 15)"},
            {"javacard.compileTimeUncertainty.messageDigest",
                "MessageDigest.getInstance/open(byte) byte argument is undecidable (ACSAP 15)"},
            {"javacard.compileTimeUncertainty.initializedMessageDigest",
                "InitializedMessageDigest factory/open(byte) byte argument is undecidable (ACSAP 15)"},
            {"javacard.compileTimeUncertainty.signature",
                "Signature.getInstance/open(byte, …) byte argument is undecidable (ACSAP 15)"},
            {"javacard.compileTimeUncertainty.derivationFunction",
                "DerivationFunction.getInstance/open algorithm argument is undecidable (ACSAP 15)"},
            {"javacard.compileTimeUncertainty.keyBuilder",
                "KeyBuilder.buildKey(byte, …) byte argument is undecidable (ACSAP 15)"},
            {"javacard.compileTimeUncertainty.keyPair",
                "KeyPair(byte, …) byte argument is undecidable (ACSAP 15)"},
            {"javacard.compileTimeUncertainty.checksum",
                "Checksum.getInstance(byte) byte argument is undecidable (ACSAP 15)"},
            {"javacard.compileTimeUncertainty.keyAgreement",
                "KeyAgreement.getInstance(byte) byte argument is undecidable (ACSAP 15)"},
            {"javacard.compileTimeUncertainty.certificateParser",
                "CertificateParser.getInstance(short) algorithm argument is undecidable (ACSAP 15)"},
            {"javacard.compileTimeUncertainty.namedParameterSpec",
                "NamedParameterSpec.getInstance(short) parameter is undecidable (ACSAP 15)"},
            {"javacard.oneShot.invalidUsage",
                "OneShot.open must be inside try with close in finally (ACSAP 12)"},
            {"javacard.recursion.directSelfCall", "Recursion cycle detected; method calls itself directly (AFSCM 29)"},
            {"javacard.recursion.callGraphCycle",
                "Recursion cycle detected; method participates in a CHA call-graph cycle: {0} (AFSCM 29)"},
            {"javacard.register.missing",
                "register() missing from applet life-cycle (ACSAP 14)"},
            {"javacard.bitshift.signedRight",
                "Signed right-shift is not allowed (ACSAP 10)"},
            {"javacard.api.blacklist",
                "Forbidden API found: {0} (AFSCM 17; TCA 28)"},
            {"javacard.shareable.mustBeMultiselectable",
                "Applet providing shareable object must implement MultiSelectable (TCA 34)"},
            {"javacard.applet.missingUninstall",
                "Applet does not implement uninstall method (AFSCM 14)"},
            {"javacard.exception.custom",
                "Application-specific exception not allowed: {0} (AFSCM 47)"},
            {"javacard.process.allocation",
                "Object allocation in Applet.process() call graph: new {0} (TCA 1)"},
            {"javacard.exception.catch",
                "Catching java.lang.Exception is not allowed (AFSCM 46)"},
            {"javacard.exception.runtime.throw",
                "Throwing java.lang.RuntimeException is not allowed: {0} (AFSCM 45)"},
            {"javacard.transaction.unbalanced",
                "JCSystem.beginTransaction may not be matched by commit/abort on all paths "
                        + "(residual depth {0}) (ACSAP 4)"},
            {"javacard.apdu.buffer.outOfOrder",
                "APDU outgoing protocol violation: call to {1} while in state {0} (ACSAP 5)"},
            {"javacard.apdu.buffer.incomplete",
                "APDU outgoing protocol not finished; method exits while in state {0} (ACSAP 5)"},
            {"javacard.integral.implicitWiden.assignment",
                "Integral widened to a wider type without masking the sign extension "
                        + "(implicit sign extension) (ACSAP 2)"},
            {"javacard.recordlike.settergetter",
                "Class declares only JavaBean-style getters and/or setters; add other behaviour or API (ACSAP 8)"},
            {"javacard.hotfield.frequentAccess",
                "Field accessed {0,number} times in one method; cache in a local variable (ACSAP 6)"},
            {"javacard.hotfield.loopAccess",
                "Field accessed inside a loop; cache in a local variable before the loop (ACSAP 6)"},
            {"javacard.ownerPin.checkIgnored",
                "OwnerPIN.check return value ignored (ACSAP 13)"},
            {"javacard.array.unknownAllocSize",
                "Array allocated with non-constant size: new {0} (AFSCM 26)"},
            {"javacard.isoException.replyBusy",
                "ISOException.throwIt must not conclude event processing with REPLY_BUSY (AFSCM 49)"},
            {"javacard.isoException.invalidSw1",
                "ISOException.throwIt uses non-standard (ISO/IEC 7816-4) SW1 {0} (AFSCM 58)"},
            {"javacard.isoException.invalidSw2",
                "ISOException.throwIt uses non-standard (ISO/IEC 7816-4) SW1-SW2 {0} (AFSCM 58)"},
            {"javacard.isoException.unknownSw",
                "ISOException.throwIt status word is not a compile-time constant or "
                + "the power set lattice has overflown (AFSCM 58)"}
        };
    }
}
