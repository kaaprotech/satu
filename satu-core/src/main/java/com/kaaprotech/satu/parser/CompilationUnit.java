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
import com.gs.collections.api.map.MutableMap;
import com.gs.collections.impl.factory.Lists;
import com.gs.collections.impl.factory.Maps;

public final class CompilationUnit {

    private String packageDeclaration_;

    private final MutableList<DeclaredType> declaredTypes_ = Lists.mutable.of();

    private final MutableMap<String, DeclaredType> declaredTypesMap_ = Maps.mutable.of();

    private final MutableList<ImportDeclaration> importDeclarations_ = Lists.mutable.of();

    private final boolean jsonCompatible_;

    public CompilationUnit(boolean jsonCompatible) {
        jsonCompatible_ = jsonCompatible;
    }

    public void setPackageDeclaration(String packageDeclaration) {
        packageDeclaration_ = packageDeclaration;
    }

    public void addDeclaredType(DeclaredType declaredType) {
        declaredTypes_.add(declaredType);
        declaredTypesMap_.put(declaredType.getName(), declaredType);
    }

    public String getPackageDeclaration() {
        return packageDeclaration_;
    }

    public MutableList<DeclaredType> getDeclaredTypes() {
        return declaredTypes_;
    }

    public MutableMap<String, DeclaredType> getDeclaredTypesMap() {
        return declaredTypesMap_;
    }

    public MutableList<ImportDeclaration> getImportDeclarations() {
        return importDeclarations_;
    }

    public void addImportDeclaration(final ImportDeclaration importDeclaration) {
        importDeclarations_.add(importDeclaration);
    }

    public boolean isJsonCompatible() {
        return jsonCompatible_;
    }
}
