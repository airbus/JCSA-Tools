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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.airbus.esar.fixture.cipher.DeprecatedCipherAlgorithms;
import com.airbus.esar.fixture.cipher.DeprecatedCipherDynamicAlg;
import com.airbus.esar.fixture.cipher.OkCipherAlgorithms;
import com.airbus.esar.fixture.comptime.ComptimeAEADCipherDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeAEADCipherOneShotDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeAEADCipherThreeArgDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeCertificateParserDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeChecksumDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeDerivationFunctionDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeDerivationFunctionOneShotDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeInitializedMessageDigestDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeInitializedMessageDigestOneShotDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeKeyAgreementDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeKeyBuilderDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeKeyBuilderSharedDomainDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeKeyBuilderTwoByteDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeKeyPairDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeMessageDigestDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeMessageDigestOneShotDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeNamedParameterSpecDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeRandomDataOneShotDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeSignatureDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeSignatureOneShotDynamicAlg;
import com.airbus.esar.fixture.comptime.ComptimeSignatureThreeArgDynamicAlg;
import com.airbus.esar.fixture.comptime.OkComptimeCryptoAlgorithms;
import com.airbus.esar.fixture.randomdata.DeprecatedRandomData;
import com.airbus.esar.fixture.randomdata.DeprecatedRandomDataCastPseudo;
import com.airbus.esar.fixture.randomdata.DeprecatedRandomDataDynamicAlg;
import com.airbus.esar.fixture.randomdata.DeprecatedRandomDataIfdsChallenging;
import com.airbus.esar.fixture.randomdata.DeprecatedRandomDataIfdsUnresolved;
import com.airbus.esar.fixture.randomdata.OkRandomData;
import com.airbus.esar.fixture.randomdata.OkRandomDataCastTrng;
import com.airbus.esar.junit.JcRuleAnalysisExtension;
import com.airbus.esar.junit.JcTestRunner;

@ExtendWith(JcRuleAnalysisExtension.class)
class TestJcCompileTimeUncertainty {
    @Test
    void testNoUncertaintyTrngDeepChain(final JcTestRunner analysis) {
        final Class<?> c = DeprecatedRandomDataIfdsChallenging.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "entryTrngDeepOnly"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyLocalCalleeMismatch(final JcTestRunner analysis) {
        final Class<?> c = DeprecatedRandomDataIfdsUnresolved.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "entryLocalConstantMismatch"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyParameterFedAlg(final JcTestRunner analysis) {
        final Class<?> c = DeprecatedRandomDataDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryParameterByte", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyCipherParamByte(final JcTestRunner analysis) {
        final Class<?> c = DeprecatedCipherDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryCipherByte", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyMessageDigestParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeMessageDigestDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryParameterByte", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyMessageDigestOneShotParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeMessageDigestOneShotDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryOneShot", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyInitializedMessageDigestParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeInitializedMessageDigestDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryParameterByte", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyInitializedMessageDigestOneShotParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeInitializedMessageDigestOneShotDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryOneShot", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintySignatureParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeSignatureDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryParameterByte", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintySignatureThreeArgParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeSignatureThreeArgDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryParameterByte", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintySignatureOneShotParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeSignatureOneShotDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryOneShot", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyDerivationFunctionParamShort(final JcTestRunner analysis) {
        final Class<?> c = ComptimeDerivationFunctionDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryParameterShort", "void", List.of("short"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyDerivationFunctionOneShotParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeDerivationFunctionOneShotDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryOneShot", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyKeyBuilderParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeKeyBuilderDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryParameterByte", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyKeyBuilderTwoByteParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeKeyBuilderTwoByteDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryParameterByte", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyKeyPairParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeKeyPairDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryParameterByte", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyChecksumParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeChecksumDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryParameterByte", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyAEADCipherParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeAEADCipherDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryParameterByte", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyAEADCipherThreeArgParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeAEADCipherThreeArgDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryParameterByte", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyAEADCipherOneShotParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeAEADCipherOneShotDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryOneShot", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyRandomDataOneShotParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeRandomDataOneShotDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryOneShot", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyKeyBuilderSharedDomainParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeKeyBuilderSharedDomainDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryParameterByte", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyKeyAgreementParamByte(final JcTestRunner analysis) {
        final Class<?> c = ComptimeKeyAgreementDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryParameterByte", "void", List.of("byte"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyCertificateParserParamShort(final JcTestRunner analysis) {
        final Class<?> c = ComptimeCertificateParserDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryParameterShort", "void", List.of("short"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testUncertaintyNamedParameterSpecParamShort(final JcTestRunner analysis) {
        final Class<?> c = ComptimeNamedParameterSpecDynamicAlg.class;
        analysis.analyse(c, List.of(JcTestRunner.sig(c, "entryParameterShort", "void", List.of("short"))));
        assertEquals(1, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyMessageDigestConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okMessageDigestGetInstance"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyMessageDigestOneShotConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okMessageDigestOneShot"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyInitializedMessageDigestConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okInitializedMessageDigestGetInstance"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyInitializedMessageDigestOneShotConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okInitializedMessageDigestOneShot"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintySignatureConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okSignatureGetInstance"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintySignatureThreeArgConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okSignatureThreeArgGetInstance"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintySignatureOneShotConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okSignatureOneShot"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyDerivationFunctionConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okDerivationFunctionGetInstance"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyDerivationFunctionOneShotConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okDerivationFunctionOneShot"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyKeyBuilderConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okKeyBuilderBuildKey"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyKeyBuilderTwoByteConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okKeyBuilderTwoByteBuildKey"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyKeyPairConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okKeyPairConstructor"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyChecksumConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okChecksumGetInstance"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyRandomDataOneShotConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okRandomDataOneShot"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyKeyBuilderSharedDomainConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okKeyBuilderSharedDomain"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyKeyAgreementConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okKeyAgreementGetInstance"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyCertificateParserConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okCertificateParserGetInstance"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyNamedParameterSpecConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okNamedParameterSpecGetInstance"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyKeyBuilderBuildXECKeyConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okKeyBuilderBuildXECKey"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyAEADCipherConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okAEADCipherGetInstance"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyAEADCipherThreeArgConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okAEADCipherThreeArgGetInstance"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyAEADCipherOneShotConstant(final JcTestRunner analysis) {
        final Class<?> c = OkComptimeCryptoAlgorithms.class;
        analysis.analyse(c, JcTestRunner.sigVoidNoArg(c, "okAEADCipherOneShot"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, c));
    }

    @Test
    void testNoUncertaintyTrngCast(final JcTestRunner analysis) {
        analysis.analyse(OkRandomDataCastTrng.class,
            JcTestRunner.sigVoidNoArg(OkRandomDataCastTrng.class, "entryCastTrng"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, OkRandomDataCastTrng.class));
        assertEquals(0, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, OkRandomDataCastTrng.class));
    }

    @Test
    void testCastPseudoUncertaintySilent(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedRandomDataCastPseudo.class,
            JcTestRunner.sigVoidNoArg(DeprecatedRandomDataCastPseudo.class, "entryCastPseudo"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, DeprecatedRandomDataCastPseudo.class));
        assertEquals(1, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, DeprecatedRandomDataCastPseudo.class));
    }

    @Test
    void testNoUncertaintyOkRandomData(final JcTestRunner analysis) {
        analysis.analyse(OkRandomData.class,
            JcTestRunner.sigVoidNoArg(OkRandomData.class, "ok1", "ok2", "ok3", "ok4"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, OkRandomData.class));
    }

    @Test
    void testOkRdBothSilent(final JcTestRunner analysis) {
        analysis.analyse(OkRandomData.class,
            JcTestRunner.sigVoidNoArg(OkRandomData.class, "ok1", "ok2", "ok3", "ok4"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, OkRandomData.class));
        assertEquals(0, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, OkRandomData.class));
    }

    @Test
    void testOkCipherBothSilent(final JcTestRunner analysis) {
        analysis.analyse(OkCipherAlgorithms.class,
            JcTestRunner.sigVoidNoArg(OkCipherAlgorithms.class, "entryDesEcb"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, OkCipherAlgorithms.class));
        assertEquals(0, analysis.countReports(JcDeprecatedCipherAlg.RULE_ID, OkCipherAlgorithms.class));
    }

    @Test
    void testPseudoUncertaintySilent(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedRandomData.class,
            JcTestRunner.sigVoidNoArg(DeprecatedRandomData.class, "pseudo"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, DeprecatedRandomData.class));
        assertEquals(1, analysis.countReports(JcDeprecatedRandomDataAlg.RULE_ID, DeprecatedRandomData.class));
    }

    @Test
    void testNoUncertaintyDeprecatedRdFields(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedRandomData.class,
            JcTestRunner.sigVoidNoArg(DeprecatedRandomData.class, "pseudo", "secure"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, DeprecatedRandomData.class));
    }

    @Test
    void testNoUncertaintyOkCipher(final JcTestRunner analysis) {
        analysis.analyse(OkCipherAlgorithms.class,
            JcTestRunner.sigVoidNoArg(OkCipherAlgorithms.class, "entryDesEcb"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, OkCipherAlgorithms.class));
    }

    @Test
    void testNoUncertaintyDeprecatedCipherFields(final JcTestRunner analysis) {
        analysis.analyse(DeprecatedCipherAlgorithms.class,
            JcTestRunner.sigVoidNoArg(DeprecatedCipherAlgorithms.class,
            "entryAes192Cbc", "entryRsaIso14888", "entryThreeArg"));
        assertEquals(0, analysis.countReports(JcCompileTimeUncertainty.RULE_ID, DeprecatedCipherAlgorithms.class));
    }
}
