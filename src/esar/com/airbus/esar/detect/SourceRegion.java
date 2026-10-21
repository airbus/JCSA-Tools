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
 * Text region aligned with SARIF {@code region} (line/column bounds from the analysis model).
 */
public final class SourceRegion {

    private final int mFirstLine;
    private final int mLastLine;
    private final int mFirstCol;
    private final int mLastCol;

    /**
     * @param firstLine 1-based start line
     * @param lastLine 1-based end line
     * @param firstCol 0-based or 1-based per SARIF convention used at export time
     * @param lastCol end column
     */
    public SourceRegion(final int firstLine, final int lastLine, final int firstCol, final int lastCol) {
        mFirstLine = firstLine;
        mLastLine = lastLine;
        mFirstCol = firstCol;
        mLastCol = lastCol;
    }

    public int getFirstLine() {
        return mFirstLine;
    }

    public int getLastLine() {
        return mLastLine;
    }

    public int getFirstCol() {
        return mFirstCol;
    }

    public int getLastCol() {
        return mLastCol;
    }
}
