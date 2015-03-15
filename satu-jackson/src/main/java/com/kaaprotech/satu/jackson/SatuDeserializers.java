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

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.gs.collections.api.map.ImmutableMap;
import com.gs.collections.api.map.MutableMap;
import com.gs.collections.api.set.ImmutableSet;
import com.gs.collections.api.set.MutableSet;
import com.gs.collections.impl.factory.Maps;
import com.gs.collections.impl.factory.Sets;

/**
 * Created by jwhiting on 13/03/2015.
 */
public class SatuDeserializers extends Deserializers.Base {

    @Override
    public JsonDeserializer<?> findMapDeserializer(final MapType type, final DeserializationConfig config, BeanDescription beanDesc, final KeyDeserializer keyDeserializer,
            final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {

        if (ImmutableMap.class.isAssignableFrom(type.getRawClass())) {
            return new StdDeserializer<Object>(type) {
                private static final long serialVersionUID = 1L;

                @Override
                public Object deserialize(JsonParser jp, DeserializationContext context) throws IOException {

                    JsonToken t = jp.getCurrentToken();
                    if (t == JsonToken.START_OBJECT) {
                        t = jp.nextToken();
                    }
                    if (t != JsonToken.FIELD_NAME && t != JsonToken.END_OBJECT) {
                        throw context.mappingException(type.getRawClass());
                    }

                    MutableMap<Object, Object> m = Maps.mutable.of();

                    for (; jp.getCurrentToken() == JsonToken.FIELD_NAME; jp.nextToken()) {
                        // Pointing to field name
                        String fieldName = jp.getCurrentName();
                        Object key = (keyDeserializer == null) ? fieldName : keyDeserializer.deserializeKey(fieldName, context);
                        t = jp.nextToken();

                        Object value;
                        if (t == JsonToken.VALUE_NULL) {
                            value = null;
                        }
                        else if (elementDeserializer == null) {
                            value = jp.readValueAs(type.getContentType().getRawClass());
                        }
                        else if (elementTypeDeserializer == null) {
                            value = elementDeserializer.deserialize(jp, context);
                        }
                        else {
                            value = elementDeserializer.deserializeWithType(jp, context, elementTypeDeserializer);
                        }
                        m.put(key, value);
                    }
                    return m.toImmutable();
                }
            };
        }

        return super.findMapDeserializer(type, config, beanDesc, keyDeserializer, elementTypeDeserializer, elementDeserializer);
    }

    @Override
    public JsonDeserializer<?> findCollectionDeserializer(final CollectionType type, final DeserializationConfig config, final BeanDescription beanDesc,
            final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {

        if (ImmutableSet.class.isAssignableFrom(type.getRawClass())) {
            return new StdDeserializer<Object>(type) {
                private static final long serialVersionUID = 1L;

                @Override
                public Object deserialize(JsonParser jp, DeserializationContext context) throws IOException {

                    if (jp.isExpectedStartArrayToken()) {
                        JsonToken t;

                        MutableSet<Object> s = Sets.mutable.of();

                        while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
                            Object value;
                            if (t == JsonToken.VALUE_NULL) {
                                value = null;
                            }
                            else if (elementDeserializer == null) {
                                value = jp.readValueAs(type.getContentType().getRawClass());
                            }
                            else if (elementTypeDeserializer == null) {
                                value = elementDeserializer.deserialize(jp, context);
                            }
                            else {
                                value = elementDeserializer.deserializeWithType(jp, context, elementTypeDeserializer);
                            }
                            s.add(value);
                        }
                        return s.toImmutable();
                    }
                    throw context.mappingException(type.getRawClass());
                }
            };
        }

        return super.findCollectionDeserializer(type, config, beanDesc, elementTypeDeserializer, elementDeserializer);
    }
}
