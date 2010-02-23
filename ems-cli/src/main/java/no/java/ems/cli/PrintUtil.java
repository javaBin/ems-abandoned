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

import no.java.ems.external.v2.EmsV2F;
import no.java.ems.external.v2.SessionV2;
import org.apache.commons.cli.CommandLine;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author Trygve Laugstol
 */
public class PrintUtil {
    public static void print(CommandLine commandLine, SessionV2 session) {
        boolean parseable = commandLine.hasOption(AbstractCli.OPTION_PARSABLE);

        LocalDateTime start = EmsV2F.toLocalDateTime.f(session.getTimeslot().getStart());

        if(parseable) {
            System.err.println(session.getUuid() + ":" + formatDate(start) + ":" + session.getTitle());
        }
        else {
            System.err.println("Id:       " + session.getUuid());
            System.err.println("Lead:     " + session.getLead());
            System.err.println("Date:     " + formatDate(start));

            System.err.println("Tags");
            for (String tag : session.getTags().getTag()) {
                System.err.println(" " + tag);
            }

            System.err.println("Body");
            System.err.println(session.getBody());
        }
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private static DateTimeFormatter formatter = DateTimeFormat.shortDateTime();

    protected static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "?";
        }

        return dateTime.toString(formatter);
    }
}
