package no.java.ems.cli;

import no.java.ems.domain.Event;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ListEvents extends AbstractCli {
    public ListEvents() {
        super("list-events");
    }

    public static void main(String[] args) throws Exception {
        new ListEvents().doMain(args);
    }

    public void work() {
        System.err.println("Events: ");
        for (Event event : getEms().getEvents()) {
            System.err.println(event.getId() + ":" + event.getName());
        }
    }
}
