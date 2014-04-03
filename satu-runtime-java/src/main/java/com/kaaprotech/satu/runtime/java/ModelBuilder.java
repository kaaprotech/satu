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

public interface ModelBuilder<K, M extends Model<K, ? extends ModelBuilder<K, M, D>>, D extends ModelDelta<K, ? extends ModelBuilder<K, ?, D>, ? extends ModelDeltaBuilder<K, D>>> extends Identity<K> {

    @Override
    K getKey();

    ModelBuilder<K, M, D> applyDelta(D delta);

    D reconcile();

    D reconcile(DeltaType deltaType, M ref);

    D toDelta(DeltaType deltaType);

    M build();

    M buildEmpty();

    M getRef();

    DeltaType getLastDeltaType();

    ModelBuilder<K, M, D> setLastDeltaType(DeltaType lastDeltaType);
}
