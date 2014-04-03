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

import com.gs.collections.api.stack.MutableStack;
import com.gs.collections.impl.factory.Stacks;
import com.kaaprotech.satu.antlr4.SatuBaseListener;
import com.kaaprotech.satu.antlr4.SatuParser.AnnotationContext;
import com.kaaprotech.satu.antlr4.SatuParser.AnnotationNameContext;
import com.kaaprotech.satu.antlr4.SatuParser.ClassTypeContext;
import com.kaaprotech.satu.antlr4.SatuParser.EnumConstantsContext;
import com.kaaprotech.satu.antlr4.SatuParser.EnumDeclarationContext;
import com.kaaprotech.satu.antlr4.SatuParser.FieldDeclarationContext;
import com.kaaprotech.satu.antlr4.SatuParser.FieldDeclaratorIdContext;
import com.kaaprotech.satu.antlr4.SatuParser.FieldInitializerContext;
import com.kaaprotech.satu.antlr4.SatuParser.FieldModifierContext;
import com.kaaprotech.satu.antlr4.SatuParser.ImportDeclarationContext;
import com.kaaprotech.satu.antlr4.SatuParser.ImportedTypeContext;
import com.kaaprotech.satu.antlr4.SatuParser.KeyClassDeclarationContext;
import com.kaaprotech.satu.antlr4.SatuParser.MapTypeArgumentsContext;
import com.kaaprotech.satu.antlr4.SatuParser.MapTypeContext;
import com.kaaprotech.satu.antlr4.SatuParser.ModelClassDeclarationContext;
import com.kaaprotech.satu.antlr4.SatuParser.PackageDeclarationContext;
import com.kaaprotech.satu.antlr4.SatuParser.PrimitiveTypeContext;
import com.kaaprotech.satu.antlr4.SatuParser.SetTypeArgumentContext;
import com.kaaprotech.satu.antlr4.SatuParser.SetTypeContext;

public class SatuListener extends SatuBaseListener {

    private final CompilationUnit compilationUnit_ = new CompilationUnit();

    private final MutableStack<DeclaredType> typeStack_ = Stacks.mutable.of();

    private final MutableStack<Field> fieldStack_ = Stacks.mutable.of();

    public CompilationUnit getCompilationUnit() {
        return compilationUnit_;
    }

    @Override
    public void enterPackageDeclaration(final PackageDeclarationContext ctx) {
        compilationUnit_.setPackageDeclaration(ctx.children.get(1).getText().trim());
    }

    @Override
    public void enterImportDeclaration(final ImportDeclarationContext ctx) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 1; i < ctx.getChildCount() - 1; i++) {
            builder.append(ctx.getChild(i).getText());
        }
        final ImportDeclaration importDeclaration = new ImportDeclaration();
        importDeclaration.setQualifiedName(builder.toString());
        compilationUnit_.addImportDeclaration(importDeclaration);
    }

    @Override
    public void enterModelClassDeclaration(final ModelClassDeclarationContext ctx) {
        typeStack_.push(new DeclaredType(ctx.getChild(1).toString(), DeclaredTypeCategory.Model));
    }

    @SuppressWarnings("unused")
    @Override
    public void exitModelClassDeclaration(final ModelClassDeclarationContext ctx) {
        compilationUnit_.addDeclaredType(typeStack_.pop());
    }

    @Override
    public void enterKeyClassDeclaration(final KeyClassDeclarationContext ctx) {
        typeStack_.push(new DeclaredType(ctx.getChild(2).toString(), DeclaredTypeCategory.Key));
    }

    @SuppressWarnings("unused")
    @Override
    public void exitKeyClassDeclaration(final KeyClassDeclarationContext ctx) {
        compilationUnit_.addDeclaredType(typeStack_.pop());
    }

    @Override
    public void enterEnumDeclaration(final EnumDeclarationContext ctx) {
        typeStack_.push(new DeclaredType(ctx.getChild(1).toString(), DeclaredTypeCategory.Enum));
    }

    @SuppressWarnings("unused")
    @Override
    public void exitEnumDeclaration(final EnumDeclarationContext ctx) {
        compilationUnit_.addDeclaredType(typeStack_.pop());
    }

    @Override
    public void enterAnnotation(final AnnotationContext ctx) {
        final Annotation annotation = new Annotation();
        final AnnotationNameContext actx = ctx.annotationName();
        annotation.setName(actx.getChild(0).getText());
        if (ctx.getChildCount() > 3) {
            final String text = ctx.getChild(3).getText();
            annotation.setText(text.substring(1, text.length() - 1));
        }
        fieldStack_.peek().addAnnotation(annotation);
    }

    @SuppressWarnings("unused")
    @Override
    public void enterFieldDeclaration(final FieldDeclarationContext ctx) {
        fieldStack_.push(new Field());
    }

    @SuppressWarnings("unused")
    @Override
    public void exitFieldDeclaration(final FieldDeclarationContext ctx) {
        typeStack_.peek().addField(fieldStack_.pop());
    }

    @Override
    public void enterFieldModifier(final FieldModifierContext ctx) {
        fieldStack_.peek().setModifier(FieldModifier.valueOf(ctx.getChild(0).toString()));
    }

    @Override
    public void enterEnumConstants(final EnumConstantsContext ctx) {
        final DeclaredType dt = typeStack_.peek();
        for (int i = 0; i < ctx.getChildCount(); i = i + 2) {
            Field field = new Field();
            field.setModifier(FieldModifier.val);
            field.setFieldTypeCategory(FieldTypeCategory.DeclaredType);
            field.setName(ctx.getChild(i).toString());
            dt.addField(field);
        }
    }

    @Override
    public void enterFieldDeclaratorId(final FieldDeclaratorIdContext ctx) {
        fieldStack_.peek().setName(ctx.getText());
    }

    @Override
    public void enterClassType(final ClassTypeContext ctx) {
        fieldStack_.peek().setFieldTypeCategory(FieldTypeCategory.DeclaredType);
        fieldStack_.peek().setTypeName(ctx.getText());
    }

    @Override
    public void enterPrimitiveType(final PrimitiveTypeContext ctx) {
        fieldStack_.peek().setFieldTypeCategory(FieldTypeCategory.Primitive);
        fieldStack_.peek().setTypeName(ctx.getText());
    }

    @Override
    public void enterImportedType(final ImportedTypeContext ctx) {
        fieldStack_.peek().setFieldTypeCategory(FieldTypeCategory.ImportedType);
        fieldStack_.peek().setTypeName(ctx.getText());
    }

    @Override
    public void enterFieldInitializer(final FieldInitializerContext ctx) {
        fieldStack_.peek().setInitializer(ctx.getText());
    }

    @SuppressWarnings("unused")
    @Override
    public void enterSetType(final SetTypeContext ctx) {
        fieldStack_.peek().setFieldTypeCategory(FieldTypeCategory.Set);
    }

    @Override
    public void enterSetTypeArgument(final SetTypeArgumentContext cfcx) {
        fieldStack_.peek().addTypeArgument(cfcx.getChild(1).getText());
        fieldStack_.push(new Field());
    }

    @SuppressWarnings("unused")
    @Override
    public void exitSetTypeArgument(final SetTypeArgumentContext ctx) {
        fieldStack_.pop();
    }

    @SuppressWarnings("unused")
    @Override
    public void enterMapType(final MapTypeContext ctx) {
        fieldStack_.peek().setFieldTypeCategory(FieldTypeCategory.Map);
    }

    @Override
    public void enterMapTypeArguments(final MapTypeArgumentsContext ctx) {
        fieldStack_.peek().addTypeArgument(ctx.getChild(1).getText());
        fieldStack_.peek().addTypeArgument(ctx.getChild(3).getText());
        fieldStack_.push(new Field());
    }

    @SuppressWarnings("unused")
    @Override
    public void exitMapTypeArguments(final MapTypeArgumentsContext ctx) {
        fieldStack_.pop();
    }
}
