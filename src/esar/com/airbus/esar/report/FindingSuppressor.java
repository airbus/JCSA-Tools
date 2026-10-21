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

package com.airbus.esar.report;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.airbus.esar.detect.EsarFinding;
import com.airbus.esar.detect.SourceRegion;

/**
 * Indexes {@code // NOESAR} and {@code // EsarSuppress: ...} comments in source files for finding filtering.
 */
public final class FindingSuppressor {

    private static final Pattern NO_ESAR = Pattern.compile("^NOESAR\\s*$");
    private static final Pattern ESAR_SUPPRESS = Pattern.compile(
            "^EsarSuppress:\\s*(\\S+)(?:\\s*\\+\\s*(\\d+)\\s*lines?)?\\s*$");

    private final Map<String, FileSuppressions> mByClassFqn;

    private FindingSuppressor(final Map<String, FileSuppressions> byClassFqn) {
        mByClassFqn = byClassFqn;
    }

    /**
     * @return suppressor that never filters findings
     */
    public static FindingSuppressor empty() {
        return new FindingSuppressor(Map.of());
    }

    /**
     * Walks {@code *.java} under each root and parses suppression comments.
     *
     * @param sourcePaths source roots; null or empty yields {@link #empty()}
     * @return suppressor built from discovered comments
     */
    public static FindingSuppressor fromSourcePaths(final List<String> sourcePaths) {
        if (sourcePaths == null || sourcePaths.isEmpty()) {
            return empty();
        }
        final Map<String, FileSuppressions> byClassFqn = new HashMap<>();
        for (final String root : sourcePaths) {
            if (root == null || root.isBlank()) {
                continue;
            }
            indexSourceTree(Path.of(root), byClassFqn);
        }
        return new FindingSuppressor(Map.copyOf(byClassFqn));
    }

    private static String stripSubClass(final String className) {
        final int dollarIndex = className.indexOf('$');
        if (dollarIndex != -1) {
            return className.substring(0, dollarIndex);
        }
        return className;
    }

    /**
     * @param finding finding to test
     * @return true when a source comment suppresses this finding
     */
    public boolean isSuppressed(final EsarFinding finding) {
        Objects.requireNonNull(finding, "finding");
        final FileSuppressions suppressions = mByClassFqn.get(stripSubClass(finding.getClassSignature()));
        if (suppressions == null) {
            return false;
        }
        final Optional<SourceRegion> region = finding.getRegion();
        if (region.isEmpty()) {
            return suppressions.isFileWideSuppressed(finding.getRuleId());
        }
        final int firstLine = region.get().getFirstLine();
        final int lastLine = region.get().getLastLine() > 0 ? region.get().getLastLine() : firstLine;
        if (suppressions.overlapsNoEsar(firstLine, lastLine)) {
            return true;
        }
        return suppressions.isRuleSuppressed(finding.getRuleId(), firstLine, lastLine);
    }

    private static void indexSourceTree(final Path root, final Map<String, FileSuppressions> byClassFqn) {
        if (!Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".java"))
                    .forEach(path -> indexJavaFile(root, path, byClassFqn));
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read source tree: " + root, e);
        }
    }

    private static void indexJavaFile(final Path sourceRoot,
                                      final Path javaFile,
                                      final Map<String, FileSuppressions> byClassFqn) {
        final String classFqn = classFqnFromSourcePath(sourceRoot, javaFile);
        if (classFqn == null) {
            return;
        }
        final FileSuppressions suppressions = byClassFqn.computeIfAbsent(classFqn, k -> new FileSuppressions());
        final List<String> lines;
        try {
            lines = Files.readAllLines(javaFile, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read " + javaFile, e);
        }
        for (int i = 0; i < lines.size(); i++) {
            parseComment(lines.get(i), i + 1, suppressions);
        }
    }

    private static String classFqnFromSourcePath(final Path sourceRoot, final Path javaFile) {
        final Path normalizedRoot = sourceRoot.toAbsolutePath().normalize();
        final Path normalizedFile = javaFile.toAbsolutePath().normalize();
        if (!normalizedFile.startsWith(normalizedRoot)) {
            return null;
        }
        final Path relative = normalizedRoot.relativize(normalizedFile);
        final String relativePath = relative.toString().replace('\\', '/');
        if (!relativePath.endsWith(".java")) {
            return null;
        }
        return relativePath.substring(0, relativePath.length() - ".java".length()).replace('/', '.');
    }

    private static void parseComment(final String line, final int lineNumber, final FileSuppressions suppressions) {
        final Optional<String> comment = extractCommentText(line);
        if (comment.isEmpty()) {
            return;
        }
        final String text = comment.get();
        if (NO_ESAR.matcher(text).matches()) {
            suppressions.addNoEsarLine(lineNumber);
            return;
        }
        final Matcher matcher = ESAR_SUPPRESS.matcher(text);
        if (!matcher.matches()) {
            return;
        }
        final String ruleId = matcher.group(1);
        final String lineCountGroup = matcher.group(2);
        if (lineCountGroup == null) {
            suppressions.addFileWideRule(ruleId);
        } else {
            final int lineCount = Integer.parseInt(lineCountGroup);
            if (lineCount > 0) {
                suppressions.addRuleLineRange(ruleId, lineNumber + 1, lineNumber + lineCount);
            }
        }
    }

    private static Optional<String> extractCommentText(final String line) {
        boolean inString = false;
        for (int i = 0; i < line.length() - 1; i++) {
            final char c = line.charAt(i);
            if (c == '"' && (i == 0 || line.charAt(i - 1) != '\\')) {
                inString = !inString;
            }
            if (!inString && line.charAt(i) == '/' && line.charAt(i + 1) == '/') {
                final String text = line.substring(i + 2).trim();
                return text.isEmpty() ? Optional.empty() : Optional.of(text);
            }
        }
        return Optional.empty();
    }

    private static final class FileSuppressions {
        private final Set<Integer> mNoEsarLines = new HashSet<>();
        private final Set<String> mFileWideRuleIds = new HashSet<>();
        private final Set<RuleLineRange> mRuleLineRanges = new HashSet<>();

        private void addNoEsarLine(final int lineNumber) {
            mNoEsarLines.add(lineNumber);
        }

        private void addFileWideRule(final String ruleId) {
            mFileWideRuleIds.add(ruleId);
        }

        private void addRuleLineRange(final String ruleId, final int startLine, final int endLine) {
            mRuleLineRanges.add(new RuleLineRange(ruleId, startLine, endLine));
        }

        private boolean overlapsNoEsar(final int firstLine, final int lastLine) {
            for (int line = firstLine; line <= lastLine; line++) {
                if (mNoEsarLines.contains(line)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isFileWideSuppressed(final String ruleId) {
            return mFileWideRuleIds.contains(ruleId);
        }

        private boolean isRuleSuppressed(final String ruleId, final int firstLine, final int lastLine) {
            if (mFileWideRuleIds.contains(ruleId)) {
                return true;
            }
            for (final RuleLineRange range : mRuleLineRanges) {
                if (range.overlaps(ruleId, firstLine, lastLine)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class RuleLineRange {
        private final String mRuleId;
        private final int mStartLine;
        private final int mEndLine;

        private RuleLineRange(final String ruleId, final int startLine, final int endLine) {
            mRuleId = ruleId;
            mStartLine = startLine;
            mEndLine = endLine;
        }

        private boolean overlaps(final String ruleId, final int firstLine, final int lastLine) {
            return mRuleId.equals(ruleId) && firstLine <= mEndLine && lastLine >= mStartLine;
        }

        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof RuleLineRange that)) {
                return false;
            }
            return mRuleId.equals(that.mRuleId)
                    && mStartLine == that.mStartLine
                    && mEndLine == that.mEndLine;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mRuleId, mStartLine, mEndLine);
        }
    }
}
