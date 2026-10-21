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

package com.airbus.esar.fixture.hotfield;

/**
 * Five field reads after a switch. Jimple list order may put later reads first;
 * the frequent-access report should still anchor on the earliest source line.
 */
public final class HfFrequentReadAfterSwitch {
    private static volatile int sSink;
    private byte[] mBuf;

    public void entry(final byte kind) {
        switch (kind) {
            case 1:
                sSink = 1;
                break;
            case 2:
                sSink = 2;
                break;
            default:
                return;
        }
        sSink = mBuf.length;
        sSink += mBuf.length;
        sSink += mBuf.length;
        sSink += mBuf.length;
        sSink += mBuf.length;
    }
}
