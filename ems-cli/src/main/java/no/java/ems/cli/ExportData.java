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

import org.apache.commons.cli.Options;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import no.java.ems.external.v1.SessionV1;
import static no.java.ems.external.v1.EmsV1F.throwException;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ExportData extends AbstractCli {

    File dir;

    protected ExportData() {
        super("export-data");
    }

    public static void main(String[] args) throws Exception {
        new ExportData().doMain(args);
    }

    protected Options addOptions(Options options) {
        options.addOption(eventId);
        options.addOption(null, OPTION_DIRECTORY, true, "The data to import");
        return options;
    }

    protected void work() throws Exception {
        if (!assertIsPresent(OPTION_DIRECTORY)) {
            usage();
            return;
        }

        String eventId = getDefaultEventId();
        dir = new File(getCommandLine().getOptionValue(OPTION_DIRECTORY));

        List<SessionV1> sessions = getEms().getSessions(eventId).getSession();

        OutputStream outputStream = null;
        try {
            for (SessionV1 session : sessions) {
                outputStream = new FileOutputStream(new File(dir, session.getUuid()));
            }
        } finally {
            close(outputStream);
        }
    }
}
