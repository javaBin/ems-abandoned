package no.java.ems.cli;

import no.java.ems.domain.Session;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.LocalDateTime;

/**
 * @author Trygve Laugstol
 */
public class PrintUtil {
    public static void print(CommandLine commandLine, Session session) {
        boolean parseable = commandLine.hasOption(AbstractCli.OPTION_PARSABLE);

        if(parseable) {
            System.err.println(session.getId() + ":" + formatDate(session.getTimeslot().getStart().toLocalDateTime()) + ":" + session.getTitle());
        }
        else {
            System.err.println("Id:       " + session.getId());
            System.err.println("Lead:     " + session.getLead());
            System.err.println("Date:     " + formatDate(session.getTimeslot().getStart().toLocalDateTime()));
            System.err.println("Notes:    " + session.getNotes());
            System.err.println("Revision: " + session.getRevision());

            System.err.println("Tags");
            for (String tag : session.getTags()) {
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
