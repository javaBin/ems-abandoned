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

import no.java.ems.client.ResourceHandle;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import no.java.ems.external.v2.SessionV2;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ExportData extends AbstractCli {
    public static String OPTION_SESSIONS_URI = "sessions-uri";
    File dir;


    protected ExportData() {
        super("export-data");
    }

    public static void main(String[] args) throws Exception {
        new ExportData().doMain(args);
    }

    protected Options addOptions(Options options) {
        options.addOption(eventUri);
        options.addOption(null, OPTION_DIRECTORY, true, "The data to import");
        return options;
    }

    protected void work() throws Exception {
        if (!assertIsPresent(OPTION_DIRECTORY)) {
            usage();
            return;
        }

        URI sessionsURI = getOptionAsURI(OPTION_SESSIONS_URI);
        dir = new File(getCommandLine().getOptionValue(OPTION_DIRECTORY));

        List<SessionV2> sessions = getEms().getSessions(new ResourceHandle(sessionsURI)).getSession();

        OutputStream outputStream = null;
        try {
            for (SessionV2 session : sessions) {
                outputStream = new FileOutputStream(new File(dir, session.getUuid()));
            }
        } finally {
            close(outputStream);
        }
    }
}
