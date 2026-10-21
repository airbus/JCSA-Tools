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

package com.airbus.esar.testing;

import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.airbus.esar.EsarDetectorProviders;
import com.airbus.esar.detect.ProgramVisitorIf;

/** JUnit extension: EsarAnalysisDriver with SPI-loaded detectors; override createDriver for custom drivers. */
public abstract class EsarAnalysisExtension implements ParameterResolver {

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext,
                                     final ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == EsarAnalysisRunner.class;
    }

    @Override
    public final Object resolveParameter(final ParameterContext parameterContext,
                                         final ExtensionContext extensionContext) throws ParameterResolutionException {
        final ClassLoader loader = extensionContext.getRequiredTestClass().getClassLoader();
        return createDriver(EsarDetectorProviders.getDetectors(loader));
    }

    /**
     * @param detectors SPI-loaded visitors
     * @return driver injected into tests
     */
    protected abstract EsarAnalysisRunner createDriver(List<ProgramVisitorIf> detectors);
}
