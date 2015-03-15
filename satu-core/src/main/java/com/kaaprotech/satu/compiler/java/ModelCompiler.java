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

package com.kaaprotech.satu.compiler.java;

import java.io.PrintWriter;

import com.kaaprotech.satu.parser.CompilationUnit;
import com.kaaprotech.satu.parser.DeclaredType;
import com.kaaprotech.satu.parser.Field;

public final class ModelCompiler extends AbstractModelCompiler {

    private final ModelBuilderCompiler builder_;

    private final ModelDeltaCompiler delta_;

    private final ModelDeltaBuilderCompiler deltaBuilder_;

    public ModelCompiler(final CompilationUnit cu, final DeclaredType dt, final PrintWriter writer) {
        super(cu, dt, writer);
        builder_ = new ModelBuilderCompiler(this);
        delta_ = new ModelDeltaCompiler(this);
        deltaBuilder_ = new ModelDeltaBuilderCompiler(this);
    }

    public void compile() {
        compileHeader();
        compilePackage();
        compileImports();
        compileClassJavaDoc();
        compileClassStart();
        compileFieldMembers();
        compilePrivateConstructor();
        compileGetters();
        compileIdentityMethods();
        compileToString();
        compileEquals();
        compileHashCode();
        compileCompareTo();
        compileToBuilderMethod();
        compileToBuilderEmptyMethod();
        compileNewBuilderMethod();
        compileToDeltaMethod();
        compileNewDeltaBuilderMethod();

        builder_.compile();

        delta_.compile();

        deltaBuilder_.compile();

        delta_.compileDeltaClassEnd();

        compileClassEnd();
    }

    public void compileImports() {
        out();
        out("import java.io.Serializable;");
        out("import java.util.BitSet;");
        out();

        out("import org.apache.commons.lang.ObjectUtils;");
        out("import org.apache.commons.lang.builder.EqualsBuilder;");
        out("import org.apache.commons.lang.builder.HashCodeBuilder;");
        out("import org.apache.commons.lang.builder.ToStringBuilder;");
        out("import org.apache.commons.lang.builder.ToStringStyle;");
        out();

        out("import com.gs.collections.api.block.function.Function;");
        out("import com.gs.collections.api.block.procedure.Procedure;");
        out("import com.gs.collections.api.list.ImmutableList;");
        out("import com.gs.collections.api.map.ImmutableMap;");
        out("import com.gs.collections.api.map.MutableMap;");
        out("import com.gs.collections.api.set.ImmutableSet;");
        out("import com.gs.collections.api.set.MutableSet;");
        out("import com.gs.collections.impl.factory.Lists;");
        out("import com.gs.collections.impl.factory.Maps;");
        out("import com.gs.collections.impl.factory.Sets;");

        if (cu_.isJsonCompatible()) {
            out();
            out("import com.fasterxml.jackson.annotation.JsonIgnore;");
            out("import com.fasterxml.jackson.annotation.JsonProperty;");
        }

        out();
        out("import com.kaaprotech.satu.runtime.java.*;");

        compileImportedTypeImports();

        compileUserImports();
    }

    public void compileCompareTo() {
        out();
        out(1, "@Override");
        out(1, "public int compareTo(final " + dt_.getName() + " rhs) {");
        out(2, "return getKey().compareTo(rhs.getKey());");
        out(1, "}");
    }

    public void compileClassStart() {
        out("public final class " + dt_.getName() + " implements Model<" + getKeyFieldType() + ", " + dt_.getName() + ".Builder>, Comparable<" + dt_.getName() + ">, Serializable {");
        out();
        final long serialVersionUID = serialVersionUID();
        out(1, "private static final long serialVersionUID = " + serialVersionUID + "L;");
    }

    public void compileToBuilderMethod() {
        out();
        out(1, "@Override");
        out(1, "public " + dt_.getName() + ".Builder toBuilder() {");
        out(2, "return new " + dt_.getName() + ".Builder(this);");
        out(1, "}");
    }

    public void compileToBuilderEmptyMethod() {
        out();
        out(1, "@Override");
        out(1, "public " + dt_.getName() + ".Builder toBuilderEmpty() {");
        out(2, "return new " + dt_.getName() + ".Builder(getKey());");
        out(1, "}");
    }

    public void compileToDeltaMethod() {
        out();
        out(1, "public " + dt_.getName() + ".Delta toDelta(final DeltaType deltaType) {");
        out(2, "switch (deltaType) {");
        out(2, "case ADD:");
        out(2, "case UPDATE:");
        out(3, "return toBuilder().reconcile(deltaType, toBuilderEmpty().build());");
        out(2, "case DELETE:");
        out(3, "return toBuilderEmpty().reconcile(deltaType, this);");
        out(2, "}");
        out(2, "throw new RuntimeException(\"Invalid delta type \" + deltaType);");
        out(1, "}");
    }

    public void compileNewBuilderMethod() {
        final Field field = getKeyField();
        out();
        out(1, "public static " + dt_.getName() + ".Builder newBuilder(final " + getFieldType(field) + " " + field.getName() + ") {");
        out(2, "return new " + dt_.getName() + ".Builder(" + field.getName() + ");");
        out(1, "}");
    }

    public void compileNewDeltaBuilderMethod() {
        final Field field = getKeyField();
        out();
        out(1, "public static " + dt_.getName() + ".Delta.Builder newDeltaBuilder(final DeltaType deltaType, final " + getFieldType(field) + " " + field.getName() + ") {");
        out(2, "return new " + dt_.getName() + ".Delta.Builder(deltaType, " + field.getName() + ");");
        out(1, "}");
    }
}
