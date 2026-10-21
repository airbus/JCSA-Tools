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

import java.util.List;
import java.util.Optional;

import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.Position;
import sootup.java.core.JavaSootMethod;

/**
 * Reads usable source line/column data from Jimple statements.
 */
public final class StmtSourceLocations {

    private StmtSourceLocations() {
    }

    /**
     * @param stmt Jimple statement whose position info is read
     * @return physical region when the statement carries usable line/column data
     */
    public static Optional<SourceRegion> regionForStmt(final Stmt stmt) {
        final StmtPositionInfo info = stmt.getPositionInfo();
        final Position pos = info.getStmtPosition();
        if (pos instanceof NoPositionInformation) {
            return Optional.empty();
        }
        final int firstLine = pos.getFirstLine();
        if (firstLine <= 0) {
            return Optional.empty();
        }
        return Optional.of(new SourceRegion(
                firstLine,
                pos.getLastLine(),
                pos.getFirstCol(),
                pos.getLastCol()));
    }

    /**
     * @param stmt statement to inspect
     * @return true when {@link #regionForStmt(Stmt)} would return a region
     */
    public static boolean hasUsableSourceLine(final Stmt stmt) {
        return regionForStmt(stmt).isPresent();
    }

    /**
     * @param method method whose body is scanned in statement order
     * @return physical region from the first statement with usable source line data
     */
    public static Optional<SourceRegion> regionForMethod(final JavaSootMethod method) {
        if (!method.hasBody()) {
            return Optional.empty();
        }
        return regionForBody(method.getBody());
    }

    /**
     * @param body method body scanned in statement order
     * @return physical region from the first statement with usable source line data
     */
    public static Optional<SourceRegion> regionForBody(final Body body) {
        for (final Stmt stmt : body.getStmts()) {
            final Optional<SourceRegion> region = regionForStmt(stmt);
            if (region.isPresent()) {
                return region;
            }
        }
        return Optional.empty();
    }

    /**
     * Catch handlers begin with {@code $x := @caughtexception}, which has no source position. This returns the first
     * later statement in the same method body that does.
     *
     * @param body enclosing method body
     * @param caughtExceptionEntry {@code JIdentityStmt} binding {@code @caughtexception}
     * @return positioned anchor for reporting, or the entry stmt when none exists
     */
    public static Stmt reportAnchorAfterCaughtException(final Body body, final Stmt caughtExceptionEntry) {
        return firstWithSourceLineAfter(body, caughtExceptionEntry).orElse(caughtExceptionEntry);
    }

    /**
     * @param body enclosing method body
     * @param start statement after which to search (exclusive)
     * @return first later statement in body order with usable source line data
     */
    public static Optional<Stmt> firstWithSourceLineAfter(final Body body, final Stmt start) {
        final List<Stmt> stmts = body.getStmts();
        final int startIndex = stmts.indexOf(start);
        if (startIndex < 0) {
            return Optional.empty();
        }
        for (int i = startIndex + 1; i < stmts.size(); i++) {
            final Stmt candidate = stmts.get(i);
            if (hasUsableSourceLine(candidate)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }
}
