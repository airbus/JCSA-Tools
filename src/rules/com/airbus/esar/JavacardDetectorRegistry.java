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

package com.airbus.esar;

import java.util.List;

import com.airbus.esar.detect.JcApduBufferState;
import com.airbus.esar.detect.JcAtomicTransactionCorrectness;
import com.airbus.esar.detect.JcCompileTimeUncertainty;
import com.airbus.esar.detect.JcCallGraphCycle;
import com.airbus.esar.detect.JcCatchException;
import com.airbus.esar.detect.JcRuntimeExceptionsNotAllowed;
import com.airbus.esar.detect.JcCustomException;
import com.airbus.esar.detect.JcImplicitIntegralWidening;
import com.airbus.esar.detect.JcIsoExceptionInvalidSw;
import com.airbus.esar.detect.JcUnknownArraySize;
import com.airbus.esar.detect.JcDeprecatedCipherAlg;
import com.airbus.esar.detect.JcDeprecatedRandomDataAlg;
import com.airbus.esar.detect.JcDeprecatedSignatureAlg;
import com.airbus.esar.detect.JcDirectRecursion;
import com.airbus.esar.detect.JcHotField;
import com.airbus.esar.detect.JcMissingUninstall;
import com.airbus.esar.detect.JcOneShotUsage;
import com.airbus.esar.detect.JcOwnerPinCheck;
import com.airbus.esar.detect.JcPackageBlackList;
import com.airbus.esar.detect.JcProcessAllocation;
import com.airbus.esar.detect.JcRecordLikeClass;
import com.airbus.esar.detect.JcRegisterMissingLifeCycle;
import com.airbus.esar.detect.JcShareableMultiSelectable;
import com.airbus.esar.detect.JcSignedRightShift;
import com.airbus.esar.detect.ProgramVisitorIf;
import com.airbus.esar.messages.JavacardRuleMessages;

/**
 * Default EsarDetectorProvider for Java Card rules (ServiceLoader, META-INF/services).
 */
public final class JavacardDetectorRegistry implements EsarDetectorProvider {
    private static final String NAME = "javacard";
    private static final String VERSION = "1.1.0-NB";

    @Override
    public List<ProgramVisitorIf> detectors() {
        return List.of(
            new JcDeprecatedRandomDataAlg(),
            new JcDeprecatedCipherAlg(),
            new JcDeprecatedSignatureAlg(),
            new JcCompileTimeUncertainty(),
            new JcOneShotUsage(),
            new JcDirectRecursion(),
            new JcCallGraphCycle(),
            new JcRegisterMissingLifeCycle(),
            new JcSignedRightShift(),
            new JcPackageBlackList(),
            new JcShareableMultiSelectable(),
            new JcMissingUninstall(),
            new JcCustomException(),
            new JcCatchException(),
            new JcRuntimeExceptionsNotAllowed(),
            new JcProcessAllocation(),
            new JcRecordLikeClass(),
            new JcAtomicTransactionCorrectness(),
            new JcApduBufferState(),
            new JcImplicitIntegralWidening(),
            new JcUnknownArraySize(),
            new JcIsoExceptionInvalidSw(),
            /* new JcIsoExceptionReplyBusy(), */
            new JcHotField(),
            new JcOwnerPinCheck()
        );
    }
    public List<String> ruleMessageBundleNames() {
        return List.of(JavacardRuleMessages.class.getName());
    }
    @Override
    public String name() {
        return NAME;
    }
    @Override
    public String version() {
        return VERSION;
    }
}
