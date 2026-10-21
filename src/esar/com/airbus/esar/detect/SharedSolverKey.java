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

package com.airbus.esar.detect;

/**
 * Stable id for a shared solver in SharedSolverRegistryIf. Type parameter is documentation; callers cast after get.
 *
 * @param <D> documented solver or handle type (e.g. a SootUp IFDS solver)
 */
public final class SharedSolverKey<D> {

    private final String mId;

    private SharedSolverKey(final String id) {
        mId = id;
    }

    /**
     * @param <D> documented type at call sites
     * @param id unique key among shared solvers for one analysis run
     * @return key instance
     */
    public static <D> SharedSolverKey<D> of(final String id) {
        return new SharedSolverKey<>(id);
    }

    public String id() {
        return mId;
    }
}
