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

package com.airbus;

public class LocalInfo {
    private final String mName;
    private final int mWords;

    public LocalInfo(final String name, final int words) {
        mName = name;
        mWords = words;
    }

    public int getSizeBytes() {
        return mWords * JCVMStackAnalyser.WORD_SIZE;
    }

    public String toString() {
        return String.format("%-22s : %d bytes", mName, getSizeBytes());
    }
}
