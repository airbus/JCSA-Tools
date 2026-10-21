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

package com.airbus.esar.analysis.signext;

/**
* Abstract pollution for a Jimple value or local.
*
* Sign-extension bits use {@link SignExtensionPollution#BYTE_WIDEN} and {@link SignExtensionPollution#SHORT_WIDEN}.
* {@link IntTempKind} classifies {@code int} temps for {@code (byte)} casts; it does not affect sink reporting.
*
* @param signExtPollution sign-extension bitmask
* @param intTempKind optional {@code int} temp classification when sign-ext bits are clear
* @param typedNarrowLoad {@code true} when a narrow local holds a same-type field/array/invoke load
*/
public record SignExtensionValueInfo(int signExtPollution, IntTempKind intTempKind, boolean typedNarrowLoad) {

    public static final SignExtensionValueInfo BOTTOM = new SignExtensionValueInfo(0, IntTempKind.BOTTOM, false);

    public static SignExtensionValueInfo ofSignExt(final int signExtPollution) {
        return signExtPollution == 0 ? BOTTOM : new SignExtensionValueInfo(signExtPollution, IntTempKind.BOTTOM, false);
    }

    public static SignExtensionValueInfo ofIntTemp(final IntTempKind kind) {
        return kind == null || kind == IntTempKind.BOTTOM ? BOTTOM : new SignExtensionValueInfo(0, kind, false);
    }

    public static SignExtensionValueInfo ofTypedNarrowLoad() {
        return new SignExtensionValueInfo(0, IntTempKind.BOTTOM, true);
    }

    public int pollution() {
        return signExtPollution;
    }

    public boolean affectsDestination(final int significantMask) {
        return (signExtPollution & significantMask) != 0;
    }

    public static SignExtensionValueInfo join(final SignExtensionValueInfo existing,
                                              final SignExtensionValueInfo proposed) {
        if (existing == null) {
            return proposed == null ? BOTTOM : proposed;
        }
        if (proposed == null) {
            return existing;
        }
        final int mergedSignExt = existing.signExtPollution | proposed.signExtPollution;
        if (mergedSignExt != 0) {
            return ofSignExt(mergedSignExt);
        }
        final IntTempKind mergedKind = IntTempKind.join(existing.intTempKind, proposed.intTempKind);
        final boolean mergedTypedNarrowLoad = existing.typedNarrowLoad || proposed.typedNarrowLoad;
        if (mergedKind == IntTempKind.BOTTOM && !mergedTypedNarrowLoad) {
            return BOTTOM;
        }
        if (mergedTypedNarrowLoad && mergedKind == IntTempKind.BOTTOM) {
            return ofTypedNarrowLoad();
        }
        return ofIntTemp(mergedKind);
    }
}
