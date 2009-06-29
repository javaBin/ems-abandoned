/*
 * Copyright 2009 JavaBin
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package no.java.ems.server.cli;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.ParseException;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class EmsOptions extends Options {
    public static final String OPTION_HELP = "help";
    public static final String OPTION_HOME = "home";

    private String applicationId;

    public EmsOptions(String applicationId) {
        this.applicationId = applicationId;
        addDefaultValues();
    }

    private void addDefaultValues() {
        addOption(OPTION_HELP, false, "Shows this help screen");
        addOption(OPTION_HOME, true, "Sets the base directory for where EMS should store the data files. Default: " + getDefaultEmsHome());
    }

    public static String getDefaultEmsHome() {
        String defaultEmsHome = System.getProperty("ems.home");
        if (defaultEmsHome != null) {
            return defaultEmsHome;
        }

        return new File(System.getProperty("user.home"), ".ems").getAbsolutePath();
    }

    private void usage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(applicationId, this);
    }

    public EmsCommandLine parse(String[] args) {
        EmsCommandLine commandLine;

        try {
            CommandLineParser parser = new PosixParser();
            commandLine = new EmsCommandLine(parser.parse(this, args));
        } catch (ParseException e) {
            System.err.println(e.getMessage());

            usage();
            return null;
        }

        if (commandLine.hasOption(EmsOptions.OPTION_HELP)) {
            usage();
            return null;
        }

        return commandLine;
    }

    public EmsOptions addEmsOption(String option, boolean hasArg, String description) {
        addOption(option, hasArg, description);

        return this;
    }
}
