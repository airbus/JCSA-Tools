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

package com.airbus.esar.fixture.implicitwiden;

/**
 * False positive candidate: {@code switch (w)} on an {@code int} temp from {@code byte} with case {@code 1}.
 * Expect zero reports.
 */
public final class IbwFpSwitchStmtIntKeyFromByteEqOne {
    private static volatile int sSink;

    private IbwFpSwitchStmtIntKeyFromByteEqOne() {
    }

    public static void entry(final byte b) {
        final int w = b;
        switch (w) {
            case 1:
                sSink = 1;
                break;
            default:
                break;
        }
    }
}
