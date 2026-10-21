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

import sootup.core.jimple.common.stmt.Stmt;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;

/**
 * Thread-safe sink for findings. Use RuleMessageRef (not raw strings); the engine resolves text.
 * Rare shapes: report(EsarFinding) or EsarFinding factories.
 */
public interface BugReporterIf {

    /**
     * @param ruleId stable rule identifier (non-null)
     * @param level SARIF-aligned severity
     * @param message reference to a globally unique key and format arguments
     * @param clazz class for logical location
     */
    void report(String ruleId, SarifResultLevel level, RuleMessageRef message, JavaSootClass clazz);

    /**
     * @param ruleId stable rule identifier (non-null)
     * @param level SARIF-aligned severity
     * @param message reference to a globally unique key and format arguments
     * @param method method for logical location (signature stored on the finding)
     */
    void report(String ruleId, SarifResultLevel level, RuleMessageRef message, JavaSootMethod method);

    /**
     * @param ruleId stable rule identifier (non-null)
     * @param level SARIF-aligned severity
     * @param message reference to a globally unique key and format arguments
     * @param method declaring method for logical location
     * @param stmt statement for source region when line info exists
     */
    void report(String ruleId, SarifResultLevel level, RuleMessageRef message, JavaSootMethod method, Stmt stmt);

    /**
     * @param finding fully constructed finding (typically for uncommon shapes; prefer overloads when possible)
     */
    void report(EsarFinding finding);
}
