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

package com.airbus.esar.analysis.typestate;

import java.util.Objects;

import com.airbus.esar.analysis.callflow.InvokeSignaturePattern;

/**
 * One edge of a typestate DFA: in state "from", a statement matching "pattern" moves us to "to".
 * Several edges may fire for the same statement; all their targets enter the resulting state set.
 *
 * @param <S> state type
 */
public final class TypestateTransition<S> {
    private final S mFrom;
    private final InvokeSignaturePattern mPattern;
    private final S mTo;

    private TypestateTransition(final S from, final InvokeSignaturePattern pattern, final S to) {
        mFrom = Objects.requireNonNull(from, "from");
        mPattern = Objects.requireNonNull(pattern, "pattern");
        mTo = Objects.requireNonNull(to, "to");
    }

    public static <S> TypestateTransition<S> of(final S from, final InvokeSignaturePattern pattern, final S to) {
        return new TypestateTransition<>(from, pattern, to);
    }

    public S getFrom() {
        return mFrom;
    }

    public InvokeSignaturePattern getPattern() {
        return mPattern;
    }

    public S getTo() {
        return mTo;
    }
}
