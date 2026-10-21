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

package com.airbus.esar.internal.engine;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * Package-prefix blacklist for analysis traversal (platform / JCRE / JDK types).
 */
public final class TraversalPackageBlacklist {

    /** Default prefixes skipped when visiting classes. */
    public static final List<String> DEFAULT_PREFIXES = List.of(
            "java.",
            "javax.",
            "javacard.",
            "javacardx.",
            "com.sun.javacard.",
            "sim.toolkit.",
            "sim.access.",
            "uicc.access.",
            "uicc.system.",
            "uicc.toolkit.",
            "uicc.usim.",
            "org.globalplatform.",
            "visa.openplatform.");

    private TraversalPackageBlacklist() {
    }

    /**
     * @return copy of {@link #DEFAULT_PREFIXES}
     */
    public static List<String> defaults() {
        return List.copyOf(DEFAULT_PREFIXES);
    }

    /**
     * Defaults plus optional extra prefixes (deduplicated, order preserved).
     *
     * @param extra additional prefixes, or null/empty for defaults only
     * @return effective prefix list
     */
    public static List<String> mergeDefaults(final List<String> extra) {
        final LinkedHashSet<String> out = new LinkedHashSet<>(DEFAULT_PREFIXES);
        if (extra == null) {
            return List.copyOf(out);
        }

        for (final String prefix : extra) {
            if (prefix != null && !prefix.isEmpty()) {
                out.add(prefix);
            }
        }
        return List.copyOf(out);
    }

    /**
     * @param configured extra prefixes from CLI/YAML, or null
     * @param includeDefaults when false, only {@code configured} is used (empty disables blacklist)
     * @return effective prefix list
     */
    public static List<String> resolve(final List<String> configured, final boolean includeDefaults) {
        if (includeDefaults) {
            return mergeDefaults(configured);
        }
        return copyOf(configured);
    }

    /**
     * @param fqn fully qualified class name
     * @param prefixes package prefixes (typically ending with {@code .})
     * @return true when {@code fqn} starts with any prefix
     */
    public static boolean matches(final String fqn, final List<String> prefixes) {
        Objects.requireNonNull(fqn, "fqn");
        if (prefixes == null || prefixes.isEmpty()) {
            return false;
        }
        for (final String prefix : prefixes) {
            if (prefix != null && !prefix.isEmpty() && fqn.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param prefixes source list, or null
     * @return immutable copy, or empty when null
     */
    public static List<String> copyOf(final List<String> prefixes) {
        if (prefixes == null || prefixes.isEmpty()) {
            return List.of();
        }
        final List<String> out = new ArrayList<>(prefixes.size());
        for (final String prefix : prefixes) {
            if (prefix != null && !prefix.isEmpty()) {
                out.add(prefix);
            }
        }
        return List.copyOf(out);
    }
}
