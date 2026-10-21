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

package com.airbus.esar.fixture.shareable;

import javacard.framework.AID;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.Shareable;

/** Applet overrides getShareableInterfaceObject but does not implement MultiSelectable -- should warn. */
public final class ShareableAppletNotMultiSelectable extends Applet {

    private ShareableAppletNotMultiSelectable() {
    }

    public static void install(final byte[] bArray, final short bOffset, final byte bLength) {
        final ShareableAppletNotMultiSelectable a = new ShareableAppletNotMultiSelectable();
        a.register();
    }

    @Override
    public void process(final APDU apdu) {
    }

    @Override
    public Shareable getShareableInterfaceObject(final AID clientAID, final byte parameter) {
        return null;
    }
}
