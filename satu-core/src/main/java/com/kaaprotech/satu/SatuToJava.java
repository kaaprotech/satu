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

package com.kaaprotech.satu;

import java.io.File;

import com.kaaprotech.satu.compiler.java.Compiler;
import com.kaaprotech.satu.parser.CompilationUnit;
import com.kaaprotech.satu.parser.CompilationUnitValidator;
import com.kaaprotech.satu.parser.SatuParserHelper;
import com.kaaprotech.satu.util.CmdLineUtil;
import com.kaaprotech.satu.util.CmdLineUtil.CommanndLineValues;

public class SatuToJava {

    private final File out_;

    private final SatuParserHelper parser_;

    private final CompilationUnitValidator validator_;

    private final Compiler compiler_;

    public SatuToJava(final String out) {
        out_ = new File(out);
        parser_ = new SatuParserHelper();
        validator_ = new CompilationUnitValidator();
        compiler_ = new Compiler();
    }

    public void generate(final String modelFile, final String encoding) {
        final CompilationUnit cu = parser_.parse(modelFile, encoding);
        validator_.validate(cu);
        compiler_.compile(out_, cu);
    }

    public static void main(String[] args) {
        try {
            final CommanndLineValues cmdLineValues = CmdLineUtil.parseCommanndLineArgs(args);
            System.out.println("Satu Java code generation output directory: " + cmdLineValues.getOutDirectory());
            final StringBuilder builder = new StringBuilder();
            builder.append("Model definition files (" + cmdLineValues.getModelFiles().length + "):");
            for (String modelFile : cmdLineValues.getModelFiles()) {
                builder.append(System.lineSeparator() + "\t" + modelFile);
            }
            System.out.println(builder.toString());

            final SatuToJava modelToJava = new SatuToJava(cmdLineValues.getOutDirectory());
            for (String modelFile : cmdLineValues.getModelFiles()) {
                System.out.println("Starting Satu Java code generation for model file: " + modelFile);
                modelToJava.generate(modelFile, null);
            }

            System.out.println("Satu Java code generation complete for " + cmdLineValues.getModelFiles().length + " model files to output directory: " + cmdLineValues.getOutDirectory());
        }
        catch (Exception e) {
            System.err.println("Satu Java code generation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
