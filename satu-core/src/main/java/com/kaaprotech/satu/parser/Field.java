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

package com.kaaprotech.satu.parser;

import com.gs.collections.api.list.MutableList;
import com.gs.collections.impl.factory.Lists;

public final class Field {

    private FieldModifier modifier_;

    private String name_;

    private FieldTypeCategory fieldTypeCategory_;

    private String typeName_;

    private String initializer_;

    private final MutableList<String> typeArguments_ = Lists.mutable.of();

    private final MutableList<Annotation> annotations_ = Lists.mutable.of();

    public FieldModifier getModifier() {
        return modifier_;
    }

    public void setModifier(final FieldModifier modifier) {
        modifier_ = modifier;
    }

    public String getName() {
        return name_;
    }

    public void setName(final String name) {
        name_ = name;
    }

    public FieldTypeCategory getFieldTypeCategory() {
        return fieldTypeCategory_;
    }

    public void setFieldTypeCategory(final FieldTypeCategory fieldTypeCategory) {
        fieldTypeCategory_ = fieldTypeCategory;
    }

    public String getTypeName() {
        return typeName_;
    }

    public void setTypeName(final String typeName) {
        typeName_ = typeName;
    }

    public String getJavaTypeName() {
        switch (fieldTypeCategory_) {
        case Primitive:
            return PrimitiveType.valueOf(typeName_).getWrapperClass();
        case ImportedType:
            return ImportedType.valueOf(typeName_).getWrapperClass();
        default:
            return typeName_;
        }
    }

    public String getInitializer() {
        return initializer_;
    }

    public void setInitializer(final String Initializer) {
        initializer_ = Initializer;
    }

    public MutableList<String> getTypeArguments() {
        return typeArguments_;
    }

    public void addTypeArgument(final String typeArgument) {
        typeArguments_.add(typeArgument);
    }

    public MutableList<Annotation> getAnnotations() {
        return annotations_;
    }

    public void addAnnotation(final Annotation annotation) {
        annotations_.add(annotation);
    }
}
