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

package com.airbus.esar.fixture.appletregister;

/**
 * install calls inherited {@code syncRegister}; bytecode methodref uses this class, body is on the
 * superclass (same pattern as jc_sync TestApplet3).
 */
public final class InstallCallsInheritedSyncRegister extends RegisteringSyncBaseApplet {

    private InstallCallsInheritedSyncRegister() {
    }

    public static void install(final byte[] bArray, final short bOffset, final byte bLength) {
        new InstallCallsInheritedSyncRegister().syncRegister();
    }
}
