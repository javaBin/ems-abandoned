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

import fj.data.*;
import static no.java.ems.cli.PrintUtil.print;

import no.java.ems.client.ResourceHandle;
import no.java.ems.external.v2.*;
import org.apache.commons.cli.Options;

import java.util.List;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ListSessions extends AbstractCli {

    public ListSessions() {
        super("list-sessions");
    }

    public static void main(String[] args) throws Exception {
        new ListSessions().doMain(args);
    }

    protected Options addOptions(Options options) {
        options.addOption(eventUri);
        options.addOption(parseable);
        return options;
    }

    public void work() throws Exception {
        if (!assertIsPresent(OPTION_EVENT_URI)) {
            usage();
            return;
        }

        Either<Exception,SessionListV2> either = getEms().getSessions(getDefaultEventHandle());

        if(either.isLeft()){
            throw either.left().value();
        }

        for (SessionV2 session : either.right().value().getSession()) {
            print(getCommandLine(), session);
        }
    }
}
