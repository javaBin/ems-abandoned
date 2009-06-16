package no.java.ems.cli;

import no.java.ems.domain.Event;
import org.apache.commons.cli.Options;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class GetEvent extends AbstractCli {
    public GetEvent() {
        super("get-event");
    }

    private static final String OPTION_ID = "id";

    public static void main(String[] args) throws Exception {
        new GetEvent().doMain(args);
    }

    protected Options addOptions(Options options) {
        options.addOption(null, OPTION_ID, true, "The id of the event to show.");

        return options;
    }

    public void work() {
        Event event = getEms().getEvent(getCommandLine().getOptionValue(OPTION_ID));

        System.err.println("Id: " + event.getId());
        System.err.println("Name: " + event.getName());
        System.err.println("Date: " + event.getDate());
        System.err.println("Notes: " + event.getNotes());
        System.err.println("Revision: " + event.getRevision());

        System.err.println("Tags");
        for (String tag : event.getTags()) {
            System.err.println(" " + tag);
        }
    }
}
