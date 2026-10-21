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

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

import com.airbus.esar.detect.RuleMessageRef;

/**
 * Loads ResourceBundle patterns (try base names in order) and formats with MessageFormat.
 */
public final class DefaultRuleMessageResolver implements RuleMessageResolverIf {

    private final ClassLoader mClassLoader;
    private final Locale mLocale;
    private final List<String> mBundleBaseNames;

    /**
     * @param classLoader loader for the rule module(s) that contain the bundle classes
     * @param locale locale for formatting and bundle selection
     * @param bundleBaseNames fully qualified base names (e.g. {@code com.airbus.esar.messages.JavacardRuleMessages})
     */
    public DefaultRuleMessageResolver(final ClassLoader classLoader,
                                      final Locale locale,
                                      final List<String> bundleBaseNames) {
        mClassLoader = Objects.requireNonNull(classLoader, "classLoader");
        mLocale = Objects.requireNonNull(locale, "locale");
        mBundleBaseNames = List.copyOf(Objects.requireNonNull(bundleBaseNames, "bundleBaseNames"));
        if (mBundleBaseNames.isEmpty()) {
            throw new IllegalArgumentException("bundleBaseNames must not be empty");
        }
    }

    @Override
    public ResolvedRuleMessage resolve(final RuleMessageRef ref) {
        Objects.requireNonNull(ref, "ref");
        final String key = ref.getKey();
        MissingResourceException last = null;
        for (String baseName : mBundleBaseNames) {
            try {
                final ResourceBundle bundle = ResourceBundle.getBundle(baseName, mLocale, mClassLoader);
                final String pattern = bundle.getString(key);
                final MessageFormat mf = new MessageFormat(pattern, mLocale);
                final String text = mf.format(ref.getArgs());
                return new ResolvedRuleMessage(key, text);
            } catch (final MissingResourceException ex) {
                last = ex;
            }
        }
        throw new IllegalArgumentException(
                "No resource bundle defines message key \"" + key + "\" in " + mBundleBaseNames,
                last);
    }
}
