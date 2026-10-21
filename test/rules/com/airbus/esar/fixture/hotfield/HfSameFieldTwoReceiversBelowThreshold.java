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

package com.airbus.esar.fixture.hotfield;

/**
 * Same field on {@code this} and {@code other}: each receiver stays below the
 * frequent-access threshold (5), but merging receivers would reach 5 on a path.
 */
public final class HfSameFieldTwoReceiversBelowThreshold {
    private static volatile int sSink;
    private int mSize;

    public void copy(final HfSameFieldTwoReceiversBelowThreshold other) {
        if (this.mSize >= other.mSize) {
            sSink = this.mSize + other.mSize + this.mSize;
        } else {
            sSink = other.mSize + this.mSize + other.mSize;
        }
    }
}
