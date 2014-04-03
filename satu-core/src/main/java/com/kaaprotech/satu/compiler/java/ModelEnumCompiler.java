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

public final class ModelEnumCompiler extends AbstractModelCompiler {

    public ModelEnumCompiler(final CompilationUnit cu, final DeclaredType dt, final PrintWriter writer) {
        super(cu, dt, writer);
    }

    public void compile() {
        compileHeader();
        compilePackage();
        compileClassJavaDoc();
        out("public enum " + dt_.getName() + " {");
        for (int i = 0; i < dt_.getFields().size(); i++) {
            final Field field = dt_.getFields().get(i);
            if (i + 1 < dt_.getFields().size()) {
                out(1, field.getName() + ",");
            }
            else {
                out(1, field.getName() + ";");
            }
        }
        compileClassEnd();
    }
}
