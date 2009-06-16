package no.java.ems.cli;

import no.java.ems.external.v1.EventV1;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ListEvents extends AbstractCli {
    public ListEvents() {
        super("list-events");
    }

    public static void main(String[] args) throws Exception {
        new ListEvents().doMain(args);
    }

    public void work() throws Exception {
        System.err.println("Events: ");
        for (EventV1 event : getEms().getEvents().getEvent()) {
            System.err.println(event.getUuid() + ":" + event.getName());
        }
    }
}
