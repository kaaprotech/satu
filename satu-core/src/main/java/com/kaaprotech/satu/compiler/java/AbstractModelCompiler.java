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

import static com.kaaprotech.satu.compiler.java.CompilerUtil.LS;
import static com.kaaprotech.satu.compiler.java.CompilerUtil.TAB;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;

import com.gs.collections.api.block.predicate.Predicate;
import com.gs.collections.api.block.procedure.Procedure;
import com.gs.collections.api.set.MutableSet;
import com.gs.collections.impl.factory.Sets;
import com.kaaprotech.satu.parser.Annotation;
import com.kaaprotech.satu.parser.CompilationUnit;
import com.kaaprotech.satu.parser.DeclaredType;
import com.kaaprotech.satu.parser.DeclaredTypeCategory;
import com.kaaprotech.satu.parser.Field;
import com.kaaprotech.satu.parser.FieldModifier;
import com.kaaprotech.satu.parser.FieldTypeCategory;
import com.kaaprotech.satu.parser.ImportDeclaration;
import com.kaaprotech.satu.parser.ImportedType;
import com.kaaprotech.satu.parser.PrimitiveType;

@SuppressWarnings("serial")
public abstract class AbstractModelCompiler {

    protected final CompilationUnit cu_;

    protected final DeclaredType dt_;

    protected final PrintWriter writer_;

    public AbstractModelCompiler(final CompilationUnit cu, final DeclaredType dt, final PrintWriter writer) {
        cu_ = cu;
        dt_ = dt;
        writer_ = writer;
    }

    public final Predicate<Field> fieldsByModifierPredicate(final FieldModifier... modifiers) {
        return new Predicate<Field>() {
            @Override
            public boolean accept(final Field field) {
                for (int i = 0; i < modifiers.length; i++) {
                    if (field.getModifier() == modifiers[i]) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public final Predicate<Field> selectFieldsByFieldTypeCategory(final FieldTypeCategory... categories) {
        return new Predicate<Field>() {
            @Override
            public boolean accept(final Field field) {
                for (int i = 0; i < categories.length; i++) {
                    if (field.getFieldTypeCategory() == categories[i]) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public final String getMutableColOf(final Field field) {
        switch (field.getFieldTypeCategory()) {
        case Set:
            return "Sets.mutable.of()";
        case Map:
            return "Maps.mutable.of()";
        default:
            throw new RuntimeException("Unsupported FieldTypeCategory " + field.getFieldTypeCategory());
        }
    }

    public final String getMutableColConv(final Field field) {
        switch (field.getFieldTypeCategory()) {
        case Set:
            return ".toSet()";
        case Map:
            return ".toMap()";
        default:
            throw new RuntimeException("Unsupported FieldTypeCategory " + field.getFieldTypeCategory());
        }
    }

    public final String getImmutableColType(final Field field) {
        switch (field.getFieldTypeCategory()) {
        case Set:
        case Map:
            return CompilerUtil.getFieldType(field);
        default:
            throw new RuntimeException("Unsupported FieldTypeCategory " + field.getFieldTypeCategory());
        }
    }

    public final String getPrimitiveWrapperInitializer(final Field field) {
        if (field.getInitializer() == null) {
            return "null";
        }
        final PrimitiveType type = PrimitiveType.valueOf(field.getTypeName());
        switch (type) {
        case String:
            return field.getInitializer();
        default:
            return "new " + type.getWrapperClass() + "(" + field.getInitializer() + ")";
        }
    }

    public final boolean isTypeMutable(final String typeName) {
        return cu_.getDeclaredTypesMap().containsKey(typeName) && cu_.getDeclaredTypesMap().get(typeName).getDeclaredTypeCategory() == DeclaredTypeCategory.Model;
    }

    public final boolean isEnum(final Field field) {
        return field.getFieldTypeCategory() == FieldTypeCategory.DeclaredType && cu_.getDeclaredTypesMap().get(field.getTypeName()).getDeclaredTypeCategory() == DeclaredTypeCategory.Enum;
    }

    public final String javaTypeName(final String typeName) {
        return CompilerUtil.javaTypeName(typeName);
    }

    public final boolean isCollectionType(final Field field) {
        return field.getFieldTypeCategory() == FieldTypeCategory.Map || field.getFieldTypeCategory() == FieldTypeCategory.Set;
    }

    public final boolean isMapValueTypeMutable(final Field field) {
        return field.getFieldTypeCategory() == FieldTypeCategory.Map && isTypeMutable(field.getTypeArgs().get(1));
    }

    public final boolean isMapKeySameAsMutableValueTypeKey(final Field field) {
        if (!isMapValueTypeMutable(field)) {
            return false;
        }
        return getKeyField(cu_.getDeclaredTypesMap().get(field.getTypeArgs().get(1))).getTypeName().equals(field.getTypeArgs().get(0));
    }

    public final void compileHeader() {
        final StringBuilder builder = new StringBuilder();
        builder.append("/*****************************************************************************" + LS);
        builder.append(" * Auto generated by Satu, please don't edit or commit this file to SCM" + LS);
        builder.append("*****************************************************************************/" + LS);
        out(builder.toString());
    }

    public final void compilePackage() {
        out("package " + cu_.getPackageDeclaration() + ";");
    }

    public final void compileImportedTypeImports() {
        final MutableSet<String> imports = Sets.mutable.of();
        dt_.getFields().forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                if (field.getFieldTypeCategory() == FieldTypeCategory.ImportedType) {
                    imports.add(ImportedType.valueOf(field.getTypeName()).getImportStatement());
                }
            }
        });
        if (imports.notEmpty()) {
            out();
            imports.toSortedList().forEach(new Procedure<String>() {
                public void value(final String importStatement) {
                    out(importStatement);
                }
            });
        }
    }

    public final void compileUserImports() {
        if (cu_.getImportDeclarations().notEmpty()) {
            out();
            for (ImportDeclaration id : cu_.getImportDeclarations()) {
                out("import " + id.getQualifiedName() + ";");
            }
        }
    }

    public final void compileClassJavaDoc() {
        out();
        out("/**");
        out(" * <p>");
        out(" * " + dt_.getDeclaredTypeCategory() + ": " + dt_.getName());
        out(" * </p>");
        out(" * This class was generated by Satu");
        out(" */");
    }

    public final void compileClassEnd() {
        out("}");
    }

    protected final int getHashCodeBuilderArg(final String name) {
        int retVal = Math.abs(name.hashCode());
        if (retVal == 0) {
            retVal = 3251;
        }
        if (retVal % 2 == 0) {
            retVal++;
        }
        return retVal;
    }

    protected final String tab(final int tabs) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tabs; i++) {
            builder.append(TAB);
        }
        return builder.toString();
    }

    protected final void out(final int tabs, final String output) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tabs; i++) {
            builder.append(TAB);
        }
        builder.append(output);
        writer_.println(builder.toString());
    }

    protected final void out(final String output) {
        out(0, output);
    }

    protected final void out() {
        writer_.println();
    }

    protected final void compileIdentityMethods() {
        out();
        out(1, "@Override");
        if (cu_.isJsonCompatible()) {
            out(1, "@JsonIgnore");
        }
        out(1, "public " + getKeyFieldType() + " getKey() {");
        if (dt_.getDeclaredTypeCategory() == DeclaredTypeCategory.Key) {
            out(2, "return this;");
        }
        else {
            out(2, "return " + getKeyField().getName() + "_;");
        }
        out(1, "}");
    }

    protected final void compileToString() {
        out();
        out(1, "@Override");
        out(1, "public String toString() {");
        out(2, "return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)");
        for (Field field : dt_.getFields()) {
            out(4, ".append(\"" + field.getName() + "\", " + field.getName() + "_)");
        }
        out(4, ".toString();");
        out(1, "}");
    }

    public final void compileEquals() {
        out();
        out(1, "@Override");
        out(1, "public boolean equals(final Object obj) {");
        out(2, "if (obj == null) {");
        out(3, "return false;");
        out(2, "}");
        out(2, "if (obj == this) {");
        out(3, "return true;");
        out(2, "}");
        out(2, "if (obj.getClass() != getClass()) {");
        out(3, "return false;");
        out(2, "}");
        out(2, "final " + dt_.getName() + " rhs = (" + dt_.getName() + ") obj;");
        out(2, "return new EqualsBuilder()");
        for (Field field : dt_.getFields()) {
            out(4, ".append(" + field.getName() + "_, rhs." + field.getName() + "_)");
        }
        out(4, ".isEquals();");
        out(1, "}");
    }

    public final void compileHashCode() {
        out();
        out(1, "@Override");
        out(1, "public int hashCode() {");
        int arg1 = getHashCodeBuilderArg(dt_.getName());
        int arg2 = getHashCodeBuilderArg(new StringBuilder(dt_.getName()).reverse().toString());
        out(2, "return new HashCodeBuilder(" + arg1 + ", " + arg2 + ")");
        for (Field field : dt_.getFields()) {
            out(4, ".append(" + field.getName() + "_)");
        }
        out(4, ".toHashCode();");
        out(1, "}");
    }

    public final String getFieldType(final Field field) {
        return CompilerUtil.getFieldType(field);
    }

    public final String getKeyFieldType() {
        return CompilerUtil.getKeyFieldType(dt_);
    }

    public final Field getKeyField() {
        return getKeyField(dt_);
    }

    public final Field getKeyField(DeclaredType dt) {
        return CompilerUtil.getKeyField(dt);
    }

    public final void compileFieldMembers() {
        for (Field field : dt_.getFields()) {
            out();
            if (field.getModifier() == FieldModifier.key) {
                out(1, "// Key");
            }
            for (Annotation annotation : field.getAnnotations()) {
                if (annotation.getText() == null) {
                    out(1, "@" + annotation.getName());
                }
                else {
                    out(1, "@" + annotation.getName() + "(" + annotation.getText() + ")");
                }
            }
            out(1, "private final " + getFieldType(field) + " " + field.getName() + "_;");
        }
    }

    public final String methodNameForGetter(Field field) {
        return "get" + capitalize(field.getName());
    }

    public final String methodNameForSetter(Field field) {
        return "set" + capitalize(field.getName());
    }

    public final String methodNameForHas(Field field) {
        return "has" + capitalize(field.getName());
    }

    public final String methodNameForAdd(Field field) {
        return "add" + capitalize(field.getName());
    }

    public final String methodNameForPut(Field field) {
        return "put" + capitalize(field.getName());
    }

    public final String methodNameForAddOrReplace(Field field) {
        return "addOrReplace" + capitalize(field.getName());
    }

    public final String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public final void compilePrivateConstructor() {
        compileConstructor("private");
    }

    public final void compilePublicConstructor() {
        compileConstructor("public");
    }

    private final void compileConstructor(final String visibility) {
        out();
        out(1, visibility + " " + dt_.getName() + "(");
        for (int i = 0; i < dt_.getFields().size(); i++) {
            final Field field = dt_.getFields().get(i);

            String prefix = "";
            if (cu_.isJsonCompatible()) {
                prefix = "@JsonProperty(\"" + field.getName() + "\") ";
            }
            if (i + 1 < dt_.getFields().size()) {
                out(3, prefix + getFieldType(field) + " " + field.getName() + ",");
            }
            else {
                out(3, prefix + getFieldType(field) + " " + field.getName() + ") {");
            }
        }
        for (Field field : dt_.getFields()) {
            if (field.getFieldTypeCategory() == FieldTypeCategory.Map || field.getFieldTypeCategory() == FieldTypeCategory.Set) {
                String createEmpty = String.format("(Immutable%1$s)%1$ss.immutable.of()", field.getFieldTypeCategory().name());
                out(2, field.getName() + "_ = " + field.getName() + " != null ? " + field.getName() + " : " + createEmpty + ";");
            }
            else {
                out(2, field.getName() + "_ = " + field.getName() + ";");
            }
        }
        out(1, "}");
    }

    public final void compileGetters() {
        for (Field field : dt_.getFields()) {
            out();
            out(1, "public " + getFieldType(field) + " " + methodNameForGetter(field) + "() {");
            out(2, "return " + field.getName() + "_;");
            out(1, "}");
        }
    }

    public String getBuilderFieldType(final Field field) {
        switch (field.getFieldTypeCategory()) {
        case Primitive:
            return getFieldType(field);
        case ImportedType:
            return getFieldType(field);
        case DeclaredType:
            if (isTypeMutable(field.getTypeName())) {
                return getFieldType(field) + ".Builder";
            }
            return getFieldType(field);
        case Set:
            switch (field.getModifier()) {
            case key:
                return getFieldType(field);
            case val:
                return "MutableSet<" + javaTypeName(field.getTypeArgs().get(0)) + ">";
            default:
                throw new RuntimeException("Unexpected enum " + field.getModifier());
            }
        case Map:
            switch (field.getModifier()) {
            case key:
                return getFieldType(field);
            case val:
                final String valueType = isMapValueTypeMutable(field) ? field.getTypeArgs().get(1) + ".Builder" : javaTypeName(field.getTypeArgs().get(1));
                return "MutableMap<" + javaTypeName(field.getTypeArgs().get(0)) + ", " + valueType + ">";
            default:
                throw new RuntimeException("Unexpected enum " + field.getModifier());
            }
        default:
            throw new RuntimeException("Unexpected enum " + field.getFieldTypeCategory());
        }
    }

    public String getDeltaFieldTypeForMap(final Field field) {
        switch (field.getFieldTypeCategory()) {
        case Map:
            final StringBuilder sb = new StringBuilder();
            if (!isMapValueTypeMutable(field)) {
                sb.append("KeyValuePairDelta<");
                sb.append(javaTypeName(field.getTypeArgs().get(0)));
                sb.append(", ");
                sb.append(javaTypeName(field.getTypeArgs().get(1)));
                sb.append(">");
                return sb.toString();
            }
            sb.append("KeyModelDeltaPairDelta<");
            sb.append(javaTypeName(field.getTypeArgs().get(0)));
            sb.append(", ");
            sb.append(CompilerUtil.getKeyFieldType(cu_.getDeclaredTypesMap().get(field.getTypeArgs().get(1))));
            sb.append(", ");
            sb.append(field.getTypeArgs().get(1) + ".Delta");
            sb.append(", ");
            sb.append(field.getTypeArgs().get(1) + ".Delta.Builder");
            sb.append(">");
            return sb.toString();
        default:
            return "";
        }
    }

    public String getDeltaFieldType(final Field field) {
        switch (field.getFieldTypeCategory()) {
        case Primitive:
            return getFieldType(field);
        case ImportedType:
            return getFieldType(field);
        case DeclaredType:
            if (isTypeMutable(field.getTypeName())) {
                return getFieldType(field) + ".Delta";
            }
            return getFieldType(field);
        case Set:
            switch (field.getModifier()) {
            case key:
                return getFieldType(field);
            case val:
                return "ImmutableList<KeyDelta<" + javaTypeName(field.getTypeArgs().get(0)) + ">>";
            default:
                throw new RuntimeException("Unexpected enum " + field.getModifier());
            }
        case Map:
            switch (field.getModifier()) {
            case key:
                return getFieldType(field);
            case val:
                return "ImmutableList<" + getDeltaFieldTypeForMap(field) + ">";
            default:
                throw new RuntimeException("Unexpected enum " + field.getModifier());
            }
        default:
            throw new RuntimeException("Unexpected enum " + field.getFieldTypeCategory());
        }
    }

    public String getDeltaBuilderFieldType(final Field field) {
        switch (field.getFieldTypeCategory()) {
        case Primitive:
            return getFieldType(field);
        case ImportedType:
            return getFieldType(field);
        case DeclaredType:
            if (isTypeMutable(field.getTypeName())) {
                return getFieldType(field) + ".Delta.Builder";
            }
            return getFieldType(field);
        case Set:
            switch (field.getModifier()) {
            case key:
                return getFieldType(field);
            case val:
                return "MutableMap<" + javaTypeName(field.getTypeArgs().get(0)) + ", KeyDelta.Builder<" + javaTypeName(field.getTypeArgs().get(0)) + ">>";
            default:
                throw new RuntimeException("Unexpected enum " + field.getModifier());
            }
        case Map:
            switch (field.getModifier()) {
            case key:
                return getFieldType(field);
            case val:
                final StringBuilder sb = new StringBuilder();
                sb.append("MutableMap<");
                sb.append(javaTypeName(field.getTypeArgs().get(0)));
                if (!isMapValueTypeMutable(field)) {
                    sb.append(", KeyValuePairDelta.Builder<");
                    sb.append(javaTypeName(field.getTypeArgs().get(0)));
                    sb.append(", ");
                    sb.append(javaTypeName(field.getTypeArgs().get(1)));
                    sb.append(">>");
                    return sb.toString();
                }
                sb.append(", KeyModelDeltaPairDelta.Builder<");
                sb.append(javaTypeName(field.getTypeArgs().get(0)));
                sb.append(", ");
                sb.append(CompilerUtil.getKeyFieldType(cu_.getDeclaredTypesMap().get(field.getTypeArgs().get(1))));
                sb.append(", ");
                sb.append(field.getTypeArgs().get(1) + ".Delta");
                sb.append(", ");
                sb.append(field.getTypeArgs().get(1) + ".Delta.Builder");
                sb.append(">>");
                return sb.toString();
            default:
                throw new RuntimeException("Unexpected enum " + field.getModifier());
            }
        default:
            throw new RuntimeException("Unexpected enum " + field.getFieldTypeCategory());
        }
    }

    public final String paramTypeForDeltaBuilderAddMethod(final Field field) {
        switch (field.getFieldTypeCategory()) {
        case Set:
            return "KeyDelta<" + javaTypeName(field.getTypeArgs().getFirst()) + ">";
        case Map:
            if (isMapValueTypeMutable(field)) {
                final DeclaredType dt = cu_.getDeclaredTypesMap().get(field.getTypeArgs().get(1));
                final Field key = getKeyField(dt);
                return "KeyModelDeltaPairDelta<" + javaTypeName(field.getTypeArgs().get(0)) + ", " + key.getJavaTypeName() + ", " + dt.getName() + ".Delta, " + dt.getName() + ".Delta.Builder>";
            }
            return "KeyValuePairDelta<" + javaTypeName(field.getTypeArgs().get(0)) + ", " + javaTypeName(field.getTypeArgs().get(1)) + ">";
        default:
            throw new RuntimeException("Unexpected enum " + field.getFieldTypeCategory());
        }
    }

    public final Predicate<Field> initForUpdateMethodFieldPredicate = new Predicate<Field>() {
        @Override
        public boolean accept(final Field field) {
            if (field.getModifier() != FieldModifier.val) {
                return false;
            }
            switch (field.getFieldTypeCategory()) {
            case DeclaredType:
                return isTypeMutable(field.getTypeName());
            case Map:
            case Set:
                return true;
            default:
                return false;
            }
        }
    };

    public final long serialVersionUID() {
        if (dt_.getDeclaredTypeCategory() == DeclaredTypeCategory.Enum) {
            return 0L;
        }

        try {
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final DataOutputStream dout = new DataOutputStream(bout);

            dout.writeUTF(dt_.getName());

            final List<Field> sortedFields = dt_.getFields().toSortedList(new Comparator<Field>() {
                public int compare(final Field f1, final Field f2) {
                    return f1.getName().compareTo(f2.getName());
                }
            });

            for (final Field field : sortedFields) {
                dout.writeUTF(field.getName());

                // Exclude type parameters
                final String fieldType = CompilerUtil.getFieldType(field);
                final int index = fieldType.indexOf("<");
                if (index < 0) {
                    dout.writeUTF(fieldType);
                }
                else {
                    dout.writeUTF(fieldType.substring(0, index));
                }
            }

            dout.flush();

            MessageDigest md = MessageDigest.getInstance("SHA");
            byte[] hashBytes = md.digest(bout.toByteArray());
            long hash = 0;
            for (int i = Math.min(hashBytes.length, 8) - 1; i >= 0; i--) {
                hash = (hash << 8) | (hashBytes[i] & 0xFF);
            }
            return hash;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
