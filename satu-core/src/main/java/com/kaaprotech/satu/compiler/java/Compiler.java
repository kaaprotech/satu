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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.kaaprotech.satu.parser.CompilationUnit;
import com.kaaprotech.satu.parser.DeclaredType;

public final class Compiler {

    public void compile(final File out, final CompilationUnit cu) {
        try {
            final String packagePath = cu.getPackageDeclaration().replace('.', '/');
            final File dir = new File(out.getCanonicalPath() + "/" + packagePath);
            dir.mkdirs();

            for (final DeclaredType dt : cu.getDeclaredTypes()) {
                final File file = new File(dir, dt.getName() + ".java");
                final PrintWriter writer = new PrintWriter(file, "UTF-8");
                try {
                    switch (dt.getDeclaredTypeCategory()) {
                    case Model:
                        ModelCompiler mc = new ModelCompiler(cu, dt, writer);
                        mc.compile();
                        break;

                    case Key:
                        ModelKeyCompiler mkc = new ModelKeyCompiler(cu, dt, writer);
                        mkc.compile();
                        break;

                    case Enum:
                        ModelEnumCompiler mec = new ModelEnumCompiler(cu, dt, writer);
                        mec.compile();
                        break;

                    default:
                        throw new RuntimeException("Unexpected enum " + dt.getDeclaredTypeCategory());
                    }
                }
                finally {
                    writer.flush();
                    writer.close();
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void compile(final PrintWriter writer, final CompilationUnit cu) {
        for (final DeclaredType dt : cu.getDeclaredTypes()) {
            switch (dt.getDeclaredTypeCategory()) {
            case Model:
                ModelCompiler mc = new ModelCompiler(cu, dt, writer);
                mc.compile();
                break;

            case Key:
                ModelKeyCompiler mkc = new ModelKeyCompiler(cu, dt, writer);
                mkc.compile();
                break;

            case Enum:
                ModelEnumCompiler mec = new ModelEnumCompiler(cu, dt, writer);
                mec.compile();
                break;

            default:
                throw new RuntimeException("Unexpected enum " + dt.getDeclaredTypeCategory());
            }
        }
    }
}
