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

import java.io.IOException;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.kaaprotech.satu.antlr4.SatuLexer;
import com.kaaprotech.satu.antlr4.SatuParser;

public final class SatuParserHelper {

    public CompilationUnit parse(final String modelFile, final String encoding, final boolean jsonCompatible) {
        final ANTLRFileStream charStream;
        try {
            charStream = new ANTLRFileStream(modelFile, encoding);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        final SatuLexer lexer = new SatuLexer(charStream);
        final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        final SatuParser parser = new SatuParser(tokenStream);
        final ParserRuleContext tree = parser.compilationUnit();
        final ParseTreeWalker walker = new ParseTreeWalker();
        final SatuListener listener = new SatuListener(jsonCompatible);
        walker.walk(listener, tree);
        return listener.getCompilationUnit();
    }
}
