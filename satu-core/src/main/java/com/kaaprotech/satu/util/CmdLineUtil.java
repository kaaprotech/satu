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
import java.io.StringWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public final class CmdLineUtil {

    public static final String IN = "in";

    public static final String OUT = "out";

    public static String getCommandLineUsageMessage() {
        final HelpFormatter formatter = new HelpFormatter();
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        pw.println();
        formatter.printHelp(pw, 120, "Satu", null, getOptions(), 2, 10, null);
        return sw.getBuffer().toString();
    }

    public static CommanndLineValues parseCommanndLineArgs(String[] args) {
        final CommandLine commandLine;
        try {
            final CommandLineParser parser = new GnuParser();
            commandLine = parser.parse(getOptions(), args);
        }
        catch (ParseException e) {
            System.err.println("Satu: Failed to parse cammand line arguments: " + e.getMessage());
            throw new RuntimeException(e);
        }

        final String[] in = commandLine.getOptionValues(IN);
        final String out = commandLine.getOptionValue(OUT);
        return new CommanndLineValuesImpl(in, out);
    }

    @SuppressWarnings("static-access")
    private static Options getOptions() {
        final Options options = new Options();
        OptionBuilder.create("init");
        final Option in = OptionBuilder.withArgName(IN).hasArgs().withDescription("One or more model files").isRequired().create(IN);
        options.addOption(in);
        final Option out = OptionBuilder.withArgName(OUT).hasArg().withDescription("Output root directory").isRequired().create(OUT);
        options.addOption(out);
        return options;
    }

    private static final class CommanndLineValuesImpl implements CommanndLineValues {

        private final String[] modelFiles_;

        private final String outDirectory_;

        public CommanndLineValuesImpl(String[] modelFiles, String outDirectory) {
            modelFiles_ = modelFiles;
            outDirectory_ = outDirectory;
        }

        public String[] getModelFiles() {
            return modelFiles_;
        }

        public String getOutDirectory() {
            return outDirectory_;
        }
    }

    public interface CommanndLineValues {

        String[] getModelFiles();

        String getOutDirectory();
    }
}
