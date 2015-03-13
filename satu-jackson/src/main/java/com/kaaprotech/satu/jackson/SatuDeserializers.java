package com.kaaprotech.satu.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
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

import java.io.IOException;

/**
 * Created by jwhiting on 13/03/2015.
 */
public class SatuDeserializers extends Deserializers.Base {

    @Override
    public JsonDeserializer<?> findMapDeserializer(final MapType type, DeserializationConfig config, BeanDescription beanDesc, final KeyDeserializer keyDeserializer, final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {

        if (ImmutableMap.class.isAssignableFrom(type.getRawClass())) {
            return new StdDeserializer<Object>(type) {
                @Override
                public Object deserialize(JsonParser jp, DeserializationContext context) throws IOException {

                    JsonToken t = jp.getCurrentToken();
                    if (t == JsonToken.START_OBJECT) {
                        t = jp.nextToken();
                    }
                    if (t != JsonToken.FIELD_NAME && t != JsonToken.END_OBJECT) {
                        throw context.mappingException(type.getRawClass());
                    }

                    MutableMap m = Maps.mutable.of();

                    for (; jp.getCurrentToken() == JsonToken.FIELD_NAME; jp.nextToken()) {
                        // Pointing to field name
                        String fieldName = jp.getCurrentName();
                        Object key = (keyDeserializer == null) ? fieldName : keyDeserializer.deserializeKey(fieldName, context);
                        t = jp.nextToken();

                        Object value;
                        if (t == JsonToken.VALUE_NULL) {
                            value = null;
                        } else if (elementDeserializer == null) {
                            value = jp.readValueAs(type.getContentType().getRawClass());
                        } else if (elementTypeDeserializer == null) {
                            value = elementDeserializer.deserialize(jp, context);
                        } else {
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
    public JsonDeserializer<?> findCollectionDeserializer(final CollectionType type, DeserializationConfig config, BeanDescription beanDesc, final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {

        if (ImmutableSet.class.isAssignableFrom(type.getRawClass())) {
            return new StdDeserializer<Object>(type) {
                @Override
                public Object deserialize(JsonParser jp, DeserializationContext context) throws IOException {

                    if (jp.isExpectedStartArrayToken()) {
                        JsonToken t;

                        MutableSet s = Sets.mutable.of();

                        while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
                            Object value;
                            if (t == JsonToken.VALUE_NULL) {
                                value = null;
                            } else if (elementDeserializer == null) {
                                value = jp.readValueAs(type.getContentType().getRawClass());
                            } else if (elementTypeDeserializer == null) {
                                value = elementDeserializer.deserialize(jp, context);
                            } else {
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
