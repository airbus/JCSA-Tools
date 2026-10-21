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

import com.airbus.esar.detect.ProgramVisitorIf;

/**
 * Supplies ProgramVisitorIf detectors for an AnalysisEngine run (SPI alongside rule modules).
 */
public interface EsarDetectorProvider {

    /**
     * @return new detector instances for this analysis run (each call may return fresh instances)
     */
    List<ProgramVisitorIf> detectors();

    /**
     * @return list of message bundle names
     */
    List<String> ruleMessageBundleNames();

    /**
     * @return plugin name
     */

    String name();

    /**
     * @return plugin version
     */
    String version();
}
