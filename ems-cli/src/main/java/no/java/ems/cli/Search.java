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

import no.java.ems.external.v2.SessionV2;
import org.apache.commons.cli.Options;

import java.util.List;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Search extends AbstractCli {
    public Search() {
        super("search");
    }

    public static void main(String[] args) throws Exception {
        new Search().doMain(args);
    }

    private static final String OPTION_QUERY = "query";

    protected Options addOptions(Options options) {
        options.addOption(eventUri);
        options.addOption(null, OPTION_QUERY, true, "The query");
        return options;
    }

    public void work() throws Exception {
        if (!assertIsPresent(OPTION_EVENT_URI)) {
            usage();
            return;
        }

        String eventId = getCommandLine().getOptionValue(OPTION_EVENT_URI);
        String query = getCommandLine().getOptionValue(OPTION_QUERY);

        /*List<SessionV2> sessions = getEms().searchForSessions(eventId, query).getSession();

        for (SessionV2 session : sessions) {
            System.out.println(session);
        }*/
    }
}
