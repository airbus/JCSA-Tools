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

package com.airbus.esar.internal.engine;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.airbus.esar.detect.SharedSolverKey;
import com.airbus.esar.detect.SharedSolverRegistryIf;

final class SharedSolverRegistry implements SharedSolverRegistryIf {

    private final Map<String, Object> mEntries = new ConcurrentHashMap<>();

    void clear() {
        mEntries.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <D> D lazyRegister(final SharedSolverKey<D> key, final Supplier<D> createIfAbsent) {
        D solver = (D) mEntries.get(key.id());
        if (solver == null) {
            solver = createIfAbsent.get();
            mEntries.put(key.id(), solver);
        }
        return solver;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D> Optional<D> get(final SharedSolverKey<D> key) {
        return Optional.ofNullable((D) mEntries.get(key.id()));
    }
}
