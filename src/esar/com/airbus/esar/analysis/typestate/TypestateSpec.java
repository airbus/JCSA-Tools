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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Declarative spec for a typestate rule. Describes a DFA over a caller-chosen state type, driven by invoke patterns.
 *
 * A statement is a "trigger" if it matches the pattern of any transition. Non-trigger invokes are handled by
 * callee summaries; everything else passes through unchanged.
 *
 * Two flavours:
 *   - relaxed (of) – unknown sequences keep the current state and are only reported at CFG tails;
 *   - strict (ofStrict) – unknown sequences move to the error state and are reported at the offending call.
 *
 * Message contract: mismatch message (strict only) gets {0} = state, {1} = callee fqn; tail message gets
 * {0} = state.
 *
 * @param <S> state type, usually an enum
 */
public final class TypestateSpec<S> {
    private final String mRuleId;
    // null for relaxed specs
    private final String mMismatchMessageId;
    private final String mTailMessageId;
    private final S mInitialState;
    // null for relaxed specs
    private final S mErrorState;
    private final Set<S> mAcceptingAtTail;
    private final List<TypestateTransition<S>> mTransitions;
    private final MismatchPolicy mMismatchPolicy;

    private TypestateSpec(final String ruleId,
                          final String tailMessageId,
                          final S initialState,
                          final Set<S> acceptingAtTail,
                          final List<TypestateTransition<S>> transitions) {
        mRuleId = ruleId;
        mMismatchMessageId = null;
        mTailMessageId = tailMessageId;
        mInitialState = initialState;
        mErrorState = null;
        mAcceptingAtTail = acceptingAtTail;
        mTransitions = transitions;
        mMismatchPolicy = MismatchPolicy.KEEP_STATE;
    }

    private TypestateSpec(final String ruleId,
                          final String mismatchMessageId,
                          final String tailMessageId,
                          final S initialState,
                          final S errorState,
                          final Set<S> acceptingAtTail,
                          final List<TypestateTransition<S>> transitions) {
        mRuleId = ruleId;
        mMismatchMessageId = mismatchMessageId;
        mTailMessageId = tailMessageId;
        mInitialState = initialState;
        mErrorState = errorState;
        mAcceptingAtTail = acceptingAtTail;
        mTransitions = transitions;
        mMismatchPolicy = MismatchPolicy.GO_TO_ERROR;
    }

    public static <S> TypestateSpec<S> of(final String ruleId,
                                          final String tailMessageId,
                                          final S initialState,
                                          final Set<S> acceptingAtTail,
                                          final List<TypestateTransition<S>> transitions) {
        Objects.requireNonNull(ruleId, "ruleId");
        Objects.requireNonNull(tailMessageId, "tailMessageId");
        Objects.requireNonNull(initialState, "initialState");
        Objects.requireNonNull(acceptingAtTail, "acceptingAtTail");
        Objects.requireNonNull(transitions, "transitions");
        return new TypestateSpec<>(
            ruleId,
            tailMessageId,
            initialState,
            Set.copyOf(acceptingAtTail),
            List.copyOf(transitions)
        );
    }

    @SafeVarargs
    public static <S> TypestateSpec<S> of(final String ruleId,
                                          final String tailMessageId,
                                          final S initialState,
                                          final Set<S> acceptingAtTail,
                                          final TypestateTransition<S>... transitions) {
        return of(
            ruleId,
            tailMessageId,
            initialState,
            acceptingAtTail,
            List.copyOf(Arrays.asList(transitions))
        );
    }

    public static <S> TypestateSpec<S> ofStrict(final String ruleId,
                                                final String mismatchMessageId,
                                                final String tailMessageId,
                                                final S initialState,
                                                final S errorState,
                                                final Set<S> acceptingAtTail,
                                                final List<TypestateTransition<S>> transitions) {
        Objects.requireNonNull(ruleId, "ruleId");
        Objects.requireNonNull(mismatchMessageId, "mismatchMessageId");
        Objects.requireNonNull(tailMessageId, "tailMessageId");
        Objects.requireNonNull(initialState, "initialState");
        Objects.requireNonNull(errorState, "errorState");
        Objects.requireNonNull(acceptingAtTail, "acceptingAtTail");
        Objects.requireNonNull(transitions, "transitions");
        return new TypestateSpec<>(
            ruleId,
            mismatchMessageId,
            tailMessageId,
            initialState,
            errorState,
            Set.copyOf(acceptingAtTail),
            List.copyOf(transitions)
        );
    }

    @SafeVarargs
    public static <S> TypestateSpec<S> ofStrict(final String ruleId,
                                                final String mismatchMessageId,
                                                final String tailMessageId,
                                                final S initialState,
                                                final S errorState,
                                                final Set<S> acceptingAtTail,
                                                final TypestateTransition<S>... transitions) {
        return ofStrict(
            ruleId,
            mismatchMessageId,
            tailMessageId,
            initialState,
            errorState,
            acceptingAtTail,
            List.copyOf(Arrays.asList(transitions))
        );
    }

    public String getRuleId() {
        return mRuleId;
    }

    public String getMismatchMessageId() {
        return mMismatchMessageId;
    }

    public String getTailMessageId() {
        return mTailMessageId;
    }

    public S getInitialState() {
        return mInitialState;
    }

    public S getErrorState() {
        return mErrorState;
    }

    public Set<S> getAcceptingAtTail() {
        return mAcceptingAtTail;
    }

    public List<TypestateTransition<S>> getTransitions() {
        return mTransitions;
    }

    public MismatchPolicy getMismatchPolicy() {
        return mMismatchPolicy;
    }

    public boolean hasApplicableTransition(final sootup.core.jimple.common.stmt.Stmt stmt, final S state) {
        for (final TypestateTransition<S> t : mTransitions) {
            if (!Objects.equals(t.getFrom(), state)) {
                continue;
            }
            if (t.getPattern().matches(stmt)) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesAnyTrigger(final sootup.core.jimple.common.stmt.Stmt stmt) {
        for (final TypestateTransition<S> t : mTransitions) {
            if (t.getPattern().matches(stmt)) {
                return true;
            }
        }
        return false;
    }
}
