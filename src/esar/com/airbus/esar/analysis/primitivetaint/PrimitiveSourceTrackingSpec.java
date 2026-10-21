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

package com.airbus.esar.analysis.primitivetaint;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.constant.FloatConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.signatures.FieldSignature;

/**
 * Declarative primitive sources for IFDS (static fields and Jimple constants). Build with of(Field.of / *Literal.of).
 *
 * @param <L> label type on TrackedPrimitiveFact
 */
public final class PrimitiveSourceTrackingSpec<L> {

    /**
     * One static field or constant entry for of(...).
     *
     * @param <L> label type
     */
    public sealed interface Entry<L> permits Field, IntLiteral, LongLiteral, FloatLiteral, DoubleLiteral {
    }

    /**
     * Static field → tag.
     *
     * @param <L> label type
     * @param declaringClassFqn declaring type of the static field
     * @param fieldName static field simple name
     * @param tag label propagated for that field
     */
    public record Field<L>(String declaringClassFqn, String fieldName, L tag) implements Entry<L> {

        public static <L> Field<L> of(final String declaringClassFqn, final String fieldName, final L tag) {
            return new Field<>(declaringClassFqn, fieldName, tag);
        }
    }

    /**
     * Inlined int-family literal → tag.
     *
     * @param <L> label type
     * @param value numeric int constant (byte, short, char, int in Jimple)
     * @param tag label propagated for that literal
     */
    public record IntLiteral<L>(int value, L tag) implements Entry<L> {

        public static <L> IntLiteral<L> of(final int value, final L tag) {
            return new IntLiteral<>(value, tag);
        }
    }

    /**
     * Inlined long literal → tag.
     *
     * @param <L> label type
     * @param value numeric long constant
     * @param tag label propagated for that literal
     */
    public record LongLiteral<L>(long value, L tag) implements Entry<L> {

        public static <L> LongLiteral<L> of(final long value, final L tag) {
            return new LongLiteral<>(value, tag);
        }
    }

    /**
     * Inlined float literal → tag.
     *
     * @param <L> label type
     * @param value numeric float constant
     * @param tag label propagated for that literal
     */
    public record FloatLiteral<L>(float value, L tag) implements Entry<L> {

        public static <L> FloatLiteral<L> of(final float value, final L tag) {
            return new FloatLiteral<>(value, tag);
        }
    }

    /**
     * Inlined double literal → tag.
     *
     * @param <L> label type
     * @param value numeric double constant
     * @param tag label propagated for that literal
     */
    public record DoubleLiteral<L>(double value, L tag) implements Entry<L> {

        public static <L> DoubleLiteral<L> of(final double value, final L tag) {
            return new DoubleLiteral<>(value, tag);
        }
    }

    private final Map<String, Map<String, L>> mFieldsByClass;
    private final Map<Integer, L> mIntLiterals;
    private final Map<Long, L> mLongLiterals;
    private final Map<Integer, L> mFloatLiterals;
    private final Map<Long, L> mDoubleLiterals;

    private PrimitiveSourceTrackingSpec(final Map<String, Map<String, L>> fieldsByClass,
            final Map<Integer, L> intLiterals,
            final Map<Long, L> longLiterals,
            final Map<Integer, L> floatLiterals,
            final Map<Long, L> doubleLiterals) {
        mFieldsByClass = fieldsByClass;
        mIntLiterals = intLiterals;
        mLongLiterals = longLiterals;
        mFloatLiterals = floatLiterals;
        mDoubleLiterals = doubleLiterals;
    }

    /**
     * @param <L> label type
     * @param entries field and literal sources
     * @return frozen spec with the same duplicate rules as the former builder (each literal key must not map to two
     *     tags)
     */
    @SafeVarargs
    public static <L> PrimitiveSourceTrackingSpec<L> of(final Entry<L>... entries) {
        final Map<String, Map<String, L>> fieldsByClass = new HashMap<>();
        final Map<Integer, L> intLiterals = new HashMap<>();
        final Map<Long, L> longLiterals = new HashMap<>();
        final Map<Integer, L> floatLiterals = new HashMap<>();
        final Map<Long, L> doubleLiterals = new HashMap<>();
        for (final Entry<L> entry : entries) {
            switch (entry) {
                case final Field<L> f -> fieldsByClass
                        .computeIfAbsent(f.declaringClassFqn(), k -> new HashMap<>())
                        .put(f.fieldName(), f.tag());
                case final IntLiteral<L> lit -> putLiteral(intLiterals, lit.value(), lit.tag());
                case final LongLiteral<L> lit -> putLiteral(longLiterals, lit.value(), lit.tag());
                case final FloatLiteral<L> lit ->
                        putLiteral(floatLiterals, Float.floatToIntBits(lit.value()), lit.tag());
                case final DoubleLiteral<L> lit ->
                        putLiteral(doubleLiterals, Double.doubleToLongBits(lit.value()), lit.tag());
            }
        }
        return new PrimitiveSourceTrackingSpec<>(
                Map.copyOf(deepCopyFields(fieldsByClass)),
                Map.copyOf(intLiterals),
                Map.copyOf(longLiterals),
                Map.copyOf(floatLiterals),
                Map.copyOf(doubleLiterals));
    }

    /**
     * @param <L> label type
     * @param a first spec
     * @param b second spec
     * @return merged spec; duplicate class+field or duplicate literal key throws
     */
    public static <L> PrimitiveSourceTrackingSpec<L> merge(
            final PrimitiveSourceTrackingSpec<L> a,
            final PrimitiveSourceTrackingSpec<L> b) {
        final Map<String, Map<String, L>> fields = new HashMap<>();
        copyFields(fields, a.mFieldsByClass);
        copyFieldsMerge(fields, b.mFieldsByClass);
        final Map<Integer, L> intLiterals = new HashMap<>(a.mIntLiterals);
        mergeLiterals(intLiterals, b.mIntLiterals, "int");
        final Map<Long, L> longLiterals = new HashMap<>(a.mLongLiterals);
        mergeLiterals(longLiterals, b.mLongLiterals, "long");
        final Map<Integer, L> floatLiterals = new HashMap<>(a.mFloatLiterals);
        mergeLiterals(floatLiterals, b.mFloatLiterals, "float");
        final Map<Long, L> doubleLiterals = new HashMap<>(a.mDoubleLiterals);
        mergeLiterals(doubleLiterals, b.mDoubleLiterals, "double");
        return new PrimitiveSourceTrackingSpec<>(
                Map.copyOf(deepCopyFields(fields)),
                Map.copyOf(intLiterals),
                Map.copyOf(longLiterals),
                Map.copyOf(floatLiterals),
                Map.copyOf(doubleLiterals));
    }

    public Optional<L> tagForField(final JStaticFieldRef ref) {
        final FieldSignature fs = ref.getFieldSignature();
        final String cls = fs.getDeclClassType().getFullyQualifiedName();
        final Map<String, L> inner = mFieldsByClass.get(cls);
        if (inner == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(inner.get(fs.getName()));
    }

    /**
     * @param value Jimple constant
     * @return tag when the constant is a configured source
     */
    public Optional<L> tagForConstant(final Value value) {
        if (value instanceof IntConstant c) {
            return Optional.ofNullable(mIntLiterals.get(c.getValue()));
        }
        if (value instanceof LongConstant c) {
            return Optional.ofNullable(mLongLiterals.get(c.getValue()));
        }
        if (value instanceof FloatConstant c) {
            return Optional.ofNullable(mFloatLiterals.get(Float.floatToIntBits(c.getValue())));
        }
        if (value instanceof DoubleConstant c) {
            return Optional.ofNullable(mDoubleLiterals.get(Double.doubleToLongBits(c.getValue())));
        }
        return Optional.empty();
    }

    private static <K, L> void putLiteral(final Map<K, L> literals, final K key, final L tag) {
        final L old = literals.putIfAbsent(key, tag);
        if (old != null && !Objects.equals(old, tag)) {
            throw new IllegalArgumentException("Literal " + key + " already mapped to " + old);
        }
    }

    private static <K, L> void mergeLiterals(final Map<K, L> to, final Map<K, L> from, final String kind) {
        for (final Map.Entry<K, L> e : from.entrySet()) {
            final L old = to.putIfAbsent(e.getKey(), e.getValue());
            if (old != null && !Objects.equals(old, e.getValue())) {
                throw new IllegalArgumentException(
                        kind + " literal " + e.getKey() + " mapped to both " + old + " and " + e.getValue());
            }
        }
    }

    private static <L> void copyFields(final Map<String, Map<String, L>> to,
            final Map<String, Map<String, L>> from) {
        for (final Map.Entry<String, Map<String, L>> e : from.entrySet()) {
            to.put(e.getKey(), new HashMap<>(e.getValue()));
        }
    }

    private static <L> void copyFieldsMerge(final Map<String, Map<String, L>> to,
            final Map<String, Map<String, L>> from) {
        for (final Map.Entry<String, Map<String, L>> ce : from.entrySet()) {
            final Map<String, L> inner = to.computeIfAbsent(ce.getKey(), k -> new HashMap<>());
            for (final Map.Entry<String, L> fe : ce.getValue().entrySet()) {
                final L old = inner.putIfAbsent(fe.getKey(), fe.getValue());
                if (old != null && !Objects.equals(old, fe.getValue())) {
                    throw new IllegalArgumentException(
                            "Field " + ce.getKey() + "." + fe.getKey() + " mapped to both " + old + " and "
                                    + fe.getValue());
                }
            }
        }
    }

    private static <L> Map<String, Map<String, L>> deepCopyFields(final Map<String, Map<String, L>> in) {
        final Map<String, Map<String, L>> out = new HashMap<>();
        for (final Map.Entry<String, Map<String, L>> e : in.entrySet()) {
            out.put(e.getKey(), Map.copyOf(e.getValue()));
        }
        return out;
    }
}
