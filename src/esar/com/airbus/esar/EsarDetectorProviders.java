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

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.airbus.esar.detect.ProgramVisitorIf;

/**
 * Loads EsarDetectorProvider via ServiceLoader (META-INF/services).
 */
public final class EsarDetectorProviders {
    private EsarDetectorProviders() {
    }

    /**
     * @param loader class loader for provider discovery
     * @return every detector from every registered provider, in discovery order
     */
    public static List<ProgramVisitorIf> getDetectors(final ClassLoader loader) {
        final List<ProgramVisitorIf> out = new ArrayList<>();
        for (EsarDetectorProvider provider : ServiceLoader.load(EsarDetectorProvider.class, loader)) {
            out.addAll(provider.detectors());
        }
        return out;
    }

    /**
     * loadAll with context class loader, else system class loader.
     *
     * @return combined detector list
     */
    public static List<ProgramVisitorIf> getDetectors() {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return getDetectors(cl != null ? cl : ClassLoader.getSystemClassLoader());
    }

    public static List<String> getRuleBundleNames() {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final List<String> out = new ArrayList<>();
        for (EsarDetectorProvider provider : ServiceLoader.load(EsarDetectorProvider.class, cl)) {
            out.addAll(provider.ruleMessageBundleNames());
        }

        return out;
    }
}
