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

import static com.kaaprotech.satu.compiler.java.CompilerUtil.DBF;
import static com.kaaprotech.satu.compiler.java.CompilerUtil.DBS;
import static com.kaaprotech.satu.compiler.java.CompilerUtil.DT;
import static com.kaaprotech.satu.compiler.java.CompilerUtil.LS;
import static com.kaaprotech.satu.compiler.java.CompilerUtil.REF;

import com.gs.collections.api.block.procedure.Procedure;
import com.gs.collections.api.list.MutableList;
import com.kaaprotech.satu.parser.Field;
import com.kaaprotech.satu.parser.FieldModifier;
import com.kaaprotech.satu.parser.FieldTypeCategory;

@SuppressWarnings("serial")
public final class ModelDeltaBuilderCompiler extends AbstractModelCompiler {

    public ModelDeltaBuilderCompiler(final ModelCompiler compiler) {
        super(compiler.cu_, compiler.dt_, compiler.writer_);
    }

    public void compile() {
        compileDeltaBuilderClassStart();
        compileFieldEnum();
        compileFieldBitSet();
        compileRefFieldMember();
        compileDeltaBuilderFieldMembers();
        compileDeltaBuilderConstructorl();
        compileDeltaBuilderConstructor2();
        compileDeltaBuilderUpdatedMethod();
        compileDeltaBuilderInitForUpdateMethod();
        compileDeltaBuilderIdentityMethods();
        compileDeltaBuilderGettersAndSetters();
        compileDeltaBuilderAddDeltaMethod();
        compileDeltaBuilderSetDeltaTypeMethod();
        compileDeltaBuilderResetDeltaTypeMethod();
        compileDeltaBuilderBuildDeltaMethod();
        compileDeltaBuilderToStringMethod();
        compileDeltaBuilderEqualsMethod();
        compileDeltaBuilderHashCodeMethod();
        compileDeltaBuilderCompareToMethod();
        compileDeltaBuilderClassEnd();
    }

    public void compileDeltaBuilderClassStart() {
        out();
        out(2, "public static final class Builder extends AbstractDeltaBuilder<" + dt_.getName() + ".Delta> implements ModelDeltaBuilder<" + getKeyFieldType() + ", " +
                dt_.getName() + ".Delta>, Comparable<" + dt_.getName() + ".Delta.Builder> {");
    }

    public void compileFieldEnum() {
        final MutableList<Field> fields = dt_.getFields().select(fieldsByModifierPredicate(FieldModifier.val));
        out();
        out(3, "private static enum " + DBF + " {");
        out(4, DT + (fields.notEmpty() ? "," : ""));
        for (int i = 0; i < fields.size(); i++) {
            final Field field = fields.get(i);
            if (i + 1 < fields.size()) {
                out(4, field.getName() + ",");
            }
            else {
                out(4, field.getName());
            }
        }
        out(3, "}");
    }

    public void compileFieldBitSet() {
        out();
        out(3, "private final BitSet " + DBS + " = new BitSet(" + DBF + ".values().length);");
    }

    public void compileRefFieldMember() {
        out();
        out(3, "private final " + dt_.getName() + ".Delta " + REF + ";");
    }

    public void compileDeltaBuilderFieldMembers() {
        dt_.getFields().forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                out();
                final String modifier;
                switch (field.getModifier()) {
                case key:
                    out(3, "// Key");
                    modifier = "private final";
                    break;

                default:
                    modifier = "private";
                    break;
                }
                out(3, modifier + " " + getDeltaBuilderFieldType(field) + " " + field.getName() + "_;");
            }
        });
    }

    public void compileDeltaBuilderConstructorl() {
        final Field kField = getKeyField();
        out();
        out(3, "private Builder(final DeltaType deltaType, final " + getFieldType(kField) + " " + kField.getName() + ") {");
        out(4, "super(deltaType);");
        out(4, REF + " = null;");
        out(4, kField.getName() + "_ = " + kField.getName() + ";");
        out(4, "if (deltaType == DeltaType.DELETE) {");
        out(5, DBS + ".set(" + DBF + "." + DT + ".ordinal());");
        out(4, "}");
        out(3, "}");
    }

    public void compileDeltaBuilderConstructor2() {
        out();
        out(3, "private Builder(final " + dt_.getName() + ".Delta ref) {");
        out(4, "super(ref.getDeltaType());");
        out(4, REF + " = ref;");
        dt_.getFields().reject(initForUpdateMethodFieldPredicate).forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                out(4, field.getName() + "_ = ref." + methodNameForGetter(field) + "();");
            }
        });
        out(3, "}");
    }

    public void compileDeltaBuilderUpdatedMethod() {
        out();
        out(3, "private boolean updated(final " + DBF + "  field) {");
        out(4, "return " + DBS + " == null ? true : " + DBS + ".get(field.ordinal());");
        out(3, "}");
    }

    public void compileDeltaBuilderInitForUpdateMethod() {
        out();
        out(3, "private void initForUpdate(final " + DBF + " field) {");
        out(4, "if (" + DBS + ".get(field.ordinal())) {");
        out(5, "return;");
        out(4, "}");

        out(4, DBS + ".set(field.ordinal());");

        final MutableList<Field> fields = dt_.getFields().select(initForUpdateMethodFieldPredicate);
        if (fields.isEmpty()) {
            out(3, "}");
            return;
        }

        out(4, "switch (field) {");

        fields.forEach(new Procedure<Field>() {
            @SuppressWarnings("incomplete-switch")
            @Override
            public void value(final Field field) {
                out(4, "case  " + field.getName() + ":");
                out(5, "if (" + REF + " == null) {");
                if (field.getFieldTypeCategory() == FieldTypeCategory.DeclaredType) {
                    out(6, field.getName() + "_ = null;");
                }
                else {
                    out(6, field.getName() + "_ = Maps.mutable.of();");
                }
                out(5, "}");
                out(5, "else {");
                switch (field.getFieldTypeCategory()) {
                case DeclaredType:
                    out(6, field.getName() + "_ = " + REF + "." + methodNameForGetter(field) + "().toDeltaBuilder();");
                    break;

                case Set:
                    out(6, field.getName() + "_ = SatuUtil.toKeyDeltaBuilderMap(" + REF + "." + methodNameForGetter(field) + "());");
                    break;

                case Map:
                    if (isMapValueTypeMutable(field)) {
                        out(6, field.getName() + "_ = SatuUtil.toKeyModelDeltaBuilderMap(" + REF + "." + methodNameForGetter(field) + "());");
                    }
                    else {
                        out(6, field.getName() + "_ = SatuUtil.toKeyValuePairDeltaBuilderMap(" + REF + "." + methodNameForGetter(field) + "());");
                    }
                    break;
                }

                out(5, "}");
                out(5, "break;");
            }
        });

        out(4, "}");
        out(3, "}");
    }

    public void compileDeltaBuilderIdentityMethods() {
        out();
        out(3, "@Override");
        out(3, "public " + getKeyFieldType() + " getKey() {");
        out(4, "return " + getKeyField().getName() + "_;");
        out(3, "}");
    }

    public void compileDeltaBuilderGettersAndSetters() {
        dt_.getFields().select(fieldsByModifierPredicate(FieldModifier.key)).forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                out();
                out(3, "public " + getDeltaBuilderFieldType(field) + " " + methodNameForGetter(field) + "() {");
                out(4, "return " + field.getName() + "_;");
                out(3, "}");
            }
        });

        dt_.getFields().select(fieldsByModifierPredicate(FieldModifier.val)).forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                out();
                out(3, "public " + getDeltaBuilderFieldType(field) + " " + methodNameForGetter(field) + "() {");
                if (initForUpdateMethodFieldPredicate.accept(field)) {
                    out(4, "initForUpdate(" + DBF + "." + field.getName() + ");");
                }
                out(4, "return " + field.getName() + "_;");
                out(3, "}");

                switch (field.getFieldTypeCategory()) {
                case DeclaredType:
                case Primitive:
                case ImportedType:
                    out();
                    out(3, "public Delta.Builder " + methodNameForSetter(field) + "(" + getDeltaBuilderFieldType(field) + " " + field.getName() + ") {");
                    out(4, "initForUpdate(" + DBF + "." + field.getName() + ");");
                    out(4, field.getName() + "_ = " + field.getName() + ";");
                    out(4, "return this;");
                    out(3, "}");
                    break;

                case Set:
                    out();
                    out(3, "public Delta.Builder " + methodNameForAdd(field) + "(final " + paramTypeForDeltaBuilderAddMethod(field) + " newDelta) {");
                    out(4, "initForUpdate(" + DBF + "." + field.getName() + ");");
                    out(4, "SatuUtil.addKeyDelta(" + field.getName() + "_, newDelta);");
                    out(4, "return this;");
                    out(3, "}");
                    break;

                case Map:
                    if (isMapValueTypeMutable(field)) {
                        out();
                        out(3, "public Delta.Builder " + methodNameForAdd(field) + "(final " + paramTypeForDeltaBuilderAddMethod(field) + " newDelta) {");
                        out(4, "initForUpdate(" + DBF + "." + field.getName() + ");");
                        out(4, "SatuUtil.addKeyModelDeltaPairDelta(" + field.getName() + "_, newDelta);");
                        out(4, "return this;");
                        out(3, "}");
                    }
                    else {
                        out();
                        out(3, "public Delta.Builder " + methodNameForAdd(field) + "(final " + paramTypeForDeltaBuilderAddMethod(field) + " newDelta) {");
                        out(4, "initForUpdate(" + DBF + "." + field.getName() + ");");
                        out(4, "SatuUtil.addKeyValuePairDelta(" + field.getName() + "_, newDelta);");
                        out(4, "return this;");
                        out(3, "}");
                    }
                }

                out();
                out(3, "public boolean " + methodNameForHas(field) + "() {");
                out(4, "return updated(" + DBF + "." + field.getName() + ") ? true : (" + REF + " != null ? " + REF + "." + methodNameForHas(field) + "() : false);");
                out(3, "}");
            }
        });
    }

    public void compileDeltaBuilderAddDeltaMethod() {
        out();
        out(3, "@Override");
        out(3, "public " + dt_.getName() + ".Delta.Builder addDelta(final " + dt_.getName() + ".Delta delta) {");
        out(4, "if (!getKey().equals(delta.getKey())) {");
        out(5, "throw new RuntimeException(\"Keys don't match \" + getKey() + \" \" + delta.getKey());");
        out(4, "}");

        dt_.getFields().select(fieldsByModifierPredicate(FieldModifier.val)).forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                switch (field.getFieldTypeCategory()) {
                case Primitive:
                case DeclaredType:
                case ImportedType:
                    out();
                    out(4, "if (delta." + methodNameForHas(field) + "())  {");
                    if (field.getFieldTypeCategory() == FieldTypeCategory.DeclaredType && isTypeMutable(field.getTypeName())) {
                        out(5, methodNameForSetter(field) + "(delta." + methodNameForGetter(field) + "().toDeltaBuilder());");
                    }
                    else {
                        out(5, methodNameForSetter(field) + "(delta." + methodNameForGetter(field) + "());");
                    }
                    out(4, "}");
                    break;

                case Set:
                case Map:
                    out();
                    out(4, "if (delta." + methodNameForHas(field) + "()) {");
                    out(5, "delta." + methodNameForGetter(field) + "().forEach(new Procedure<" + paramTypeForDeltaBuilderAddMethod(field) + ">() {");
                    out(6, "private static final long serialVersionUID = 1L;");
                    out(6, "@Override");
                    out(6, "public void value(final " + paramTypeForDeltaBuilderAddMethod(field) + " d) {");
                    out(7, methodNameForAdd(field) + "(d);");
                    out(6, "}");
                    out(5, "});");
                    out(4, "}");
                    break;
                }
            }
        });

        out();
        out(4, "return this;");
        out(3, "}");
    }

    public void compileDeltaBuilderSetDeltaTypeMethod() {
        out();
        out(3, "@Override");
        out(3, "public " + dt_.getName() + ".Delta.Builder setDeltaType(final DeltaType deltaType) {");
        out(4, "if (deltaType != getInitialDeltaType()) {");
        out(5, DBS + ".set(" + DBF + "." + DT + ".ordinal());");
        out(4, "}");
        out(4, "super.setDeltaType(deltaType);");
        out(4, "return this;");
        out(3, "}");
    }

    public void compileDeltaBuilderResetDeltaTypeMethod() {
        out();
        out(3, "@Override");
        out(3, "public " + dt_.getName() + ".Delta.Builder resetDeltaType() {");
        out(4, "super.resetDeltaType();");
        out(4, "return this;");
        out(3, "}");
    }

    @SuppressWarnings("incomplete-switch")
    public void compileDeltaBuilderBuildDeltaMethod() {
        out();
        out(3, "@Override");
        out(3, "public " + dt_.getName() + ".Delta buildDelta() {");
        out(4, "final BitSet flags = (BitSet) " + DBS + ".clone();");
        out();

        out(4, "if (getDeltaType() == DeltaType.UPDATE && flags.nextSetBit(0) < 0) {");
        out(5, "return " + REF + ";");
        out(4, "}");

        dt_.getFields().select(initForUpdateMethodFieldPredicate).forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                switch (field.getFieldTypeCategory()) {
                case DeclaredType:
                    out();
                    out(4, "final " + field.getTypeName() + ".Delta " + field.getName() + ";");
                    out(4, "if (updated(" + DBF + "." + field.getName() + ")) {");
                    out(5, field.getName() + " =  " + field.getName() + "_ != null ? " + field.getName() + "_.buildDelta() : null;");
                    out(4, "}");
                    out(4, "else {");
                    out(5, field.getName() + " = " + REF + " != null ? " + REF + "." + methodNameForGetter(field) + "() : null;");
                    out(4, "}");
                    out(4, "flags.set(" + DBF + "." + field.getName() + ".ordinal(), " + field.getName() + " != null);");
                    break;

                case Set:
                case Map:
                    out();
                    out(4, "final " + getDeltaFieldType(field) + " " + field.getName() + ";");
                    out(4, "if (updated(" + DBF + "." + field.getName() + ")) {");
                    if (field.getFieldTypeCategory() == FieldTypeCategory.Set) {
                        out(5, field.getName() + " = SatuUtil.buildKeyDelta(" + field.getName() + "_.valuesView());");
                    }
                    else if (isTypeMutable(field.getTypeArgs().get(1))) {
                        out(5, field.getName() + " = SatuUtil.buildKeyModelDeltaPairDelta(" + field.getName() + "_.valuesView());");
                    }
                    else {
                        out(5, field.getName() + " = SatuUtil.buildKeyValuePairDelta(" + field.getName() + "_.valuesView());");
                    }
                    out(4, "}");
                    out(4, "else {");
                    out(5, "if (" + REF + " == null) {");
                    out(6, field.getName() + " = Lists.immutable.of();");
                    out(5, "}");
                    out(5, "else {");
                    out(6, field.getName() + " = " + REF + "." + methodNameForGetter(field) + "();");
                    out(5, "}");

                    out(4, "}");

                    out(4, "flags.set(" + DBF + "." + field.getName() + ".ordinal(), " + field.getName() + ".notEmpty());");
                    break;
                }
            }
        });

        out();
        out(4, "if (getDeltaType() == DeltaType.UPDATE && flags.nextSetBit(0) < 0) {");
        out(5, "return " + REF + ";");
        out(4, "}");

        out();
        out(4, "return new " + dt_.getName() + ".Delta(");
        out(6, "getDeltaType(),");
        for (int i = 0; i < dt_.getFields().size(); i++) {
            final Field field = dt_.getFields().get(i);
            final StringBuilder sb = new StringBuilder();
            if (field.getModifier() == FieldModifier.key) {
                sb.append(tab(6) + field.getName() + "_");
            }
            else if (!initForUpdateMethodFieldPredicate.accept(field)) {
                sb.append(tab(6) + field.getName() + "_," + LS);
                sb.append(tab(6) + methodNameForHas(field) + "()");
            }
            else {
                switch (field.getFieldTypeCategory()) {
                case DeclaredType:
                    sb.append(tab(6) + field.getName() + "," + LS);
                    sb.append(tab(6) + field.getName() + " != null");
                    break;
                case Set:
                case Map:
                    sb.append(tab(6) + field.getName() + "," + LS);
                    sb.append(tab(6) + field.getName() + ".notEmpty()");
                    break;
                }
            }
            if (i + 1 < dt_.getFields().size()) {
                sb.append(",");
            }
            else {
                sb.append(");");
            }
            out(sb.toString());
        }
        out(3, "}");
    }

    public void compileDeltaBuilderToStringMethod() {
        out();
        out(3, "@Override");
        out(3, "public String toString() {");
        out(4, "return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)");
        out(6, ".append(\"deltaType\", deltaType_)");
        out(6, ".append(\"delta\", buildDelta())");
        out(6, ".toString();");
        out(3, "}");
    }

    public void compileDeltaBuilderEqualsMethod() {
        out();
        out(3, "@Override");
        out(3, "public boolean equals(final Object obj) {");
        out(4, "if (obj == null) {");
        out(5, "return false;");
        out(4, "}");
        out(4, "if (obj == this) {");
        out(5, "return true;");
        out(4, "}");
        out(4, "if (obj.getClass() != getClass()) {");
        out(5, "return false;");
        out(4, "}");
        out(4, "final " + dt_.getName() + ".Delta.Builder rhs = (" + dt_.getName() + ".Delta.Builder) obj;");
        out(4, "return new EqualsBuilder().append(buildDelta(), rhs.buildDelta()).isEquals();");
        out(3, "}");
    }

    public void compileDeltaBuilderHashCodeMethod() {
        int arg1 = getHashCodeBuilderArg(dt_.getName() + ".Delta.Builder");
        int arg2 = getHashCodeBuilderArg(new StringBuilder(dt_.getName() + ".Delta.Builder").reverse().toString());
        out();
        out(3, "@Override");
        out(3, "public int hashCode() {");
        out(4, "return new HashCodeBuilder(" + arg1 + ", " + arg2 + ").append(buildDelta()).toHashCode();");
        out(3, "}");
    }

    public void compileDeltaBuilderCompareToMethod() {
        out();
        out(3, "@Override");
        out(3, "public int compareTo(final " + dt_.getName() + ".Delta.Builder rhs) {");
        out(4, "return getKey().compareTo(rhs.getKey());");
        out(3, "}");
    }

    public void compileDeltaBuilderClassEnd() {
        out(2, "}");
    }
}
