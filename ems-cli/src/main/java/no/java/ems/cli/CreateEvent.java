package no.java.ems.cli;

import org.apache.commons.cli.Options;
import no.java.ems.external.v1.EventV1;

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
        EventV1 event = new EventV1();
        event.setName(getCommandLine().getOptionValue(OPTION_EVENT_NAME));
        getEms().addEvent(event);
        System.err.println("event.getId() = " + event.getUuid());
    }
}
