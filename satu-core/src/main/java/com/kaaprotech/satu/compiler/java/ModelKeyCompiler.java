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

public final class ModelKeyCompiler extends AbstractModelCompiler {

    public ModelKeyCompiler(final CompilationUnit cu, final DeclaredType dt, final PrintWriter writer) {
        super(cu, dt, writer);
    }

    public void compile() {
        compileHeader();
        compilePackage();
        compileImports();
        compileClassJavaDoc();
        compileClassStart();
        compileFieldMembers();
        compilePublicConstructor();
        compileGetters();
        compileIdentityMethods();
        compileToString();
        compileEquals();
        compileHashCode();
        compileCompareTo();
        compileClassEnd();
    }

    public void compileImports() {
        out();
        out("import java.io.Serializable;");
        out();

        out("import org.apache.commons.lang.builder.CompareToBuilder; ");
        out("import org.apache.commons.lang.builder.HashCodeBuilder;");
        out("import org.apache.commons.lang.builder.ToStringBuilder;");
        out("import org.apache.commons.lang.builder.ToStringStyle;");
        out("import org.apache.commons.lang.builder.EqualsBuilder;");

        out();
        out("import com.gs.collections.api.map.ImmutableMap;");
        out("import com.gs.collections.api.set.ImmutableSet;");

        out();
        out("import com.kaaprotech.satu.runtime.java.*;");

        compileImportedTypeImports();

        compileUserImports();
    }

    public void compileClassStart() {
        out("public final class " + dt_.getName() + " implements Identity<" + getKeyFieldType() + ">, Comparable<" + dt_.getName() + ">, Serializable {");
        out();
        final long serialVersionUID = serialVersionUID();
        out(1, "private static final long serialVersionUID = " + serialVersionUID + "L;");
    }

    public void compileCompareTo() {
        out();
        out(1, "@Override");
        out(1, "public int compareTo(final " + dt_.getName() + " rhs) {");
        out(2, "return new CompareToBuilder()");
        for (Field field : dt_.getFields()) {
            if (isCollectionType(field)) {
                out(4, ".append(" + field.getName() + "_.toSortedList().toArray(), rhs." + field.getName() + "_.toSortedList().toArray())");
            }
            else {
                out(4, ".append(" + field.getName() + "_, rhs." + field.getName() + "_)");
            }
        }
        out(4, ".toComparison();");
        out(1, "}");
    }
}
