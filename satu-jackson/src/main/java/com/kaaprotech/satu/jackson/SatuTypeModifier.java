package com.kaaprotech.satu.jackson;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;
import com.gs.collections.api.map.ImmutableMap;
import com.gs.collections.api.set.ImmutableSet;
import com.gs.collections.impl.map.immutable.AbstractImmutableMap;
import com.gs.collections.impl.set.immutable.AbstractImmutableSet;

import java.lang.reflect.Type;

/**
 * Created by jwhiting on 13/03/2015.
 */
public class SatuTypeModifier extends TypeModifier {
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
            return typeFactory.constructMapType(AbstractImmutableMap.class, keyType, contentType );
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
