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

/** {@code int test = byteArray[0]; return test;} in a large branched method. */
public final class IbwReturnIntLocalFromByteArrayElementInLargeMethod {
    private static volatile int sSink;

    private IbwReturnIntLocalFromByteArrayElementInLargeMethod() {
    }

    public static int test(final byte[] byteArray) {
        int v0 = 0;
        int v1 = 0;
        int v2 = 0;
        int v3 = 0;
        int v4 = 0;
        int v5 = 0;
        int v6 = 0;
        int v7 = 0;
        int v8 = 0;
        int v9 = 0;
        int v10 = 0;
        int v11 = 0;
        int v12 = 0;
        int v13 = 0;
        int v14 = 0;
        for (int round = 0; round < 80; round++) {
            if (byteArray.length > round) {
                v0 = byteArray[0];
            } else if (byteArray.length == round) {
                v0 = v1 + 1;
            } else {
                v0 = v1 - 1;
            }
            if (byteArray.length > round + 1) {
                v1 = byteArray[1];
            } else if (byteArray.length == round + 1) {
                v1 = v2 + 1;
            } else {
                v1 = v2 - 1;
            }
            if (byteArray.length > round + 2) {
                v2 = byteArray[2];
            } else if (byteArray.length == round + 2) {
                v2 = v3 + 1;
            } else {
                v2 = v3 - 1;
            }
            if (byteArray.length > round + 3) {
                v3 = byteArray[0];
            } else if (byteArray.length == round + 3) {
                v3 = v4 + 1;
            } else {
                v3 = v4 - 1;
            }
            if (byteArray.length > round + 4) {
                v4 = byteArray[1];
            } else if (byteArray.length == round + 4) {
                v4 = v5 + 1;
            } else {
                v4 = v5 - 1;
            }
            if (byteArray.length > round + 5) {
                v5 = byteArray[2];
            } else if (byteArray.length == round + 5) {
                v5 = v6 + 1;
            } else {
                v5 = v6 - 1;
            }
            if (byteArray.length > round + 6) {
                v6 = byteArray[0];
            } else if (byteArray.length == round + 6) {
                v6 = v7 + 1;
            } else {
                v6 = v7 - 1;
            }
            if (byteArray.length > round + 7) {
                v7 = byteArray[1];
            } else if (byteArray.length == round + 7) {
                v7 = v8 + 1;
            } else {
                v7 = v8 - 1;
            }
            if (byteArray.length > round + 8) {
                v8 = byteArray[2];
            } else if (byteArray.length == round + 8) {
                v8 = v9 + 1;
            } else {
                v8 = v9 - 1;
            }
            if (byteArray.length > round + 9) {
                v9 = byteArray[0];
            } else if (byteArray.length == round + 9) {
                v9 = v10 + 1;
            } else {
                v9 = v10 - 1;
            }
            if (byteArray.length > round + 10) {
                v10 = byteArray[1];
            } else if (byteArray.length == round + 10) {
                v10 = v11 + 1;
            } else {
                v10 = v11 - 1;
            }
            if (byteArray.length > round + 11) {
                v11 = byteArray[2];
            } else if (byteArray.length == round + 11) {
                v11 = v12 + 1;
            } else {
                v11 = v12 - 1;
            }
            if (byteArray.length > round + 12) {
                v12 = byteArray[0];
            } else if (byteArray.length == round + 12) {
                v12 = v13 + 1;
            } else {
                v12 = v13 - 1;
            }
            if (byteArray.length > round + 13) {
                v13 = byteArray[1];
            } else if (byteArray.length == round + 13) {
                v13 = v14 + 1;
            } else {
                v13 = v14 - 1;
            }
            if (byteArray.length > round + 14) {
                v14 = byteArray[2];
            } else if (byteArray.length == round + 14) {
                v14 = v0 + 1;
            } else {
                v14 = v0 - 1;
            }
        }
        final int test = byteArray[0];
        return test;
    }

    public static void entry(final byte[] byteArray) {
        sSink = test(byteArray);
    }
}
