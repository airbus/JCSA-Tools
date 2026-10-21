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

package com.airbus.esar.junit;

import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import com.airbus.esar.testing.EsarAnalysisRunner;
import com.airbus.esar.detect.ProgramVisitorIf;
import com.airbus.esar.testing.EsarAnalysisExtension;

public final class JcRuleAnalysisExtension extends EsarAnalysisExtension {

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext,
                                     final ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == JcTestRunner.class
               || super.supportsParameter(parameterContext, extensionContext);
    }

    @Override
    protected EsarAnalysisRunner createDriver(final List<ProgramVisitorIf> detectors) {
        return new JcTestRunner(detectors);
    }
}
