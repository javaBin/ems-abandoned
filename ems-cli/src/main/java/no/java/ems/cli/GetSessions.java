package no.java.ems.cli;

import no.java.ems.domain.Session;
import org.apache.commons.cli.Options;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class GetSessions extends AbstractCli {
    public GetSessions() {
        super("get-sessions");
    }

    public static void main(String[] args) throws Exception {
        new GetSessions().doMain(args);
    }

    protected Options addOptions(Options options) {
        options.addOption(null, OPTION_EVENT_ID, true, "The id of the event to show.");
        options.addOption(parseable);

        return options;
    }

    public void work() {
        String eventId = getCommandLine().getOptionValue(OPTION_EVENT_ID);

        for (Session session : getEms().getSessions(eventId)) {
            PrintUtil.print(getCommandLine(), session);
        }
    }
}
