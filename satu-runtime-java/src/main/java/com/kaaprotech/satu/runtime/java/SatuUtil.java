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

import org.apache.commons.lang.ObjectUtils;

import com.gs.collections.api.RichIterable;
import com.gs.collections.api.block.function.Function;
import com.gs.collections.api.block.function.Function2;
import com.gs.collections.api.block.procedure.Procedure;
import com.gs.collections.api.list.ImmutableList;
import com.gs.collections.api.map.ImmutableMap;
import com.gs.collections.api.map.MapIterable;
import com.gs.collections.api.map.MutableMap;
import com.gs.collections.api.set.MutableSet;
import com.gs.collections.api.set.SetIterable;
import com.gs.collections.api.tuple.Pair;
import com.gs.collections.impl.block.factory.Predicates;
import com.gs.collections.impl.tuple.Tuples;

/**
 * Utility class used by the generated code
 */
@SuppressWarnings("serial")
public final class SatuUtil {

    public static <K, M extends Model<?, B>, B extends ModelBuilder<?, M, ?>> MutableMap<K, B> toKeyModelBuilderMap(final ImmutableMap<K, M> models) {
        return models.collect(new Function2<K, M, Pair<K, B>>() {
            @Override
            public Pair<K, B> value(final K key, final M mode2) {
                return Tuples.pair(key, mode2.toBuilder());
            }
        }).toMap();
    }

    public static <K, M extends Model<?, B>, B extends ModelBuilder<?, M, ?>> ImmutableMap<K, M> toKeyModelMap(final MutableMap<K, B> builders) {
        return builders.collect(new Function2<K, B, Pair<K, M>>() {
            @Override
            public Pair<K, M> value(final K key, final B bui2der) {
                return Tuples.pair(key, bui2der.build());
            }
        }).toImmutable();
    }

    public static <K, V> MutableMap<K, KeyValuePairDelta.Builder<K, V>> toKeyValuePairDeltaBuilderMap(final ImmutableList<KeyValuePairDelta<K, V>> kvPairs) {
        return kvPairs.toMap(new Function<KeyValuePairDelta<K, V>, K>() {
            @Override
            public K valueOf(final KeyValuePairDelta<K, V> kvPair) {
                return kvPair.getKey();
            }
        }, new Function<KeyValuePairDelta<K, V>, KeyValuePairDelta.Builder<K, V>>() {
            @Override
            public KeyValuePairDelta.Builder<K, V> valueOf(final KeyValuePairDelta<K, V> kvPair) {
                return kvPair.toDeltaBuilder();
            }
        });
    }

    public static <K> MutableMap<K, KeyDelta.Builder<K>> toKeyDeltaBuilderMap(final ImmutableList<KeyDelta<K>> keyDeltas) {
        return keyDeltas.toMap(new Function<KeyDelta<K>, K>() {
            @Override
            public K valueOf(final KeyDelta<K> keyDelta) {
                return keyDelta.getKey();
            }
        }, new Function<KeyDelta<K>, KeyDelta.Builder<K>>() {
            @Override
            public KeyDelta.Builder<K> valueOf(final KeyDelta<K> keyDelta) {
                return keyDelta.toDeltaBuilder();
            }
        });
    }

    public static <K, K2, D extends ModelDelta<K2, B, DB>, B extends ModelBuilder<K2, ?, D>, DB extends ModelDeltaBuilder<K2, D>> MutableMap<K, KeyModelDeltaPairDelta.Builder<K, K2, D, DB>> toKeyModelDeltaBuilderMap(
            final ImmutableList<KeyModelDeltaPairDelta<K, K2, D, DB>> deltas) {
        return deltas.toMap(new Function<KeyModelDeltaPairDelta<K, K2, D, DB>, K>() {
            @Override
            public K valueOf(final KeyModelDeltaPairDelta<K, K2, D, DB> delta) {
                return delta.getKey();
            }
        }, new Function<KeyModelDeltaPairDelta<K, K2, D, DB>, KeyModelDeltaPairDelta.Builder<K, K2, D, DB>>() {
            @Override
            public KeyModelDeltaPairDelta.Builder<K, K2, D, DB> valueOf(final KeyModelDeltaPairDelta<K, K2, D, DB> delta) {
                return delta.toDeltaBuilder();
            }
        });
    }

    public static <K, V> void applyKeyValuePairDeltas(final RichIterable<KeyValuePairDelta<K, V>> deltas, final MutableMap<K, V> pairs) {
        deltas.forEach(new Procedure<KeyValuePairDelta<K, V>>() {
            @Override
            public void value(final KeyValuePairDelta<K, V> delta) {
                switch (delta.getDeltaType()) {
                case ADD:
                case UPDATE:
                    V value = pairs.get(delta.getKey());
                    if (value != null) {
                        if (!ObjectUtils.equals(value, delta.getValue())) {
                            pairs.put(delta.getKey(), delta.getValue());
                        }
                    }
                    else {
                        pairs.put(delta.getKey(), delta.getValue());
                    }
                    break;

                case DELETE:
                    if (pairs.containsKey(delta.getKey())) {
                        pairs.removeKey(delta.getKey());
                    }
                    break;
                }
            }
        });
    }

    public static <K, K2, D extends ModelDelta<K2, MB, DB>, MB extends ModelBuilder<K2, ?, D>, DB extends ModelDeltaBuilder<K2, D>> void addKeyModelDeltaPairDelta(
            final MutableMap<K, KeyModelDeltaPairDelta.Builder<K, K2, D, DB>> deltas, final KeyModelDeltaPairDelta<K, K2, D, DB> newDelta) {
        KeyModelDeltaPairDelta.Builder<K, K2, D, DB> builder = deltas.get(newDelta.getKey());
        if (builder == null) {
            builder = newDelta.toDeltaBuilder();
            deltas.put(newDelta.getKey(), builder);
        }
        else {
            builder.addDelta(newDelta);
        }
    }

    public static <K, V> void addKeyValuePairDelta(final MutableMap<K, KeyValuePairDelta.Builder<K, V>> deltas, final KeyValuePairDelta<K, V> newDelta) {
        KeyValuePairDelta.Builder<K, V> builder = deltas.get(newDelta.getKey());
        if (builder == null) {
            builder = newDelta.toDeltaBuilder();
            deltas.put(builder.getKey(), builder);
        }
        else {
            builder.addDelta(newDelta);
        }
    }

    public static <K> void addKeyDelta(final MutableMap<K, KeyDelta.Builder<K>> deltas, final KeyDelta<K> newDelta) {
        KeyDelta.Builder<K> builder = deltas.get(newDelta.getKey());
        if (builder == null) {
            builder = newDelta.toDeltaBuilder();
            deltas.put(builder.getKey(), builder);
        }
        else {
            builder.addDelta(newDelta);
        }
    }

    public static <K, V1, V2> void reconcileMap(final MapIterable<K, V1> ref, final MapIterable<K, V2> other, final ReconcileMapNofity<K, V2> notify) {
        reconcileMap(ref, other, notify, new ReconcileMapEquals<V1, V2>() {
            @Override
            public boolean equals(V1 value1, V2 value2) {
                return ObjectUtils.equals(value1, value2);
            }
        });
    }

    public static <K, V1, V2> void reconcileMap(final MapIterable<K, V1> ref, final MapIterable<K, V2> other, final ReconcileMapNofity<K, V2> notify, final ReconcileMapEquals<V1, V2> equals) {
        ref.keysView().reject(Predicates.in(other.keysView())).forEach(new Procedure<K>() {
            @Override
            public void value(final K key) {
                notify.delete(key);
            }
        });

        other.keysView().reject(Predicates.in(ref.keysView())).forEach(new Procedure<K>() {
            @Override
            public void value(final K key) {
                notify.add(key, other.get(key));
            }
        });

        ref.keysView().select(Predicates.in(other.keysView())).forEach(new Procedure<K>() {
            @Override
            public void value(final K key) {
                V2 otherValue = other.get(key);
                if (!equals.equals(ref.get(key), otherValue)) {
                    notify.update(key, otherValue);
                }
            }
        });
    }

    public static <K, V> void reconcileKeyValuePairs(final MapIterable<K, V> refTags, final MapIterable<K, V> modTags, final DeltaAppender<KeyValuePairDelta<K, V>> appender) {
        reconcileMap(refTags, modTags, new ReconcileMapNofity<K, V>() {
            @Override
            public void add(final K key, final V value) {
                appender.append(new KeyValuePairDelta<K, V>(DeltaType.ADD, key, value));
            }

            @Override
            public void update(final K key, final V value) {
                appender.append(new KeyValuePairDelta<K, V>(DeltaType.UPDATE, key, value));
            }

            @Override
            public void delete(final K key) {
                appender.append(new KeyValuePairDelta<K, V>(DeltaType.DELETE, key, null));
            }
        });
    }

    public static <K> void applyKeyDeltas(final RichIterable<KeyDelta<K>> deltas, final MutableSet<K> entries) {
        deltas.forEach(new Procedure<KeyDelta<K>>() {
            @Override
            public void value(final KeyDelta<K> delta) {
                final K key = delta.getKey();
                switch (delta.getDeltaType()) {
                case ADD:
                case UPDATE:
                    if (!entries.contains(key)) {
                        entries.add(key);
                    }
                    break;

                case DELETE:
                    if (entries.contains(key)) {
                        entries.remove(key);
                    }
                    break;
                }
            }
        });
    }

    public static <K, K2, D extends ModelDelta<K2, B, DB>, DB extends ModelDeltaBuilder<K2, D>, B extends ModelBuilder<K2, ?, D>> void applyKeyModelPairDeltas(
            final RichIterable<KeyModelDeltaPairDelta<K, K2, D, DB>> deltas, final MutableMap<K, B> builders) {
        deltas.forEach(new Procedure<KeyModelDeltaPairDelta<K, K2, D, DB>>() {
            @Override
            public void value(final KeyModelDeltaPairDelta<K, K2, D, DB> delta) {
                B builder = builders.get(delta.getKey());
                switch (delta.getDeltaType()) {
                case ADD:
                case UPDATE:
                    if (builder == null || !builder.getKey().equals(delta.getValue().getKey())) {
                        builder = delta.getValue().toBuilder();
                        builders.put(delta.getKey(), builder);
                    }
                    else {
                        builder.applyDelta(delta.getValue());
                    }
                    break;

                case DELETE:
                    if (builder != null) {
                        builders.removeKey(delta.getKey());
                    }
                    break;
                }
            }
        });
    }

    public static <E> void reconcileSet(final SetIterable<E> ref, final SetIterable<E> other, final ReconcileSetNotify<E> notify) {
        reconcileSet(ref, other, false, notify);
    }

    public static <E> void reconcileSet(final SetIterable<E> ref, final SetIterable<E> other, final boolean skipIntersect, final ReconcileSetNotify<E> notify) {
        ref.difference(other).forEach(new Procedure<E>() {
            @Override
            public void value(final E element) {
                notify.delete(element);
            }
        });

        other.difference(ref).forEach(new Procedure<E>() {
            @Override
            public void value(final E element) {
                notify.add(element);
            }
        });

        if (!skipIntersect) {
            ref.intersect(other).forEach(new Procedure<E>() {
                @Override
                public void value(final E element) {
                    notify.intersect(element);
                }
            });
        }
    }

    public static <K> void reconcileKeys(final SetIterable<K> refValues, final SetIterable<K> modValues, final DeltaAppender<KeyDelta<K>> appender) {
        reconcileSet(refValues, modValues, true, new ReconcileSetNotify<K>() {
            @Override
            public void add(final K value) {
                appender.append(new KeyDelta<K>(DeltaType.ADD, value));
            }

            @Override
            public void delete(final K value) {
                appender.append(new KeyDelta<K>(DeltaType.DELETE, value));
            }

            @SuppressWarnings("unused")
            @Override
            public void intersect(final K value) {
                // Noop
            }
        });
    }

    public static <K> ImmutableList<KeyDelta<K>> buildKeyDelta(final RichIterable<? extends KeyDelta.Builder<K>> builders) {
        return builders.collect(new Function<KeyDelta.Builder<K>, KeyDelta<K>>() {
            @Override
            public KeyDelta<K> valueOf(final KeyDelta.Builder<K> builder) {
                return builder.buildDelta();
            }
        }).select(Predicates.notNull()).toList().toImmutable();
    }

    public static <K, V> ImmutableList<KeyValuePairDelta<K, V>> buildKeyValuePairDelta(RichIterable<KeyValuePairDelta.Builder<K, V>> builders) {
        return builders.collect(new Function<KeyValuePairDelta.Builder<K, V>, KeyValuePairDelta<K, V>>() {
            @Override
            public KeyValuePairDelta<K, V> valueOf(final KeyValuePairDelta.Builder<K, V> builder) {
                return builder.buildDelta();
            }
        }).select(Predicates.notNull()).toList().toImmutable();
    }

    public static <K, K2, D extends ModelDelta<K2, B, DB>, B extends ModelBuilder<K2, ?, D>, DB extends ModelDeltaBuilder<K2, D>> ImmutableList<KeyModelDeltaPairDelta<K, K2, D, DB>> buildKeyModelDeltaPairDelta(
            final RichIterable<KeyModelDeltaPairDelta.Builder<K, K2, D, DB>> builders) {
        return builders.collect(new Function<KeyModelDeltaPairDelta.Builder<K, K2, D, DB>, KeyModelDeltaPairDelta<K, K2, D, DB>>() {
            @Override
            public KeyModelDeltaPairDelta<K, K2, D, DB> valueOf(final KeyModelDeltaPairDelta.Builder<K, K2, D, DB> builder) {
                return builder.buildDelta();
            }
        }).select(Predicates.notNull()).toList().toImmutable();
    }

    public static <K, K2, M extends Model<K2, B>, B extends ModelBuilder<K2, M, D>, D extends ModelDelta<K2, B, DB>, DB extends ModelDeltaBuilder<K2, D>> void reconcileKeyModelBuilderPairs(
            final MapIterable<K, M> refModels, final MapIterable<K, B> builders, final DeltaAppender<KeyModelDeltaPairDelta<K, K2, D, DB>> deltaAppender) {
        reconcileSet(refModels.keysView().toSet(), builders.keysView().toSet(), new ReconcileSetNotify<K>() {
            @Override
            public void add(final K key) {
                final B builder = builders.get(key);
                final D delta = builder.toDelta(DeltaType.ADD);
                if (delta != null) {
                    deltaAppender.append(new KeyModelDeltaPairDelta<K, K2, D, DB>(DeltaType.ADD, key, delta));
                }
                else {
                    // Create a blank delta
                    final D d = builder.toDelta(DeltaType.DELETE).toDeltaBuilder().setDeltaType(DeltaType.ADD).buildDelta();
                    deltaAppender.append(new KeyModelDeltaPairDelta<K, K2, D, DB>(DeltaType.ADD, key, d));
                }
            }

            @Override
            public void intersect(final K key) {
                final M refModel = refModels.get(key);
                final B builder = builders.get(key);
                if (refModel.getKey().equals(builder.getKey())) {
                    final D delta = builder.reconcile(DeltaType.UPDATE, refModel);
                    if (delta != null) {
                        deltaAppender.append(new KeyModelDeltaPairDelta<K, K2, D, DB>(DeltaType.UPDATE, key, delta));
                    }
                }
                else {
                    final M emptyModel = builder.buildEmpty();
                    final D delta = builder.reconcile(DeltaType.ADD, emptyModel);
                    if (delta != null) {
                        deltaAppender.append(new KeyModelDeltaPairDelta<K, K2, D, DB>(DeltaType.UPDATE, key, delta));
                    }
                }
            }

            @Override
            public void delete(final K key) {
                final M refModel = refModels.get(key);
                final B emptyBuilder = refModel.toBuilderEmpty();
                final D delta = emptyBuilder.reconcile(DeltaType.DELETE, refModel);
                if (delta != null) {
                    deltaAppender.append(new KeyModelDeltaPairDelta<K, K2, D, DB>(DeltaType.DELETE, key, delta));
                }
            }
        });
    }

    public static <K, K2, M extends Model<K2, B>, B extends ModelBuilder<K2, M, D>, D extends ModelDelta<K2, B, DB>, DB extends ModelDeltaBuilder<K2, D>> void reconcileKeyModelPairs(
            final ImmutableMap<K, M> refModels, final ImmutableMap<K, M> models, final DeltaAppender<KeyModelDeltaPairDelta<K, K2, D, DB>> deltaAppender) {
        reconcileSet(refModels.keysView().toSet(), models.keysView().toSet(), new ReconcileSetNotify<K>() {
            @Override
            public void add(final K key) {
                final M model = models.get(key);
                final B builder = model.toBuilder();
                final M emptyModel = builder.buildEmpty();
                final D delta = builder.reconcile(DeltaType.ADD, emptyModel);
                if (delta != null) {
                    deltaAppender.append(new KeyModelDeltaPairDelta<K, K2, D, DB>(DeltaType.ADD, key, delta));
                }
            }

            @Override
            public void intersect(final K key) {
                final M refModel = refModels.get(key);
                final M model = models.get(key);
                if (refModel.getKey().equals(model.getKey())) {
                    final D delta = model.toBuilder().reconcile(DeltaType.UPDATE, refModel);
                    if (delta != null) {
                        deltaAppender.append(new KeyModelDeltaPairDelta<K, K2, D, DB>(DeltaType.UPDATE, key, delta));
                    }
                }
                else {
                    final B builder = model.toBuilder();
                    final M emptyModel = builder.buildEmpty();
                    final D delta = builder.reconcile(DeltaType.ADD, emptyModel);
                    if (delta != null) {
                        deltaAppender.append(new KeyModelDeltaPairDelta<K, K2, D, DB>(DeltaType.UPDATE, key, delta));
                    }
                }
            }

            @Override
            public void delete(final K key) {
                final M refModel = refModels.get(key);
                final B emptyBuilder = refModel.toBuilderEmpty();
                final D delta = emptyBuilder.reconcile(DeltaType.DELETE, refModel);
                if (delta != null) {
                    deltaAppender.append(new KeyModelDeltaPairDelta<K, K2, D, DB>(DeltaType.DELETE, key, delta));
                }
            }
        });
    }

    public static <K, K2, M extends Model<K2, B>, B extends ModelBuilder<K2, M, D>, D extends ModelDelta<K2, B, DB>, DB extends ModelDeltaBuilder<K2, D>> B applyModelDelta(final D delta,
            final B builder) {
        if (builder == null || !builder.getKey().equals(delta.getKey())) {
            return delta.toBuilder();
        }
        builder.applyDelta(delta);
        return builder;
    }

    public static <K, M extends Model<K, B>, B extends ModelBuilder<K, M, D>, D extends ModelDelta<K, B, DB>, DB extends ModelDeltaBuilder<K, D>> void reconcileModelBuilderField(final M refModel,
            final B builder, final DB deltaBuilder, final DeltaBuilderSetter<DB> setter) {
        if (refModel == null && builder == null) {
            setter.set(null);
            return;
        }

        final D delta;

        if (refModel == null && builder != null) {
            final M emptyModel = builder.buildEmpty();
            delta = builder.reconcile(DeltaType.ADD, emptyModel);
        }
        else if (refModel != null && builder != null) {
            if (refModel.getKey().equals(builder.getKey())) {
                delta = builder.reconcile(DeltaType.UPDATE, refModel);
            }
            else {
                final M emptyModel = builder.buildEmpty();
                delta = builder.reconcile(DeltaType.ADD, emptyModel);
            }
        }
        else {
            final B emptyBuilder = refModel.toBuilderEmpty();
            delta = emptyBuilder.reconcile(DeltaType.DELETE, refModel);
        }

        if (deltaBuilder != null && delta != null && deltaBuilder.getKey().equals(delta.getKey())) {
            deltaBuilder.addDelta(delta);
            setter.set(deltaBuilder);
        }
        else if (delta != null) {
            setter.set(delta.toDeltaBuilder());
        }
        else {
            setter.set(null);
        }
    }

    public static <K, M extends Model<K, B>, B extends ModelBuilder<K, M, D>, D extends ModelDelta<K, B, DB>, DB extends ModelDeltaBuilder<K, D>> void reconcileModelField(final M refModel,
            final M model, final DB deltaBuilder, final DeltaBuilderSetter<DB> setter) {
        if (refModel == null && model == null) {
            setter.set(null);
            return;
        }

        final D delta;

        if (refModel == null && model != null) {
            final B builder = model.toBuilder();
            final M emptyModel = builder.buildEmpty();
            delta = builder.reconcile(DeltaType.ADD, emptyModel);
        }
        else if (refModel != null && model != null) {
            if (refModel.getKey().equals(model.getKey())) {
                delta = model.toBuilder().reconcile(DeltaType.UPDATE, refModel);
            }
            else {
                final B builder = model.toBuilder();
                final M emptyModel = builder.buildEmpty();
                delta = builder.reconcile(DeltaType.ADD, emptyModel);
            }
        }
        else {
            final B emptyBuilder = refModel.toBuilderEmpty();
            delta = emptyBuilder.reconcile(DeltaType.DELETE, refModel);
        }

        if (deltaBuilder != null && delta != null && deltaBuilder.getKey().equals(delta.getKey())) {
            deltaBuilder.addDelta(delta);
            setter.set(deltaBuilder);
        }
        else if (delta != null) {
            setter.set(delta.toDeltaBuilder());
        }
        else {
            setter.set(null);
        }
    }

    public interface ReconcileMapNofity<K, V> {

        void add(K key, V value);

        void update(K key, V value);

        void delete(K key);
    }

    public interface ReconcileMapEquals<V1, V2> {

        boolean equals(V1 value1, V2 value2);
    }

    public interface ReconcileSetNotify<E> {

        void add(E element);

        void intersect(E element);

        void delete(E element);
    }
}
