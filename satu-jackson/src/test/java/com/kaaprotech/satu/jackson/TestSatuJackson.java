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

package com.kaaprotech.satu.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaaprotech.satu.runtime.java.Model;

public class TestSatuJackson {

    private ObjectMapper objectMapper_;
    private SimpleSatu simpleSatu1_;
    private SimpleSatu simpleSatu2_;
    private SimpleMapSatu simpleMapSatu_;
    private ModelMapSatu modelMapSatu_;
    private NestedMapSatu nestedMapSatu_;

    private SimpleSetSatu simpleSetSatu0_;
    private SimpleSetSatu simpleSetSatu1_;
    private SimpleSetSatu simpleSetSatu2_;

    private SimpleSatuKey simpleKey1_;
    private SimpleSatuKey simpleKey2_;
    private ModelSetSatu modelSetSatu_;

    @SuppressWarnings("boxing")
    @Before
    public void setup() {
        objectMapper_ = new ObjectMapper().registerModule(new SatuModule());

        simpleSatu1_ = SimpleSatu.newBuilder("simple1").setPrice(11).setDescription("desc1").build();
        simpleSatu2_ = SimpleSatu.newBuilder("simple2").setPrice(11).setDescription("desc2").build();

        simpleMapSatu_ = SimpleMapSatu.newBuilder("simpleMap")
                .setPrice(2)
                .putTestStringMap("simpleKey1", "simpleValue1")
                .putTestStringMap("simpleKey2", "simpleValue2")
                .build();

        modelMapSatu_ = ModelMapSatu.newBuilder("modelMap")
                .setPrice(3)
                .putTestMap("modelKey1", simpleSatu1_)
                .putTestMap("modelKey2", simpleSatu2_)
                .build();

        nestedMapSatu_ = NestedMapSatu.newBuilder("nestedMap")
                .setPrice(4)
                .putTestStringMap("key1", "value1")
                .putTestMap("simpleKey1", simpleSatu1_)
                .putTestMap("simpleKey2", simpleSatu2_)
                .putNestedMap("modelKey1", modelMapSatu_)
                .build();

        simpleSetSatu0_ = SimpleSetSatu.newBuilder("simpleSet").build();
        simpleSetSatu1_ = SimpleSetSatu.newBuilder("simpleSet1").addTestStringSet("set11").build();
        simpleSetSatu2_ = SimpleSetSatu.newBuilder("simpleSet2").addTestStringSet("set21").addTestStringSet("set22").build();

        simpleKey1_ = new SimpleSatuKey("name1", "name11");
        simpleKey2_ = new SimpleSatuKey("name2", "name22");

        modelSetSatu_ = ModelSetSatu.newBuilder("modelSet").addTestModelSet(simpleKey1_).addTestModelSet(simpleKey2_).build();
    }

    @Test
    public void testRoundtrip() throws IOException {
        assertEquals(simpleSatu1_, roundtrip(simpleSatu1_));
        assertEquals(simpleSatu2_, roundtrip(simpleSatu2_));
        assertNotEquals(simpleSatu2_, roundtrip(simpleSatu1_));

        assertEquals(simpleMapSatu_, roundtrip(simpleMapSatu_));
        assertEquals(modelMapSatu_, roundtrip(modelMapSatu_));
        assertEquals(nestedMapSatu_, roundtrip(nestedMapSatu_));
        assertEquals(simpleSetSatu0_, roundtrip(simpleSetSatu0_));
        assertEquals(simpleSetSatu1_, roundtrip(simpleSetSatu1_));
        assertEquals(simpleSetSatu2_, roundtrip(simpleSetSatu2_));

        assertEquals(modelSetSatu_, roundtrip(modelSetSatu_));
    }

    private <T extends Model<?, ?>> Model<?, ?> roundtrip(T myObject) throws IOException {
        String s = objectMapper_.writeValueAsString(myObject);
        return objectMapper_.readValue(s, myObject.getClass());
    }
}
