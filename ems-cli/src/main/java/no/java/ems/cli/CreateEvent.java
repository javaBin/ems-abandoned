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
import no.java.ems.external.v2.EventV2;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CreateEvent extends AbstractCli {
    protected CreateEvent() {
        super("create-event");
    }

    public static void main(String[] args) throws Exception {
        new CreateEvent().doMain(args);
    }

    private static final String OPTION_EVENT_NAME = "event-name";

    protected Options addOptions(Options options) {
        options.addOption(null, OPTION_EVENT_NAME, true, "The name of the event to create.");

        return options;
    }

    protected void work() throws Exception {
        EventV2 event = new EventV2();
        event.setName(getCommandLine().getOptionValue(OPTION_EVENT_NAME));
        getEms().addEvent(event);
        System.err.println("event.getId() = " + event.getUuid());
    }
}
