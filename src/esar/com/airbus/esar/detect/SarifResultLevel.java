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
 * Values for SARIF 2.1.0 {@code result.level} (failure level). See OASIS SARIF spec §3.27.10.
 */
public enum SarifResultLevel {

    /** SARIF {@code "none"} */
    NONE("none"),

    /** SARIF {@code "note"} */
    NOTE("note"),

    /** SARIF {@code "warning"} (default in SARIF when level is omitted) */
    WARNING("warning"),

    /** SARIF {@code "error"} */
    ERROR("error");

    private final String mSarifString;

    SarifResultLevel(final String sarifString) {
        mSarifString = sarifString;
    }

    /**
     * @return the JSON string written for SARIF {@code level}
     */
    public String toString() {
        return mSarifString;
    }
}
