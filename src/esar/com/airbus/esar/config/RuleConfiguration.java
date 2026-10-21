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

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import com.airbus.esar.detect.ConfigurableDetector;
import com.airbus.esar.detect.ProgramVisitorIf;
import com.airbus.esar.detect.SarifResultLevel;

/**
 * Loads and applies per-rule YAML configuration (enabled, severity, typed Config fields).
 */
public final class RuleConfiguration {

    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_SEVERITY = "severity";

    private final Map<String, RuleSettings> mByRuleId;

    private RuleConfiguration(final Map<String, RuleSettings> byRuleId) {
        mByRuleId = byRuleId;
    }

    /**
     * @return configuration that leaves detectors at their defaults
     */
    public static RuleConfiguration empty() {
        return new RuleConfiguration(Map.of());
    }

    /**
     * @param path rule configuration file path; null or blank yields {@link #empty()}
     * @return configuration loaded from the file
     */
    public static RuleConfiguration fromPath(final String path) {
        if (path == null || path.isBlank()) {
            return empty();
        }
        final Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(Path.of(path))) {
            final Object loaded = yaml.load(in);
            if (loaded == null) {
                return empty();
            }
            if (!(loaded instanceof Map<?, ?> root)) {
                System.err.println("Invalid rule configuration \"" + path + "\"");
                return empty();
            }
            return new RuleConfiguration(parseRules(root));
        } catch (final IOException e) {
            System.err.println("Cannot read rule configuration \"" + path + "\"");
            return empty();
        }
    }

    /**
     * @param ruleId detector rule id
     * @return settings for the rule, or defaults when the rule is absent from YAML
     */
    public RuleSettings settingsFor(final String ruleId) {
        Objects.requireNonNull(ruleId, "ruleId");
        return mByRuleId.getOrDefault(ruleId, RuleSettings.defaults());
    }

    /**
     * @return map of rule id to configured severity for rules that set severity in YAML
     */
    public Map<String, SarifResultLevel> severityOverrides() {
        final Map<String, SarifResultLevel> overrides = new HashMap<>();
        for (final Map.Entry<String, RuleSettings> entry : mByRuleId.entrySet()) {
            entry.getValue().getSeverity().ifPresent(level -> overrides.put(entry.getKey(), level));
        }
        return Map.copyOf(overrides);
    }

    /**
     * Filters disabled detectors and binds typed Config onto {@link ConfigurableDetector} instances.
     *
     * @param detectors loaded detector instances
     * @return enabled detectors with configuration applied
     */
    public List<ProgramVisitorIf> configureDetectors(final List<ProgramVisitorIf> detectors) {
        Objects.requireNonNull(detectors, "detectors");
        final List<ProgramVisitorIf> prepared = new ArrayList<>();
        for (final ProgramVisitorIf detector : detectors) {
            final RuleSettings settings = settingsFor(detector.ruleId());
            if (!settings.isEnabled()) {
                continue;
            }
            if (detector instanceof ConfigurableDetector<?> configurable) {
                configureDetector(configurable, settings);
            }
            prepared.add(detector);
        }
        return List.copyOf(prepared);
    }

    /**
     * Writes default per-rule enabled, severity, and typed Config fields for the given detectors.
     *
     * @param detectors loaded rule visitors
     * @param output destination file path
     * @throws IOException when the file cannot be written
     */
    public static void dumpDefaults(final List<ProgramVisitorIf> detectors,
                                    final Path output) throws IOException {
        Objects.requireNonNull(detectors, "detectors");
        Objects.requireNonNull(output, "output");

        final List<Map<String, Map<String, Object>>> rules = new ArrayList<>();
        detectors.stream()
                .sorted(Comparator.comparing(ProgramVisitorIf::ruleId))
                .forEach(detector -> {
                    final Map<String, Object> settings = new LinkedHashMap<>();
                    settings.put(KEY_ENABLED, true);
                    settings.put(KEY_SEVERITY, detector.severity().toString());
                    if (detector instanceof ConfigurableDetector<?> configurable) {
                        settings.putAll(defaultConfigMap(configurable));
                    }
                    rules.add(Map.of(detector.ruleId(), settings));
                });

        final Map<String, Object> root = new LinkedHashMap<>();
        root.put("rules", rules);

        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        final Yaml yaml = new Yaml(options);
        try (Writer writer = Files.newBufferedWriter(output)) {
            yaml.dump(root, writer);
        }
    }

    /**
     * Binds leftover YAML fields to a typed config bean.
     *
     * @param fields leftover keys from the rule block
     * @param type config class
     * @param <C> config type
     * @return bound instance (default-constructed when fields are empty)
     */
    public static <C> C bindConfig(final Map<String, Object> fields, final Class<C> type) {
        Objects.requireNonNull(fields, "fields");
        Objects.requireNonNull(type, "type");
        if (fields.isEmpty()) {
            return newConfigInstance(type);
        }
        final LoaderOptions loaderOptions = new LoaderOptions();
        final Constructor constructor = new Constructor(type, loaderOptions);
        final Yaml yaml = new Yaml(constructor);
        final C bound = yaml.loadAs(new Yaml().dump(fields), type);
        if (bound == null) {
            return newConfigInstance(type);
        }
        return bound;
    }

    @SuppressWarnings("unchecked")
    private static <C> void configureDetector(final ConfigurableDetector<C> detector,
                                              final RuleSettings settings) {
        final C config = bindConfig(settings.getTypedFields(), detector.configClass());
        detector.configure(config);
    }

    private static <C> C newConfigInstance(final Class<C> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (final InstantiationException
                       | IllegalAccessException
                       | InvocationTargetException
                       | NoSuchMethodException e) {
            throw new IllegalStateException("Cannot construct config type " + type.getName(), e);
        }
    }

    private static Map<String, Object> defaultConfigMap(final ConfigurableDetector<?> configurable) {
        final Object defaults = newConfigInstance(configurable.configClass());
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        final Representer representer = new Representer(options);
        representer.addClassTag(configurable.configClass(), Tag.MAP);
        final Yaml yaml = new Yaml(representer, options);
        final Object loaded = yaml.load(yaml.dump(defaults));
        if (!(loaded instanceof Map<?, ?> map)) {
            return Map.of();
        }
        final Map<String, Object> out = new LinkedHashMap<>();
        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() instanceof String key) {
                out.put(key, entry.getValue());
            }
        }
        return out;
    }

    private static Map<String, RuleSettings> parseRules(final Map<?, ?> root) {
        final Object rulesObj = root.get("rules");
        if (!(rulesObj instanceof List<?> rulesList) || rulesList.isEmpty()) {
            return Map.of();
        }

        final Map<String, RuleSettings> byRuleId = new HashMap<>();
        for (final Object blockObj : rulesList) {
            parseRuleBlock(byRuleId, blockObj);
        }
        return Map.copyOf(byRuleId);
    }

    private static void parseRuleBlock(final Map<String, RuleSettings> byRuleId, final Object blockObj) {
        if (!(blockObj instanceof Map<?, ?> block) || block.isEmpty()) {
            return;
        }
        for (final Map.Entry<?, ?> entry : block.entrySet()) {
            if (!(entry.getKey() instanceof String ruleId) || ruleId.isBlank()) {
                continue;
            }
            final Object value = entry.getValue();
            if (value == null) {
                byRuleId.put(ruleId, RuleSettings.defaults());
                continue;
            }
            if (!(value instanceof Map<?, ?> settings)) {
                System.err.println("Invalid settings for rule \"" + ruleId + "\"");
                continue;
            }
            byRuleId.put(ruleId, parseSettings(ruleId, settings));
        }
    }

    private static RuleSettings parseSettings(final String ruleId, final Map<?, ?> settings) {
        final boolean enabled = parseEnabled(settings.get(KEY_ENABLED));
        final Optional<SarifResultLevel> severity =
                parseSeverity(ruleId, asString(settings.get(KEY_SEVERITY)));
        final Map<String, Object> typedFields = new LinkedHashMap<>();
        for (final Map.Entry<?, ?> entry : settings.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                continue;
            }
            if (KEY_ENABLED.equals(key) || KEY_SEVERITY.equals(key)) {
                continue;
            }
            typedFields.put(key, entry.getValue());
        }
        return RuleSettings.of(enabled, severity, typedFields);
    }

    private static boolean parseEnabled(final Object value) {
        if (value instanceof Boolean enabled) {
            return enabled;
        }
        return true;
    }

    private static String asString(final Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Optional<SarifResultLevel> parseSeverity(final String ruleId, final String severity) {
        if (severity == null || severity.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(SarifResultLevel.valueOf(severity.trim().toUpperCase(Locale.ROOT)));
        } catch (final IllegalArgumentException e) {
            System.err.println("Unknown severity \"" + severity + "\" for rule \"" + ruleId + "\"");
            return Optional.empty();
        }
    }
}
