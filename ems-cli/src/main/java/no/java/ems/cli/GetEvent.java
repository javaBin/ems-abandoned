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
import no.java.ems.external.v2.*;
import org.apache.commons.cli.*;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class GetEvent extends AbstractCli {
    public GetEvent() {
        super("get-event");
    }

    public static void main(String[] args) throws Exception {
        new GetEvent().doMain(args);
    }

    protected Options addOptions(Options options) {
        options.addOption(null, OPTION_EVENT_URI, true, "The uri of the event to show.");

        return options;
    }

    public void work() throws Exception {
        Option<EventV2> option = Option.join(getEms().getEvent(getDefaultEventHandle()).right().toOption());

        if (option.isNone()) {
            System.err.println("No such event.");
            return;
        }

        EventV2 event = option.some();

        System.err.println("Id: " + event.getUuid());
        System.err.println("Name: " + event.getName());
        System.err.println("Date: " + event.getDate());

        System.err.println("Tags");
        for (String tag : event.getTags().getTag()) {
            System.err.println(" " + tag);
        }
    }
}
