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

package com.kaaprotech.satu.util;

import java.io.PrintWriter;

import com.kaaprotech.satu.compiler.java.Compiler;
import com.kaaprotech.satu.parser.CompilationUnit;
import com.kaaprotech.satu.parser.CompilationUnitValidator;
import com.kaaprotech.satu.parser.SatuParserHelper;

public final class SatuDebug {

    private final SatuParserHelper parser_;

    private final CompilationUnitValidator validator_;

    private final Compiler compiler_;

    public SatuDebug() {
        parser_ = new SatuParserHelper();
        validator_ = new CompilationUnitValidator();
        compiler_ = new Compiler();
    }

    public void debug(final String modelFileName) {
        final String modelFile = ClassLoader.getSystemResource(modelFileName).getPath();
        final CompilationUnit cu = parser_.parse(modelFile, null);
        validator_.validate(cu);
        compiler_.compile(new PrintWriter(System.out), cu);
    }

    public static void main(String[] args) {
        final String modelFileName = args.length > 0 ? args[0] : "SatuTestModel.satu";
        final SatuDebug debug = new SatuDebug();
        debug.debug(modelFileName);
        System.out.println("Debugging complete!!!");
    }
}
