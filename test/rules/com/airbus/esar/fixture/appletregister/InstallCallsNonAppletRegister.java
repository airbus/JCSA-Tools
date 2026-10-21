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

/**
 * The {@code install} method calls {@code register()} on a helper object whose class does NOT
 * extend {@code Applet}. The rule must skip that call (L101 continue) and still find the real
 * {@code register()} invoked on the applet itself. Should be silent.
 */
public final class InstallCallsNonAppletRegister extends Applet {

    /** Non-Applet helper that happens to declare a method named {@code register()}. */
    static final class RegisterHelper {
        void register() { /* no-op */ }
    }

    private InstallCallsNonAppletRegister() {
    }

    public static void install(final byte[] bArray, final short bOffset, final byte bLength) {
        new RegisterHelper().register();
        new InstallCallsNonAppletRegister().register();
    }

    @Override
    public void process(final APDU apdu) {
    }
}
