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

/**
 * Detector that accepts a typed configuration object bound from rule YAML.
 *
 * @param <C> per-rule configuration type (typically a nested {@code Config} class)
 */
public interface ConfigurableDetector<C> {

    /**
     * @return configuration bean class used for YAML binding
     */
    Class<C> configClass();

    /**
     * @param config bound configuration for this analysis run
     */
    void configure(C config);
}
