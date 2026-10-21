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

/** Object field used as invoke base in try and catch; no loop. */
public final class HfFieldReadInTryCatch {
    private static volatile int sSink;
    private Worker mWorker;

    public void entry() {
        try {
            if (sSink == 1) {
                mWorker.doWork();
            } else {
                mWorker.doWork();
                sSink = 2;
            }
            risky();
        } catch (final RuntimeException ex) {
            mWorker.recover();
        }
    }

    private static void risky() {
        throw new RuntimeException();
    }

    private static final class Worker {
        void doWork() {
            sSink = 1;
        }

        void recover() {
            sSink = 2;
        }
    }
}
