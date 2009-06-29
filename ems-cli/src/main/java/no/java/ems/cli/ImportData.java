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

import fj.data.Option;
import no.java.ems.cli.command.ImportDirectory;
import no.java.ems.external.v1.EventV1;
import org.apache.commons.cli.Options;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ImportData extends AbstractCli {

    protected ImportData() {
        super("import-data");
    }

    public static void main(String[] args) throws Exception {
        new ImportData().doMain(args);
    }

    protected Options addOptions(Options options) {
        options.addOption(null, OPTION_EVENT_ID, true, "The event to import data into");
        options.addOption(null, OPTION_DIRECTORY, true, "The data to import");
        return options;
    }

    protected void work() throws Exception {
        if (!assertIsPresent(OPTION_DIRECTORY)) {
            usage();
            return;
        }

        String eventId = getDefaultEventId();
        File dir = new File(getCommandLine().getOptionValue(OPTION_DIRECTORY));

        if (!dir.isDirectory()) {
            System.err.println("Not a directory: " + dir.getAbsolutePath());
            System.exit(-1);
        }

        Option<EventV1> eventOption = getEms().getEvent(eventId);
        if (eventOption.isNone()) {
            System.err.println("No such event: " + eventId);
        }

        System.out.println("Importing into " + eventOption.some().getName());

        new ImportDirectory(getEms(), eventId, dir).run();
    }
}
