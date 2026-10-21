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

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Shared analyses once per AnalysisEngine.traverse batch (before ProgramVisitorIf.startAnalysis).
 */
public interface SharedSolverRegistryIf {

    /**
     * Build and register new solver if there isn't existing one.
     *
     * @param <D> expected value type
     * @param key solver unique key
     * @param createIfAbsent callback for creating the solver if doesn't exist
     * @return solver with the requested type
     */
    <D> D lazyRegister(SharedSolverKey<D> key, Supplier<D> createIfAbsent);

    /**
     * @param <D> expected value type
     * @param key stable id (type parameter is for documentation / call-site inference)
     * @return present when this key was installed for the current traversal
     */
    <D> Optional<D> get(SharedSolverKey<D> key);

    /**
     * @param <D> expected value type
     * @param key stable id
     * @return non-empty value
     * @throws IllegalStateException when absent
     */
    default <D> D getRequired(final SharedSolverKey<D> key) {
        return get(key).orElseThrow(
                () -> new IllegalStateException("No shared solver registered for key: " + key.id()));
    }
}
