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

public class DeclaredType {

    private final String name_;

    private final DeclaredTypeCategory declaredTypeCategory_;

    private final MutableList<Field> fields = Lists.mutable.of();

    public DeclaredType(final String name, final DeclaredTypeCategory declaredTypeCategory) {
        name_ = name;
        declaredTypeCategory_ = declaredTypeCategory;
    }

    public void addField(final Field field) {
        fields.add(field);
    }

    public String getName() {
        return name_;
    }

    public DeclaredTypeCategory getDeclaredTypeCategory() {
        return declaredTypeCategory_;
    }

    public MutableList<Field> getFields() {
        return fields;
    }
}
