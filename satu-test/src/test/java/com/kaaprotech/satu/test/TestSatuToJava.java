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

package com.kaaprotech.satu.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Test;

import com.gs.collections.impl.factory.Lists;
import com.gs.collections.impl.factory.Maps;
import com.gs.collections.impl.factory.Sets;
import com.kaaprotech.satu.runtime.java.DeltaType;
import com.kaaprotech.satu.test.model.SatuTestEnum;
import com.kaaprotech.satu.test.model.SatuTestKey;
import com.kaaprotech.satu.test.model.SatuTestModel;

@SuppressWarnings("boxing")
public class TestSatuToJava {

    private final SatuTestKey KEY_1 = new SatuTestKey(
            1,
            1L,
            true,
            'a',
            (byte) 0,
            1.1f,
            Sets.immutable.of(SatuTestEnum.FirstEnumMember, SatuTestEnum.SecondEnumMember),
            Maps.immutable.of("Key1", 1.1d, "Key2", 2.2d),
            Sets.immutable.of(1, 2),
            Maps.immutable.of(1, 1, 2, 2));

    // Equal to KEY_1
    private final SatuTestKey KEY_2 = new SatuTestKey(
            1,
            1L,
            true, 'a',
            (byte) 0, 1.1f,
            Sets.immutable.of(SatuTestEnum.SecondEnumMember, SatuTestEnum.FirstEnumMember),
            Maps.immutable.of("Key2", 2.2d, "Key1", 1.1d),
            Sets.immutable.of(2, 1),
            Maps.immutable.of(2, 2, 1, 1));

    private final SatuTestKey KEY_3 = new SatuTestKey(
            1,
            1L,
            true,
            'a',
            (byte) 1, // Differs
            1.1f,
            Sets.immutable.of(SatuTestEnum.FirstEnumMember, SatuTestEnum.SecondEnumMember),
            Maps.immutable.of("Key1", 1.1d, "Key2", 2.2d),
            Sets.immutable.of(2, 1),
            Maps.immutable.of(1, 1, 2, 2));

    @Test
    public void testEnum() {
        assertEquals(4, SatuTestEnum.values().length);
        assertEquals("FirstEnumMember", SatuTestEnum.values()[0].name());
        assertEquals("SecondEnumMember", SatuTestEnum.values()[1].name());
        assertEquals("ThirdEnumMember", SatuTestEnum.values()[2].name());
        assertEquals("ForthEnumMember", SatuTestEnum.values()[3].name());
    }

    @Test
    public void testKey() {
        assertEquals(KEY_1, KEY_2);
        assertEquals(KEY_1.hashCode(), KEY_2.hashCode());
        assertEquals(0, KEY_1.compareTo(KEY_2));
        assertNotEquals(KEY_1, KEY_3);
        assertEquals(-1, KEY_1.compareTo(KEY_3));
    }

    @Test
    public void testModelDefaults() {
        final SatuTestModel model = SatuTestModel.newBuilder(1).build();
        assertEquals(Integer.valueOf(7), model.getIntFieldWithDefault());
        assertEquals(Double.valueOf(23.7d), model.getDoubleFieldWithDefault());
        assertEquals("~stringFieldWithDefault~", model.getStringFieldWithDefault());
        assertEquals(SatuTestEnum.ThirdEnumMember, model.getEnumFieldWithDefault());
    }

    @Test
    public void testModelDeltaBasic() {
        final SatuTestModel base = SatuTestModel.newBuilder(1).build();

        // No changes so no delta
        assertNull(base.toBuilder().reconcile());
        assertNull(base.toBuilder().setIntFieldWithDefault(base.getIntFieldWithDefault()).reconcile());

        final SatuTestModel.Delta delta1 = base.toBuilder().setIntFieldWithDefault(base.getIntFieldWithDefault() + 1).reconcile();
        assertNotNull(delta1);
        assertTrue(delta1.hasIntFieldWithDefault());
        assertEquals(Integer.valueOf(base.getIntFieldWithDefault() + 1), delta1.getIntFieldWithDefault());

        final SatuTestModel.Builder builder2 = base.toBuilder();
        builder2.getSetOfKeysField().addAll(Lists.mutable.of(KEY_1, KEY_2, KEY_3));
        final SatuTestModel.Delta delta2 = builder2.reconcile();
        assertNotNull(delta2);
        assertTrue(delta2.hasSetOfKeysField());
        assertEquals(2, delta2.getSetOfKeysField().size());
        assertEquals(DeltaType.ADD, delta2.getSetOfKeysField().toSortedList().get(0).getDeltaType());
        assertEquals(KEY_1, delta2.getSetOfKeysField().toSortedList().get(0).getKey());
        assertEquals(DeltaType.ADD, delta2.getSetOfKeysField().toSortedList().get(1).getDeltaType());
        assertEquals(KEY_3, delta2.getSetOfKeysField().toSortedList().get(1).getKey());

        final SatuTestModel.Builder builder3 = base.toBuilder();
        final SatuTestModel.Builder childBuilder = SatuTestModel.newBuilder(2).setStringField("Test");
        final SatuTestModel.Delta childDelta = childBuilder.toDelta(DeltaType.ADD);
        builder3.getMapOfModelsField().put(SatuTestEnum.ThirdEnumMember, childBuilder);
        final SatuTestModel.Delta delta3 = builder3.reconcile();
        assertNotNull(delta3);
        assertTrue(delta3.hasMapOfModelsField());
        assertEquals(1, delta3.getMapOfModelsField().size());
        assertEquals(DeltaType.ADD, delta3.getMapOfModelsField().getFirst().getDeltaType());
        assertEquals(SatuTestEnum.ThirdEnumMember, delta3.getMapOfModelsField().getFirst().getKey());
        assertEquals(childDelta, delta3.getMapOfModelsField().getFirst().getValue());
    }

    @Test
    public void testModelWithNullModleField() {
        final SatuTestModel base1 = SatuTestModel.newBuilder(1).build();
        final SatuTestModel modelField = SatuTestModel.newBuilder(2).build();
        final SatuTestModel base2 = base1.toBuilder().setModelField(modelField.toBuilder()).build();
        assertNotNull(base2.getModelField());
        assertEquals(base2.getModelField(), modelField);
    }

    @Test
    public void testModelDeltaWithUnmodifiedModleField() {
        final SatuTestModel base1 = SatuTestModel.newBuilder(1).build();
        final SatuTestModel modelField = SatuTestModel.newBuilder(2).build();
        final SatuTestModel base2 = base1.toBuilder().setModelField(modelField.toBuilder()).build();
        assertNotNull(base2.getModelField());
        SatuTestModel.Delta delta1 = base2.toBuilder().setIntField(Integer.valueOf(11223344)).reconcile();
        assertNotNull(delta1);
        assertTrue(delta1.hasIntField());
        assertEquals(delta1.getIntField(), Integer.valueOf(11223344));
        assertFalse(delta1.hasModelField());

        final SatuTestModel.Delta updateDelta = base2.toDelta(DeltaType.UPDATE);
        assertNotNull(updateDelta);
        assertSame(updateDelta.getDeltaType(), DeltaType.UPDATE);
        assertTrue(updateDelta.hasModelField());
        assertNotNull(updateDelta.getModelField());
        assertSame(updateDelta.getModelField().getDeltaType(), DeltaType.ADD);

        final SatuTestModel.Delta addDelta = base2.toDelta(DeltaType.ADD);
        assertNotNull(addDelta);
        assertSame(addDelta.getDeltaType(), DeltaType.ADD);
        assertTrue(addDelta.hasModelField());
        assertNotNull(addDelta.getModelField());
        assertSame(addDelta.getModelField().getDeltaType(), DeltaType.ADD);

        final SatuTestModel.Delta deleteDelta = base2.toDelta(DeltaType.DELETE);
        assertNotNull(deleteDelta);
        assertTrue(deleteDelta.hasModelField());
        assertNotNull(deleteDelta.getModelField());
        assertSame(deleteDelta.getModelField().getDeltaType(), DeltaType.DELETE);
    }

    @Test
    public void testModelCollectionDeltasBasic() {
        final SatuTestModel base1 = SatuTestModel.newBuilder(1).build();
        final DateTime dt = new DateTime();
        final SatuTestModel.Builder builder = base1.toBuilder()
                .addSetOfImportedTypes(dt)
                .addSetOfKeysField(KEY_1)
                .putMapOfImportedTypes(dt, dt)
                .putMapOfKeysField(SatuTestEnum.FirstEnumMember, KEY_2)
                .putMapOfModelsField(SatuTestEnum.SecondEnumMember, SatuTestModel.newBuilder(2));
        final SatuTestModel.Delta delta = builder.reconcile();
        final SatuTestModel base2 = builder.build();
        final SatuTestModel base3 = base1.toBuilder().applyDelta(delta).build();
        assertEquals(base2, base3);
    }
}
