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

package com.airbus.esar.analysis.knownvalue;

import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import com.airbus.esar.detect.AnalysisContextIf;

public final class CompileTimeKnownInt {

    private CompileTimeKnownInt() {
    }

    public static boolean isCompileTimeKnown(final Value value,
                                             final Stmt atStmt,
                                             final Body body,
                                             final AnalysisContextIf ctx) {
        return CompileTimeKnownValue.isKnown(value, atStmt, body, ctx, KnownValueSpec.INT);
    }
}
