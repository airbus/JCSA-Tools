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

package com.airbus.esar.fixture.comptime;

import javacard.security.Key;
import javacard.security.KeyBuilder;

public final class ComptimeKeyBuilderSharedDomainDynamicAlg {

    private static final Key DOMAIN_KEY =
            KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PARAMETERS, KeyBuilder.LENGTH_EC_FP_256, false);

    private ComptimeKeyBuilderSharedDomainDynamicAlg() {
    }

    public static void entryParameterByte(final byte algorithmType) {
        KeyBuilder.buildKeyWithSharedDomain(algorithmType, KeyBuilder.TYPE_AES, DOMAIN_KEY, false);
    }
}
