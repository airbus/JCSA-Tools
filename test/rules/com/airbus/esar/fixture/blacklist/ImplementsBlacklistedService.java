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

package com.airbus.esar.fixture.blacklist;

import javacard.framework.APDU;
import javacard.framework.service.Service;

/** Implements a blacklisted interface. */
public final class ImplementsBlacklistedService implements Service {

    @Override
    public boolean processDataIn(final APDU apdu) {
        return false;
    }

    @Override
    public boolean processCommand(final APDU apdu) {
        return false;
    }

    @Override
    public boolean processDataOut(final APDU apdu) {
        return false;
    }
}
