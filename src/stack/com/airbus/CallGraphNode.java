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

package com.airbus;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class CallGraphNode {
    private final Method mMethodSignature;
    private final List<CallGraphNode> mChildren;

    public CallGraphNode(final Method methodSignature) {
        mMethodSignature = methodSignature;
        mChildren = new ArrayList<>();
    }

    public void addChild(final CallGraphNode child) {
        mChildren.add(child);
    }

    public Method getSignature() {
        return mMethodSignature;
    }

    public List<CallGraphNode> getChildren() {
        return mChildren;
    }

    public void traverse() {
        traverse(new ArrayList<>());
    }

    private void traverse(final List<Method> path) {
        path.add(mMethodSignature);

        if (mChildren.isEmpty()) {
            final StringJoiner sj = new StringJoiner(", ");
            for (Object obj : path) {
                sj.add(String.valueOf(obj));
            }

            System.out.println("Path: " + sj.toString());
        } else {
            for (final CallGraphNode child : mChildren) {
                child.traverse(new ArrayList<>(path));
            }
        }
    }
}
