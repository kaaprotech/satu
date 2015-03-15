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

import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;
import com.gs.collections.api.map.ImmutableMap;
import com.gs.collections.api.set.ImmutableSet;
import com.gs.collections.impl.map.immutable.AbstractImmutableMap;
import com.gs.collections.impl.set.immutable.AbstractImmutableSet;

/**
 * Created by jwhiting on 13/03/2015.
 */
public class SatuTypeModifier extends TypeModifier {

    @SuppressWarnings("unused")
    @Override
    public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
        final Class<?> raw = type.getRawClass();

        if (ImmutableMap.class.isAssignableFrom(raw)) {
            JavaType keyType = type.containedType(0);
            JavaType contentType = type.containedType(1);

            if (keyType == null) {
                keyType = TypeFactory.unknownType();
            }
            if (contentType == null) {
                contentType = TypeFactory.unknownType();
            }
            return typeFactory.constructMapType(AbstractImmutableMap.class, keyType, contentType);
        }

        if (ImmutableSet.class.isAssignableFrom(raw)) {
            JavaType contentType = type.containedType(0);

            if (contentType == null) {
                contentType = TypeFactory.unknownType();
            }
            return typeFactory.constructCollectionType(AbstractImmutableSet.class, contentType);
        }
        return type;
    }
}
