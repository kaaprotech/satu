/*
 * Copyright 2014 Kaaprotech Ltd.
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

package com.kaaprotech.satu.runtime.java;

/**
 * Base class for all delta builders
 */
public abstract class AbstractDeltaBuilder<D extends Delta> implements DeltaBuilder<D> {

    private final DeltaType initialDeltaType_;

    protected DeltaType deltaType_;

    public AbstractDeltaBuilder(DeltaType deltaType) {
        initialDeltaType_ = deltaType;
        deltaType_ = deltaType;
    }

    @Override
    public DeltaType getDeltaType() {
        return deltaType_;
    }

    @Override
    public AbstractDeltaBuilder<D> setDeltaType(DeltaType deltaType) {
        deltaType_ = deltaType;
        return this;
    }

    @Override
    public DeltaType getInitialDeltaType() {
        return initialDeltaType_;
    }

    @Override
    public AbstractDeltaBuilder<D> resetDeltaType() {
        deltaType_ = initialDeltaType_ == DeltaType.DELETE ? DeltaType.UPDATE : initialDeltaType_;
        return this;
    }

    @Override
    public abstract AbstractDeltaBuilder<D> addDelta(D delta);
}
