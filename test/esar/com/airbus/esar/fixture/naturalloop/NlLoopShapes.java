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

package com.airbus.esar.fixture.naturalloop;

/** CFG shapes for {@code NaturalLoopStmts} tests. */
public final class NlLoopShapes {

    private static volatile int sSink;

    private NlLoopShapes() {
    }

    public static int whileLoop(final int n) {
        int sum = 0;
        int i = 0;
        while (i < n) {
            sum += i;
            i++;
        }
        return sum;
    }

    public static int forLoop(final int n) {
        int sum = 0;
        for (int i = 0; i < n; i++) {
            sum += i;
        }
        return sum;
    }

    public static int doWhileLoop(final int n) {
        int sum = 0;
        int i = 0;
        do {
            sum += i;
            i++;
        } while (i < n);
        return sum;
    }

    public static int nestedLoops(final int n) {
        int sum = 0;
        for (int i = 0; i < n; i++) {
            int j = 0;
            while (j < n) {
                sum += i + j;
                j++;
            }
        }
        return sum;
    }

    public static int ifElseOnly(final int n) {
        if (n > 0) {
            return n + 1;
        }
        return n - 1;
    }

    public static int straightLine(final int n) {
        final int a = n + 1;
        final int b = a * 2;
        return b - 3;
    }

    public static int loopThenStraight(final int n) {
        int i = 0;
        while (i < n) {
            i++;
        }
        sSink = i;
        return i + 1;
    }
}
