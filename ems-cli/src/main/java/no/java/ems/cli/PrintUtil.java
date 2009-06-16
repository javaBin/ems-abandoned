package no.java.ems.cli;

import no.java.ems.external.v1.EmsV1F;
import no.java.ems.external.v1.SessionV1;
import org.apache.commons.cli.CommandLine;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author Trygve Laugstol
 */
public class PrintUtil {
    public static void print(CommandLine commandLine, SessionV1 session) {
        boolean parseable = commandLine.hasOption(AbstractCli.OPTION_PARSABLE);

        LocalDateTime start = EmsV1F.toLocalDateTime.f(session.getTimeslot().getStart());

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
