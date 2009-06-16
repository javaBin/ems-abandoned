package no.java.ems.cli;

import no.java.ems.domain.Event;
import no.java.ems.client.EventsClient;
import org.apache.commons.cli.Options;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
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
        EventsClient eventsClient = getEms().getEventsClient();

        Event event = new Event();
        event.setName(getCommandLine().getOptionValue(OPTION_EVENT_NAME));
        eventsClient.createEvent(event);
        System.err.println("event.getId() = " + event.getId());
    }
}
