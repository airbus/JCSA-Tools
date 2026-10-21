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

import java.util.Optional;

import sootup.core.jimple.common.stmt.Stmt;
import sootup.java.core.JavaSootMethod;
import com.airbus.esar.detect.SourceRegion;
import com.airbus.esar.detect.StmtSourceLocations;

final class FindingLocations {

    private FindingLocations() {
    }

    /**
     * @param stmt Jimple statement whose position info is read
     * @return physical region when the statement carries usable line/column data
     */
    static Optional<SourceRegion> regionForStmt(final Stmt stmt) {
        return StmtSourceLocations.regionForStmt(stmt);
    }

    /**
     * @param method method whose body is scanned in statement order
     * @return physical region from the first statement with usable source line data
     */
    static Optional<SourceRegion> regionForMethod(final JavaSootMethod method) {
        return StmtSourceLocations.regionForMethod(method);
    }
}
