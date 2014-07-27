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

import com.gs.collections.api.block.procedure.Procedure;
import com.kaaprotech.satu.parser.Field;
import com.kaaprotech.satu.parser.FieldModifier;

@SuppressWarnings("serial")
public final class ModelDeltaCompiler extends AbstractModelCompiler {

    public ModelDeltaCompiler(final ModelCompiler compiler) {
        super(compiler.cu_, compiler.dt_, compiler.writer_);
    }

    public void compile() {
        compileDeltaClassStart();
        compileDeltaFieldMembers();
        compileDeltaConstructor();
        compileDeltaIdentityMethods();
        compileDeltaGetters();
        compileDeltaToString();
        compileDeltaEquals();
        compileDeltaHashCode();
        compileDeltaCompareTo();
        compileDeltaToDeltaBuilder();
        compileDeltaToBuilder();
    }

    public void compileDeltaClassStart() {
        out();
        out(1, "public static final class Delta extends AbstractDelta implements ModelDelta<" + getKeyFieldType() + ", " + dt_.getName() + ".Builder, " +
                dt_.getName() + ".Delta.Builder>, Comparable<" + dt_.getName() + ".Delta>, Serializable {");
        out();
        final long serialVersionUID = serialVersionUID();
        out(2, "private static final long serialVersionUID = " + serialVersionUID + "L;");
    }

    public void compileDeltaFieldMembers() {
        dt_.getFields().forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                out();
                if (field.getModifier() == FieldModifier.key) {
                    out(2, "// Key");
                    out(2, "private final " + getDeltaFieldType(field) + " " + field.getName() + "_;");
                    return;
                }
                out(2, "private final " + getDeltaFieldType(field) + " " + field.getName() + "_;");
                out(2, "private final boolean " + methodNameForHas(field) + "_;");
            }
        });
    }

    public void compileDeltaConstructor() {
        out();
        out(2, "private Delta(");
        out(4, "DeltaType deltaType,");
        for (int i = 0; i < dt_.getFields().size(); i++) {
            final Field field = dt_.getFields().get(i);
            final StringBuilder sb = new StringBuilder();
            if (field.getModifier() == FieldModifier.key) {
                sb.append(tab(4) + getDeltaFieldType(field) + " " + field.getName());
            }
            else {
                sb.append(tab(4) + getDeltaFieldType(field) + " " + field.getName() + "," + LS);
                sb.append(tab(4) + "boolean  " + methodNameForHas(field));
            }
            if (i + 1 < dt_.getFields().size()) {
                sb.append(",");
            }
            else {
                sb.append(") {");
            }
            out(sb.toString());
        }

        out(3, "super(deltaType);");
        for (Field field : dt_.getFields()) {
            if (field.getModifier() == FieldModifier.key) {
                out(3, "// Key");
                out(3, field.getName() + "_ = " + field.getName() + ";");
            }
            else {
                out(3, field.getName() + "_ = " + field.getName() + ";");
                out(3, methodNameForHas(field) + "_ = " + methodNameForHas(field) + ";");
            }
        }
        out(2, "}");
    }

    public void compileDeltaIdentityMethods() {
        out();
        out(2, "@Override");
        out(2, "public " + getKeyFieldType() + " getKey() {");
        out(3, "return " + getKeyField().getName() + "_;");
        out(2, "}");
    }

    public void compileDeltaGetters() {
        for (Field field : dt_.getFields()) {
            out();
            out(2, "public " + getDeltaFieldType(field) + " " + methodNameForGetter(field) + "() {");
            out(3, "return " + field.getName() + "_;");
            out(2, "}");

            if (isMapValueTypeMutable(field)) {
                out();
                out(2, "public ImmutableList<" + field.getTypeArgs().get(1) + ".Delta>" + " " + methodNameForGetter(field) + "Flatten() {");
                out(3, "return " + field.getName() + "_.collect(new Function<" + getDeltaFieldTypeForMap(field) + ", " + field.getTypeArgs().get(1) + ".Delta>() {");
                out(4, "private static final long serialVersionUID = 1L;");
                out(4, "@Override");
                out(4, "public " + field.getTypeArgs().get(1) + ".Delta valueOf(final " + getDeltaFieldTypeForMap(field) + " kvpDelta) {");
                out(5, "return kvpDelta.getValue();");
                out(4, "}");
                out(3, "});");
                out(2, "}");
            }

            if (field.getModifier() != FieldModifier.key) {
                out();
                out(2, "public boolean " + methodNameForHas(field) + "() {");
                out(3, "return " + methodNameForHas(field) + "_;");
                out(2, "}");
            }
        }
    }

    public void compileDeltaToString() {
        out();
        out(2, "@Override");
        out(2, "public String toString() {");
        out(3, "return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)");
        out(5, ".append(\"deltaType\", deltaType_)");
        dt_.getFields().forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                out(5, ".append(\"" + field.getName() + "\", " + field.getName() + "_)");
                if (field.getModifier() != FieldModifier.key) {
                    out(5, ".append(\"" + methodNameForHas(field) + "\", " + methodNameForHas(field) + "_)");
                }
            }
        });
        out(5, ".toString();");
        out(2, "}");
    }

    public void compileDeltaEquals() {
        out();
        out(2, "@Override");
        out(2, "public boolean equals(final Object obj) {");

        out(3, "if (obj == null) {");
        out(4, "return false;");
        out(3, "}");

        out(3, "if (obj == this) {");
        out(4, "return true;");
        out(3, "}");

        out(3, "if (obj.getClass() != getClass()) {");
        out(4, "return false;");
        out(3, "}");

        out(3, "final " + dt_.getName() + ".Delta rhs = (" + dt_.getName() + ".Delta) obj;");
        out(3, "return new EqualsBuilder()");

        out(5, ".append(deltaType_, rhs.deltaType_)");
        dt_.getFields().forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                out(5, ".append(" + field.getName() + "_, rhs." + field.getName() + "_)");
                if (field.getModifier() != FieldModifier.key) {
                    out(5, ".append(" + methodNameForHas(field) + "_, rhs." + methodNameForHas(field) + "_)");
                }
            }
        });

        out(5, ".isEquals();");
        out(2, "}");
    }

    public void compileDeltaHashCode() {
        out();
        out(2, "@Override");
        out(2, "public int hashCode() {");
        int arg1 = getHashCodeBuilderArg(dt_.getName() + ".Delta");
        int arg2 = getHashCodeBuilderArg(new StringBuilder(dt_.getName() + ".Delta").reverse().toString());
        out(3, "return new HashCodeBuilder(" + arg1 + ", " + arg2 + ")");

        out(5, ".append(deltaType_)");
        dt_.getFields().forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                out(5, ".append(" + field.getName() + "_)");
                if (field.getModifier() != FieldModifier.key) {
                    out(5, ".append(" + methodNameForHas(field) + "_)");
                }
            }
        });

        out(5, ".toHashCode();");
        out(2, "}");
    }

    public void compileDeltaCompareTo() {
        out();
        out(2, "@Override");
        out(2, "public int compareTo(final " + dt_.getName() + ".Delta rhs) {");
        out(3, "return getKey().compareTo(rhs.getKey());");
        out(2, "}");
    }

    public void compileDeltaToDeltaBuilder() {
        out();
        out(2, "@Override");
        out(2, "public " + dt_.getName() + ".Delta.Builder toDeltaBuilder() {");
        out(3, "return new " + dt_.getName() + ".Delta.Builder(this);");
        out(2, "}");
    }

    public void compileDeltaToBuilder() {
        out();
        out(2, "@Override");
        out(2, "public " + dt_.getName() + ".Builder toBuilder() {");
        out(3, "final " + dt_.getName() + ".Builder builder = new " + dt_.getName() + ".Builder(getKey());");
        out(3, "builder.applyDelta(this);");
        out(3, "return builder;");
        out(2, "}");
    }

    public void compileDeltaClassEnd() {
        out(1, "}");
    }
}
