package no.java.ems.cli;

import fj.data.Option;
import no.java.ems.external.v1.EventV1;
import org.apache.commons.cli.Options;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
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

    public void work() throws Exception {
        Option<EventV1> option = getEms().getEvent(getCommandLine().getOptionValue(OPTION_ID));

        if (option.isNone()) {
            System.err.println("No such event.");
            return;
        }

        EventV1 event = option.some();

        System.err.println("Id: " + event.getUuid());
        System.err.println("Name: " + event.getName());
        System.err.println("Date: " + event.getDate());

        System.err.println("Tags");
        for (String tag : event.getTags().getTag()) {
            System.err.println(" " + tag);
        }
    }
}
