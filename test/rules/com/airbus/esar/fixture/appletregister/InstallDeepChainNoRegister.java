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

import javacard.framework.APDU;
import javacard.framework.Applet;

public final class InstallDeepChainNoRegister extends Applet {

    private InstallDeepChainNoRegister() {
    }

    public static void install(final byte[] bArray, final short bOffset, final byte bLength) {
        final InstallDeepChainNoRegister a = new InstallDeepChainNoRegister();
        a.stepOne();
    }

    private void stepOne() {
        stepTwo();
    }

    private void stepTwo() {
        finalizeSetup();
    }

    private void finalizeSetup() {
    }

    @Override
    public void process(final APDU apdu) {
    }
}
