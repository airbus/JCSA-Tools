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

package com.airbus.esar.internal.i18n;

import com.airbus.esar.detect.RuleMessageRef;

/**
 * Used when no message bundles are configured (e.g. engines that never call {@code traverse} with reporting).
 */
public enum UnsupportedRuleMessageResolver implements RuleMessageResolverIf {
    /** Singleton. */
    INSTANCE;

    @Override
    public ResolvedRuleMessage resolve(final RuleMessageRef ref) {
        throw new UnsupportedOperationException(
                "Rule message resolution is not configured (missing ResourceBundle base names). Key: " + ref.getKey());
    }
}
