package no.java.ems.cli;

import no.java.ems.client.SessionsClient;
import no.java.ems.domain.Session;
import static no.java.ems.cli.PrintUtil.print;
import org.apache.commons.cli.Options;

import java.util.List;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ListSessions extends AbstractCli {

    public ListSessions() {
        super("list-sessions");
    }

    public static void main(String[] args) throws Exception {
        new ListSessions().doMain(args);
    }

    protected Options addOptions(Options options) {
        options.addOption(eventId);
        options.addOption(parseable);
        return options;
    }

    public void work() {
        if (!assertIsPresent(OPTION_EVENT_ID)) {
            usage();
            return;
        }

        String eventId = getCommandLine().getOptionValue(OPTION_EVENT_ID);

        List<Session> sessions = notNullList(getEms().getSessions(eventId));

        for (Session session : sessions) {
            print(getCommandLine(), session);
        }
    }
}
