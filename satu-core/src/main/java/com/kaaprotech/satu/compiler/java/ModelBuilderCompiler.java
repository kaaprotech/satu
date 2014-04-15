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

import static com.kaaprotech.satu.compiler.java.CompilerUtil.BF;
import static com.kaaprotech.satu.compiler.java.CompilerUtil.LDT;
import static com.kaaprotech.satu.compiler.java.CompilerUtil.MBS;
import static com.kaaprotech.satu.compiler.java.CompilerUtil.REF;

import com.gs.collections.api.block.procedure.Procedure;
import com.gs.collections.api.list.MutableList;
import com.kaaprotech.satu.parser.Annotation;
import com.kaaprotech.satu.parser.Field;
import com.kaaprotech.satu.parser.FieldModifier;
import com.kaaprotech.satu.parser.FieldTypeCategory;

@SuppressWarnings("serial")
public final class ModelBuilderCompiler extends AbstractModelCompiler {

    public ModelBuilderCompiler(final ModelCompiler compiler) {
        super(compiler.cu_, compiler.dt_, compiler.writer_);
    }

    public void compile() {
        compileBuilderClassStart();
        compileMutableFieldEnum();
        compileMutableFieldBitSet();
        compileRefFieldMember();
        compileLastDeltaTypeFieldMember();
        compileBuilderFieldMembers();
        compileBuilderConstructorl();
        compileBuilderConstructor2();
        compileBuilderResetMethod();
        compileBuilderUpdatedMethod();
        compileBuilderInitForUpdateMethod();
        compileBuilderInitMethod();
        compileBuilderGettersAndSetters();
        compileBuilderIdentityMethods();
        compileBuilderBuildNoArgMethod();
        compileBuilderBuildMethod();
        compileBuilderBuildEmptyMethod();
        compileBuilderApplyDeltaMethod();
        compileBuilderLastDeltaTypeGetterAndSetter();
        compileBuilderGetRefMethod();
        compileBuilderReconcileMethodNoArgs();
        compileBuilderReconcileMethod();
        compileBuilderReconcileBuilderArgMethod();
        compileBuilderToDeltaMethod();
        compileBuilderToStringMethod();
        compileBuilderEqualsMethod();
        compileBuilderHashCodeMethod();
        compileBuilderCompareToMethod();
        compileBuilderClassEnd();
    }

    public void compileBuilderClassStart() {
        out();
        out(1, "public static final class Builder implements ModelBuilder<" + getKeyFieldType() + ", " + dt_.getName() + ", " + dt_.getName() + ".Delta>, Comparable<" + dt_.getName() + ".Builder> {");
    }

    public void compileMutableFieldEnum() {
        final MutableList<Field> fields = dt_.getFields().select(fieldsByModifierPredicate(FieldModifier.val));
        out();
        out(2, "private static enum " + BF + " {");

        for (int i = 0; i < fields.size(); i++) {
            final Field field = fields.get(i);
            if (i + 1 < fields.size()) {
                out(3, field.getName() + ",");
            }
            else {
                out(3, field.getName());
            }
        }
        out(2, "}");
    }

    public void compileMutableFieldBitSet() {
        out();
        out(2, "private final BitSet " + MBS + " = new BitSet(" + BF + ".values().length);");
    }

    public void compileRefFieldMember() {
        out();
        out(2, "private final " + dt_.getName() + " " + REF + ";");
    }

    public void compileLastDeltaTypeFieldMember() {
        out();
        out(2, "private DeltaType " + LDT + ";");
    }

    public void compileBuilderFieldMembers() {
        dt_.getFields().forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                out();
                final String modifier;
                if (field.getModifier() == FieldModifier.key) {
                    out(2, "// Key");
                    modifier = "private final";
                }
                else {
                    modifier = "private";
                }
                for (Annotation annotation : field.getAnnotations()) {
                    if (annotation.getText() == null) {
                        out(2, "@" + annotation.getName());
                    }
                    else {
                        out(2, "@" + annotation.getName() + "(" + annotation.getText() + ")");
                    }
                }
                out(2, modifier + " " + getBuilderFieldType(field) + " " + field.getName() + "_;");
            }
        });
    }

    public void compileBuilderConstructorl() {
        final Field field = getKeyField();
        out();
        out(2, "private Builder(" + getFieldType(field) + " " + field.getName() + ")  {");
        out(3, REF + " = null;");
        out(3, field.getName() + "_ = " + field.getName() + ";");
        out(3, "reset();");
        out(2, "}");
    }

    public void compileBuilderConstructor2() {
        out();
        out(2, "private Builder(" + dt_.getName() + " ref) {");
        out(3, REF + " = ref;");
        dt_.getFields().reject(initForUpdateMethodFieldPredicate).forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                out(3, field.getName() + "_ = ref." + methodNameForGetter(field) + "();");
            }
        });
        out(2, "}");
    }

    public void compileBuilderResetMethod() {
        out();
        out(2, "private void reset() {");
        dt_.getFields().select(fieldsByModifierPredicate(FieldModifier.val)).forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                switch (field.getFieldTypeCategory()) {
                case Primitive:
                    out(3, field.getName() + "_ = " + getPrimitiveWrapperInitializer(field) + ";");
                    break;
                case ImportedType:
                    out(3, field.getName() + "_ = null;");
                    break;
                case Set:
                    out(3, field.getName() + "_ = Sets.mutable.of();");
                    break;
                case Map:
                    out(3, field.getName() + "_ = Maps.mutable.of();");
                    break;
                default:
                    if (field.getInitializer() != null && isEnum(field)) {
                        out(3, field.getName() + "_ = " + field.getTypeName() + "." + field.getInitializer() + ";");
                    }
                    else {
                        out(3, field.getName() + "_ = null;");
                    }
                    break;
                }
                out(3, MBS + ".set(" + BF + "." + field.getName() + ".ordinal());");
            }
        });
        out(2, "}");
    }

    public void compileBuilderUpdatedMethod() {
        out();
        out(2, "private boolean updated(final " + BF + " field) {");
        out(3, "return " + MBS + " == null ? true : " + MBS + ".get(field.ordinal());");
        out(2, "}");
    }

    public void compileBuilderInitMethod() {
        out();
        out(2, "/**");
        out(2, "* Fully initialize the builder object graph");
        out(2, "*/");
        out(2, "public " + dt_.getName() + ".Builder init() {");
        out(3, "for (int i = 0; i < " + BF + ".values().length; i++) {");
        out(4, "initForUpdate(" + BF + ".values()[i]);");
        out(3, "}");

        final MutableList<Field> fields = dt_.getFields().select(initForUpdateMethodFieldPredicate);
        fields.forEach(new Procedure<Field>() {
            @SuppressWarnings("incomplete-switch")
            @Override
            public void value(final Field field) {
                switch (field.getFieldTypeCategory()) {
                case DeclaredType:
                    out();
                    out(3, "if (" + field.getName() + "_ != null) {");
                    out(4, field.getName() + "_.init();");
                    out(3, "}");
                    break;
                case Map:
                    if (isMapValueTypeMutable(field)) {
                        out();
                        out(3, field.getName() + "_.forEachValue(new Procedure<" + field.getTypeArgs().get(1) + ".Builder>() {");
                        out(4, "private static final long serialVersionUID = 1L;");
                        out(4, "@Override");
                        out(4, "public void value(final " + field.getTypeArgs().get(1) + ".Builder builder) {");
                        out(5, "if (builder != null) {");
                        out(6, "builder.init();");
                        out(5, "}");
                        out(4, "}");
                        out(3, "});");
                    }
                    break;
                }
            }
        });

        out();
        out(3, "return this;");
        out(2, "}");
    }

    public void compileBuilderInitForUpdateMethod() {
        out();
        out(2, "private void initForUpdate(final " + BF + " field) {");
        out(3, "if (" + MBS + ".get(field.ordinal())) {");
        out(4, "return;");
        out(3, "}");

        out(3, MBS + ".set(field.ordinal());");

        final MutableList<Field> fields = dt_.getFields().select(initForUpdateMethodFieldPredicate);
        if (fields.isEmpty()) {
            out(2, "}");
            return;
        }

        out(3, "switch (field) {");
        fields.forEach(new Procedure<Field>() {
            @SuppressWarnings("incomplete-switch")
            @Override
            public void value(final Field field) {
                out(3, "case " + field.getName() + ":");

                switch (field.getFieldTypeCategory()) {
                case DeclaredType:
                    out(4, "if (" + REF + " == null || " + REF + "." + methodNameForGetter(field) + "() == null) {");
                    out(5, field.getName() + "_ = null;");
                    break;

                case Map:
                case Set:
                    out(4, "if (" + REF + " == null) {");
                    out(5, field.getName() + "_ = " + getMutableColOf(field) + ";");
                    break;
                }

                out(4, "}");
                out(4, "else {");

                switch (field.getFieldTypeCategory()) {
                case DeclaredType:
                    out(5, field.getName() + "_ = " + REF + "." + methodNameForGetter(field) + "().toBuilder();");
                    break;

                case Map:
                case Set:
                    if (isMapValueTypeMutable(field)) {
                        out(5, field.getName() + "_ = SatuUtil.toKeyModelBuilderMap(" + REF + "." + methodNameForGetter(field) + "());");
                    }
                    else {
                        out(5, field.getName() + "_ = " + REF + "." + methodNameForGetter(field) + "()" + getMutableColConv(field) + ";");
                    }
                    break;
                }

                out(4, "}");
                out(4, "break;");
            }
        });

        out(3, "}");
        out(2, "}");
    }

    public void compileBuilderGettersAndSetters() {
        dt_.getFields().select(fieldsByModifierPredicate(FieldModifier.key)).forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                out();
                out(2, "public " + getBuilderFieldType(field) + " " + methodNameForGetter(field) + "() {");
                out(3, "return " + field.getName() + "_;");
                out(2, "}");
            }
        });

        dt_.getFields().select(fieldsByModifierPredicate(FieldModifier.val)).forEach(new Procedure<Field>() {
            @SuppressWarnings("incomplete-switch")
            @Override
            public void value(final Field field) {
                out();
                out(2, "public " + getBuilderFieldType(field) + " " + methodNameForGetter(field) + "() {");
                if (initForUpdateMethodFieldPredicate.accept(field)) {
                    out(3, "initForUpdate(" + BF + "." + field.getName() + ");");
                }
                out(3, "return " + field.getName() + "_;");
                out(2, "}");

                switch (field.getFieldTypeCategory()) {
                case DeclaredType:
                case Primitive:
                case ImportedType:
                    out();
                    out(2, "public " + dt_.getName() + ".Builder " + methodNameForSetter(field) + "(" + getBuilderFieldType(field) + " " + field.getName() + ") {");
                    out(3, "initForUpdate(" + BF + "." + field.getName() + ");");
                    out(3, field.getName() + "_ = " + field.getName() + ";");
                    out(3, "return this;");
                    out(2, "}");
                    break;
                }

                if (field.getFieldTypeCategory() == FieldTypeCategory.Set) {
                    out();
                    out(2, "public " + dt_.getName() + ".Builder " + methodNameForAdd(field) + "(final " + javaTypeName(field.getTypeArgs().get(0)) + " element) {");
                    out(3, methodNameForGetter(field) + "().add(element);");
                    out(3, "return this;");
                    out(2, "}");
                }

                if (field.getFieldTypeCategory() == FieldTypeCategory.Map && !isMapValueTypeMutable(field)) {
                    out();
                    out(2, "public " + dt_.getName() + ".Builder " + methodNameForPut(field) + "(final " + javaTypeName(field.getTypeArgs().get(0)) + " key, final " + javaTypeName(field.getTypeArgs().get(1)) + " value) {");
                    out(3, methodNameForGetter(field) + "().put(key, value);");
                    out(3, "return this;");
                    out(2, "}");
                }

                if (isMapValueTypeMutable(field)) {
                    out();
                    out(2, "public " + dt_.getName() + ".Builder " + methodNameForPut(field) + "(final " + javaTypeName(field.getTypeArgs().get(0)) + " key, final " + field.getTypeArgs().get(1) + ".Builder builder) {");
                    out(3, methodNameForGetter(field) + "().put(key, builder);");
                    out(3, "return this;");
                    out(2, "}");
                }

                if (isMapKeySameAsMutableValueTypeKey(field)) {
                    out();
                    out(2, "public " + dt_.getName() + ".Builder " + methodNameForAddOrReplace(field) + "(final " + field.getTypeArgs().get(1) + ".Builder builder) {");
                    out(3, methodNameForGetter(field) + "().put(builder.getKey(), builder);");
                    out(3, "return this;");
                    out(2, "}");
                }
            }
        });
    }

    public void compileBuilderIdentityMethods() {
        out();
        out(2, "@Override");
        out(2, "public " + getKeyFieldType() + " getKey() {");
        out(3, "return " + getKeyField().getName() + "_;");
        out(2, "}");
    }

    public void compileBuilderBuildNoArgMethod() {
        out();
        out(2, "@Override");
        out(2, "public " + dt_.getName() + " build() {");
        out(3, "return build(" + LDT + ");");
        out(2, "}");
    }

    public void compileBuilderBuildMethod() {
        out();
        out(2, "private " + dt_.getName() + " build(final DeltaType lastDeltaType) {");
        out(3, "if (" + MBS + " != null && " + MBS + ".nextSetBit(0) < 0) {");
        out(4, "return " + REF + ";");
        out(3, "}");

        out();
        out(3, "if (lastDeltaType == DeltaType.DELETE) {");
        out(4, "return null;");
        out(3, "}");

        dt_.getFields().select(initForUpdateMethodFieldPredicate).forEach(new Procedure<Field>() {
            @SuppressWarnings("incomplete-switch")
            @Override
            public void value(final Field field) {
                switch (field.getFieldTypeCategory()) {
                case DeclaredType: {
                    out();
                    out(3, "final " + field.getTypeName() + " " + field.getName() + ";");
                    out(3, "if (updated(" + BF + "." + field.getName() + ")) {");
                    out(4, field.getName() + " = " + field.getName() + "_ == null ? null : " + field.getName() + "_.build();");
                    out(3, "}");
                    out(3, "else {");
                    out(4, field.getName() + " = " + REF + " == null ? null : " + REF + "." + methodNameForGetter(field) + "();");
                    out(3, "}");
                    break;
                }

                case Set:
                case Map:
                    out();
                    out(3, "final " + getImmutableColType(field) + " " + field.getName() + ";");
                    out(3, "if (updated(" + BF + "." + field.getName() + ")) {");
                    if (!isMapValueTypeMutable(field)) {
                        out(4, field.getName() + " = " + field.getName() + "_.toImmutable();");
                    }
                    else {
                        out(4, field.getName() + " = SatuUtil.toKeyModelMap(" + field.getName() + "_);");
                    }
                    out(3, "}");
                    out(3, "else {");
                    out(4, "if (" + REF + " == null) {");
                    out(5, field.getName() + " = " + (field.getFieldTypeCategory() == FieldTypeCategory.Set ? "Sets.immutable.of()" : "Maps.immutable.of()") + ";");
                    out(4, "}");
                    out(4, "else {");
                    out(5, field.getName() + " = " + REF + "." + methodNameForGetter(field) + "();");
                    out(4, "}");
                    out(3, "}");
                    break;
                }
            }
        });

        out();
        out(3, "return new " + dt_.getName() + "(");
        for (int i = 0; i < dt_.getFields().size(); i++) {
            final Field field = dt_.getFields().get(i);
            final StringBuilder sb = new StringBuilder();
            sb.append(field.getName());
            if (!initForUpdateMethodFieldPredicate.accept(field)) {
                sb.append("_");
            }
            if (i + 1 < dt_.getFields().size()) {
                sb.append(",");
            }
            else {
                sb.append(");");
            }
            out(4, sb.toString());
        }
        out(2, "}");
    }

    public void compileBuilderBuildEmptyMethod() {
        out();
        out(2, "@Override");
        out(2, "public " + dt_.getName() + " buildEmpty() {");
        out(3, "return new " + dt_.getName() + ".Builder(getKey()).build();");
        out(2, "}");
    }

    public void compileBuilderApplyDeltaMethod() {
        out();
        out(2, "@Override");
        out(2, "public " + dt_.getName() + ".Builder applyDelta(final " + dt_.getName() + ".Delta delta) {");

        out(3, "if (!getKey().equals(delta.getKey())) {");
        out(4, "throw new RuntimeException(\"Keys don't match \" + getKey() + \" \" + delta.getKey());");
        out(3, "}");

        out();
        out(3, LDT + " = delta.getDeltaType();");

        out();
        out(3, "if (delta.getDeltaType() == DeltaType.DELETE) {");
        out(4, "reset();");
        out(4, "return this;");
        out(3, "}");

        dt_.getFields().select(fieldsByModifierPredicate(FieldModifier.val)).forEach(new Procedure<Field>() {
            @SuppressWarnings("incomplete-switch")
            @Override
            public void value(final Field field) {
                out();
                out(3, "if (delta." + methodNameForHas(field) + "()) {");
                out(4, "initForUpdate(" + BF + "." + field.getName() + ");");

                if (!initForUpdateMethodFieldPredicate.accept(field)) {
                    out(4, field.getName() + "_ = delta." + methodNameForGetter(field) + "();");
                }
                else {
                    switch (field.getFieldTypeCategory()) {
                    case DeclaredType:
                        out(4, field.getName() + "_ = SatuUtil.applyModelDelta(delta." + methodNameForGetter(field) + "(), " + field.getName() + "_);");
                        break;

                    case Set:
                        out(4, "SatuUtil.applyKeyDeltas(delta." + methodNameForGetter(field) + "(), " + field.getName() + "_);");
                        break;

                    case Map:
                        if (isMapValueTypeMutable(field)) {
                            out(4, "SatuUtil.applyKeyModelPairDeltas(delta." + methodNameForGetter(field) + "(), " + field.getName() + "_);");
                        }
                        else {
                            out(4, "SatuUtil.applyKeyValuePairDeltas(delta." + methodNameForGetter(field) + "(), " + field.getName() + "_);");
                        }
                        break;
                    }
                }
                out(3, "}");
            }
        });

        out();
        out(3, "return this;");
        out(2, "}");
    }

    public void compileBuilderLastDeltaTypeGetterAndSetter() {
        out();
        out(2, "@Override");
        out(2, "public DeltaType getLastDeltaType() {");
        out(3, "return " + LDT + ";");
        out(2, "}");
        out();
        out(2, "@Override");
        out(2, "public " + dt_.getName() + ".Builder setLastDeltaType(final DeltaType lastDeltaType) {");
        out(3, LDT + " = lastDeltaType;");
        out(3, "return this;");
        out(2, "}");
    }

    public void compileBuilderGetRefMethod() {
        out();
        out(2, "@Override");
        out(2, "public " + dt_.getName() + " getRef() {");
        out(3, "return " + REF + ";");
        out(2, "}");
    }

    public void compileBuilderReconcileMethodNoArgs() {
        out();
        out(2, "@Override");
        out(2, "public " + dt_.getName() + ".Delta reconcile() {");
        out(3, "final DeltaType deltaType = " + LDT + " == DeltaType.DELETE ? DeltaType.DELETE : (" + REF + " == null ? DeltaType.ADD : DeltaType.UPDATE);");
        out(3, "final " + dt_.getName() + " ref = " + REF + " == null ? buildEmpty() : " + REF + ";");
        out(3, "return reconcile(deltaType, ref);");
        out(2, "}");
    }

    public void compileBuilderReconcileMethod() {
        out();
        out(2, "@Override");
        out(2, "public " + dt_.getName() + ".Delta reconcile(final DeltaType deltaType, final " + dt_.getName() + " ref) {");
        out(3, "final " + dt_.getName() + ".Delta.Builder deltaBuilder = new " + dt_.getName() + ".Delta.Builder(deltaType, getKey());");
        out(3, "reconcile(deltaBuilder, ref);");
        out(3, "return deltaBuilder.buildDelta();");
        out(2, "}");
    }

    public void compileBuilderReconcileBuilderArgMethod() {
        out();
        out(2, "public " + dt_.getName() + ".Delta.Builder reconcile(final " + dt_.getName() + ".Delta.Builder deltaBuilder, final " + dt_.getName() + " ref) {");
        out(3, "if (!getKey().equals(deltaBuilder.getKey())) {");
        out(4, "throw new RuntimeException(\"Builder key doesn't match \" + getKey() + \" \" + deltaBuilder.getKey());");
        out(3, "}");

        out();
        out(3, "if (!getKey().equals(ref.getKey())) {");
        out(4, "throw new RuntimeException(\"Reference key doesn't match \" + getKey() + \" \" + ref.getKey());");
        out(3, "}");

        dt_.getFields().select(fieldsByModifierPredicate(FieldModifier.val)).forEach(new Procedure<Field>() {
            @SuppressWarnings("incomplete-switch")
            @Override
            public void value(final Field field) {
                out();
                if (!initForUpdateMethodFieldPredicate.accept(field)) {
                    out(3, "if (!ObjectUtils.equals(" + field.getName() + "_, ref." + methodNameForGetter(field) + "())) {");
                    out(4, "deltaBuilder." + methodNameForSetter(field) + "(" + field.getName() + "_);");
                    out(3, "}");
                }
                else {
                    switch (field.getFieldTypeCategory()) {
                    case DeclaredType:
                        out(3, "if (updated(" + BF + "." + field.getName() + "))  {");
                        out(4, "SatuUtil.reconcileModelBuilderField(ref." + methodNameForGetter(field) + "(), " + field.getName() + "_, deltaBuilder." + methodNameForGetter(field) + "(),");
                        out(6, "new DeltaBuilderSetter<" + field.getTypeName() + ".Delta.Builder>() {");
                        out(7, "@Override");
                        out(7, "public void set(final " + field.getTypeName() + ".Delta.Builder db) {");
                        out(8, "deltaBuilder." + methodNameForSetter(field) + "(db);");
                        out(7, "}");
                        out(4, "});");
                        out(3, "}");
                        out(3, "else {");
                        out(4, "SatuUtil.reconcileModelField(ref." + methodNameForGetter(field) + "(), " + REF + "." + methodNameForGetter(field) + "(), deltaBuilder." + methodNameForGetter(field) + "(),");
                        out(6, "new DeltaBuilderSetter<" + field.getTypeName() + ".Delta.Builder>() {");
                        out(7, "@Override");
                        out(7, "public void set(final " + field.getTypeName() + ".Delta.Builder db) {");
                        out(8, "deltaBuilder." + methodNameForSetter(field) + "(db);");
                        out(7, "}");
                        out(4, "});");
                        out(3, "}");
                        break;

                    case Set:
                        out(3, "SatuUtil.reconcileKeys(ref." + methodNameForGetter(field) + "(), updated(" + BF + "." + field.getName() + ") ? " + field.getName() + "_ : " + REF + "." + methodNameForGetter(field) + "(),");
                        out(5, " new DeltaAppender<KeyDelta<" + javaTypeName(field.getTypeArgs().getFirst()) + ">>() {");
                        out(6, "@Override");
                        out(6, "public void append(final " + paramTypeForDeltaBuilderAddMethod(field) + " newDelta) {");
                        out(7, "deltaBuilder." + methodNameForAdd(field) + "(newDelta);");
                        out(6, "}");
                        out(5, "});");
                        break;

                    case Map:
                        if (isMapValueTypeMutable(field)) {
                            out(3, "if (updated(" + BF + "." + field.getName() + ")) {");
                            out(4, "SatuUtil.reconcileKeyModelBuilderPairs(ref." + methodNameForGetter(field) + "(), " + field.getName() + "_,");
                            out(6, "new DeltaAppender<" + paramTypeForDeltaBuilderAddMethod(field) + ">() {");
                            out(7, "@Override");
                            out(7, "public void append(final " + paramTypeForDeltaBuilderAddMethod(field) + " newDelta) {");
                            out(8, "deltaBuilder." + methodNameForAdd(field) + "(newDelta);");
                            out(7, "}");
                            out(6, "});");
                            out(3, "}");
                            out(3, "else {");
                            out(4, "SatuUtil.reconcileKeyModelPairs(ref." + methodNameForGetter(field) + "(), " + REF + "." + methodNameForGetter(field) + "(),");
                            out(6, "new DeltaAppender<" + paramTypeForDeltaBuilderAddMethod(field) + ">() {");
                            out(7, "@Override");
                            out(7, "public void append(final " + paramTypeForDeltaBuilderAddMethod(field) + " newDelta) {");
                            out(8, "deltaBuilder." + methodNameForAdd(field) + "(newDelta);");
                            out(7, "}");
                            out(6, "});");
                            out(3, "}");
                        }
                        else {
                            out(3, "SatuUtil.reconcileKeyValuePairs(ref." + methodNameForGetter(field) + "(), updated(" + BF + "." + field.getName() + ") ? " + field.getName() + "_ : " + REF + "." + methodNameForGetter(field) + "(),");
                            out(5, "new DeltaAppender<" + paramTypeForDeltaBuilderAddMethod(field) + ">() {");
                            out(6, "@Override");
                            out(6, "public void append(final " + paramTypeForDeltaBuilderAddMethod(field) + " newDelta) {");
                            out(7, "deltaBuilder." + methodNameForAdd(field) + "(newDelta);");
                            out(6, "}");
                            out(5, "});");
                        }
                        break;
                    }
                }
            }
        });
        out();
        out(3, "return deltaBuilder;");
        out(2, "}");

    }

    public void compileBuilderToDeltaMethod() {
        out();
        out(2, "@Override");
        out(2, "public " + dt_.getName() + ".Delta toDelta(final DeltaType deltaType) {");
        out(3, "switch (deltaType) {");
        out(3, "case ADD:");
        out(3, "case UPDATE:");
        out(4, "return reconcile(deltaType, buildEmpty());");
        out(3, "case DELETE:");
        out(4, "return new " + dt_.getName() + ".Builder(getKey()).reconcile(deltaType, build());");
        out(3, "}");
        out(3, "throw new RuntimeException(\"Invalid delta type \" + deltaType);");
        out(2, "}");
    }

    public void compileBuilderToStringMethod() {
        out();
        out(2, "@Override");
        out(2, "public String toString() {");
        out(3, "return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)");
        out(5, ".append(\"lastDeltaType\", " + LDT + ")");
        out(5, ".append(\"model\", build(null))");
        out(5, ".toString();");
        out(2, "}");
    }

    public void compileBuilderEqualsMethod() {
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
        out(3, "final " + dt_.getName() + ".Builder lhs = this;");
        out(3, "final " + dt_.getName() + ".Builder rhs = (" + dt_.getName() + ".Builder) obj;");
        out(3, "if (lhs." + LDT + " == DeltaType.DELETE || rhs." + LDT + " == DeltaType.DELETE) {");
        out(4, "return lhs." + LDT + " == rhs." + LDT + " && lhs.build(null).equals(rhs.build(null));");
        out(3, "}");
        out(3, "return lhs.build(null).equals(rhs.build(null));");
        out(2, "}");
    }

    public void compileBuilderHashCodeMethod() {
        final int arg1 = getHashCodeBuilderArg(dt_.getName() + ".Builder");
        final int arg2 = getHashCodeBuilderArg(new StringBuilder(dt_.getName() + ".Builder").reverse().toString());
        out();
        out(2, "@Override");
        out(2, "public int hashCode() {");
        out(3, "return new HashCodeBuilder(" + arg1 + ", " + arg2 + ").append(build(null)).toHashCode();");
        out(2, "}");
    }

    public void compileBuilderCompareToMethod() {
        out();
        out(2, "@Override");
        out(2, "public int compareTo(final " + dt_.getName() + ".Builder rhs) {");
        out(3, "return getKey().compareTo(rhs.getKey());");
        out(2, "}");
    }

    public void compileBuilderClassEnd() {
        out(1, "}");
    }
}
