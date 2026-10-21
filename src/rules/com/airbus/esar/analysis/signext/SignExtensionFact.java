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

import java.util.HashMap;
import java.util.Map;

import sootup.core.jimple.basic.Local;

/** Per-local sign-extension pollution and optional int-temp classification. */
public final class SignExtensionFact {

    public static final SignExtensionFact EMPTY = new SignExtensionFact(Map.of());

    private final Map<Local, SignExtensionValueInfo> mLocals;

    private SignExtensionFact(final Map<Local, SignExtensionValueInfo> locals) {
        mLocals = locals;
    }

    Map<Local, SignExtensionValueInfo> mLocals() {
        return mLocals;
    }

    public boolean isTracked(final Local local) {
        return mLocals.containsKey(local);
    }

    public int pollution(final Local local) {
        return localInfo(local).signExtPollution();
    }

    public SignExtensionValueInfo localInfo(final Local local) {
        return mLocals.getOrDefault(local, SignExtensionValueInfo.BOTTOM);
    }

    public IntTempKind intTempKind(final Local local) {
        return localInfo(local).intTempKind();
    }

    public boolean reWidenInt(final Local local) {
        return intTempKind(local) == IntTempKind.RE_WIDEN_UNSAFE;
    }

    public boolean narrowInt(final Local local) {
        return intTempKind(local) == IntTempKind.NARROW;
    }

    public boolean unsafeByteCastInt(final Local local) {
        return intTempKind(local) == IntTempKind.MOD_UNSAFE;
    }

    public boolean typedNarrowLoad(final Local local) {
        return localInfo(local).typedNarrowLoad();
    }

    public SignExtensionFact withLocalAssign(final Local local, final SignExtensionValueInfo info) {
        final SignExtensionValueInfo stored = info.signExtPollution() != 0
                ? SignExtensionValueInfo.ofSignExt(info.signExtPollution())
                : info;
        final SignExtensionValueInfo prev = mLocals.get(local);
        if (prev != null && prev.equals(stored)) {
            return this;
        }
        final Map<Local, SignExtensionValueInfo> next = new HashMap<>(mLocals);
        next.put(local, stored);
        return fact(next);
    }

    public static SignExtensionFact join(final SignExtensionFact existing, final SignExtensionFact proposed) {
        if (existing == null) {
            return proposed;
        }
        if (proposed == null) {
            return existing;
        }
        boolean changed = false;
        final Map<Local, SignExtensionValueInfo> out = new HashMap<>(existing.mLocals());
        for (final Map.Entry<Local, SignExtensionValueInfo> e : proposed.mLocals().entrySet()) {
            final Local local = e.getKey();
            final SignExtensionValueInfo proposedInfo = e.getValue();
            final SignExtensionValueInfo existingInfo = out.getOrDefault(local, SignExtensionValueInfo.BOTTOM);
            final SignExtensionValueInfo merged = SignExtensionValueInfo.join(existingInfo, proposedInfo);
            if (!merged.equals(existingInfo)) {
                out.put(local, merged);
                changed = true;
            }
        }
        if (!changed) {
            return existing;
        }
        return fact(out);
    }

    public static SignExtensionFact joinIfImproves(final SignExtensionFact existing,
                                                   final SignExtensionFact proposed) {
        if (existing == null) {
            return proposed;
        }
        final SignExtensionFact merged = join(existing, proposed);
        if (merged == existing) {
            return null;
        }
        return merged;
    }

    private static SignExtensionFact fact(final Map<Local, SignExtensionValueInfo> locals) {
        if (locals.isEmpty()) {
            return EMPTY;
        }
        return new SignExtensionFact(Map.copyOf(locals));
    }
}
