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

package no.java.ems.cli;

import fj.P;
import static fj.data.Option.some;
import no.java.ems.external.v2.RestletEmsV2Client;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractCli {

    private final String name;

    private static String baseUri = "http://localhost:3000/ems";

    private String username;

    private String password;

    private Options options;

    private CommandLine commandLine;

    private RestletEmsV2Client ems;

    // Standard options
    private static final String OPTION_HELP = "help";
    private static final String OPTION_BASE_URI = "base-uri";
    private static final String OPTION_USERNAME = "username";
    private static final String OPTION_PASSWORD = "password";

    private Option helpOption;
    private Option baseURIOption;
    private Option usernameOption;
    private Option passwordOption;

    // Per-tool options
    protected static final String OPTION_EVENT_ID = "event-id";
    protected static final String OPTION_SESSION_ID = "session-id";
    protected static final String OPTION_DIRECTORY = "directory";
    protected static final String OPTION_PARSABLE = "parsable";

    protected Option eventId;
    protected Option file;
    protected Option parseable;

    protected AbstractCli(String name) {
        this.name = name;
    }

    // -----------------------------------------------------------------------
    // Abstract methods
    // -----------------------------------------------------------------------

    protected abstract void work()
        throws Exception;

    /**
     * Override this to add extra options
     */
    protected Options addOptions(Options defaultOptions) {
        return defaultOptions;
    }

    protected void extraUsage() {
    }

    // -----------------------------------------------------------------------
    // Parameters
    // -----------------------------------------------------------------------

    public static String getBaseUri() {
        return baseUri;
    }

    // -----------------------------------------------------------------------
    // Services
    // -----------------------------------------------------------------------

    public RestletEmsV2Client getEms() {
        return ems;
    }

    // -----------------------------------------------------------------------
    // Utilities
    // -----------------------------------------------------------------------

    protected <T> List<T> notNullList(List<T> list) {
        if (list != null) {
            return list;
        }

        return Collections.emptyList();
    }

    protected CommandLine getCommandLine() {
        return commandLine;
    }

    protected long getLongOptionValue(String optionName) {
        String optionValue = getCommandLine().getOptionValue(optionName);

        if (optionValue == null) {
            return 0;
        }

        return Long.parseLong(optionValue);
    }

    protected boolean assertIsPresent(String optionName) {
        Option[] options = getCommandLine().getOptions();

        for (Option option : options) {
            if (!option.getLongOpt().equals(optionName)) {
                continue;
            }

            String value = option.getValue();

            if (value != null && value.trim().length() > 0) {
                return true;
            } else {
                System.err.println("Missing required value: " + optionName);
                return false;
            }
        }

        System.err.println("Missing required value: " + optionName);

        return false;
    }

    protected String getDefaultEventId() {
        String eventId = getCommandLine().getOptionValue(OPTION_EVENT_ID);

        if (eventId != null) {
            return eventId;
        }

//        eventId = getEms().getEvents().right().value().getEvent().get(0).getId();
//        System.err.println("Event id: " + eventId);

        return eventId;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    protected void doMain(String[] args) throws Exception {

        createOptions();

        options = new Options();
        options.addOption(helpOption);
        options.addOption(baseURIOption);
        options.addOption(usernameOption);
        options.addOption(passwordOption);

        options = addOptions(options);

        try {
            CommandLineParser parser = new PosixParser();
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());

            usage();
            return;
        }

        if (commandLine.hasOption(OPTION_HELP)) {
            usage();
            return;
        }

        baseUri = commandLine.getOptionValue(OPTION_BASE_URI, baseUri);
        username = commandLine.getOptionValue(OPTION_USERNAME, username);
        password = commandLine.getOptionValue(OPTION_PASSWORD, password);

        if (commandLine.hasOption(OPTION_USERNAME)) {
            ems = new RestletEmsV2Client(new InMemoryHttpCache(), baseUri, some(P.p(username, password)));
        }
        else {
            ems = new RestletEmsV2Client(new InMemoryHttpCache(), baseUri);
        }

        work();
    }

    private void createOptions() {
        helpOption = new Option(null, OPTION_HELP, false, "Show this help screen");
        baseURIOption = new Option(null, OPTION_BASE_URI, true, "The base URI of the EMS service.");
        usernameOption = new Option(null, OPTION_USERNAME, true, "Username");
        passwordOption = new Option(null, OPTION_PASSWORD, true, "Password");

        eventId = new Option(null, OPTION_EVENT_ID, true, "The id of the event");
        parseable = new Option(null, OPTION_PARSABLE, false, "Request machine parsable output.");
    }

    protected void usage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(name, options);

        extraUsage();
    }

    protected void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
