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

package com.airbus.esar.config;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.airbus.esar.detect.SarifResultLevel;

/**
 * Per-rule settings from rule configuration YAML: enablement, optional severity override,
 * and leftover keys for typed detector config binding.
 */
public final class RuleSettings {

    private final boolean mEnabled;
    private final Optional<SarifResultLevel> mSeverity;
    private final Map<String, Object> mTypedFields;

    private RuleSettings(final boolean enabled,
                         final Optional<SarifResultLevel> severity,
                         final Map<String, Object> typedFields) {
        mEnabled = enabled;
        mSeverity = Objects.requireNonNull(severity, "severity");
        mTypedFields = Objects.requireNonNull(typedFields, "typedFields");
    }

    /**
     * @return settings with enabled true, no severity override, empty typed fields
     */
    public static RuleSettings defaults() {
        return new RuleSettings(true, Optional.empty(), Map.of());
    }

    /**
     * @param enabled whether the rule runs
     * @param severity optional severity override
     * @param typedFields YAML keys other than enabled/severity for Config binding
     * @return settings instance
     */
    public static RuleSettings of(final boolean enabled,
                                  final Optional<SarifResultLevel> severity,
                                  final Map<String, Object> typedFields) {
        return new RuleSettings(
            enabled,
            Objects.requireNonNull(severity, "severity"),
            Map.copyOf(Objects.requireNonNull(typedFields, "typedFields"))
        );
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public Optional<SarifResultLevel> getSeverity() {
        return mSeverity;
    }

    /**
     * @return leftover YAML fields for typed Config binding (unmodifiable)
     */
    public Map<String, Object> getTypedFields() {
        return mTypedFields;
    }
}
