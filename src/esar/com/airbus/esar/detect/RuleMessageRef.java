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

import java.util.Objects;

/**
 * Globally unique message key plus format arguments. Resolved by RuleMessageResolverIf; BugReporterIf rejects raw
 * strings.
 */
public final class RuleMessageRef {

    private final String mKey;
    private final Object[] mArgs;

    private RuleMessageRef(final String key, final Object[] args) {
        mKey = Objects.requireNonNull(key, "key");
        mArgs = args.clone();
    }

    /**
     * @param key globally unique message id (matches SARIF {@code message.id} when exported)
     * @param args MessageFormat arguments
     * @return non-null ref
     */
    public static RuleMessageRef of(final String key, final Object... args) {
        Objects.requireNonNull(args, "args");
        return new RuleMessageRef(key, args);
    }

    public String getKey() {
        return mKey;
    }

    /**
     * @return a defensive copy of format arguments
     */
    public Object[] getArgs() {
        return mArgs.clone();
    }
}
