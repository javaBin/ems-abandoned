package no.java.ems.cli;

import static no.java.ems.cli.PrintUtil.print;
import no.java.ems.external.v1.SessionV1;
import no.java.ems.external.v1.EmsV1F;
import org.apache.commons.cli.Options;

import java.util.List;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
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

    public void work() throws Exception {
        if (!assertIsPresent(OPTION_EVENT_ID)) {
            usage();
            return;
        }

        String eventId = getCommandLine().getOptionValue(OPTION_EVENT_ID);

        List<SessionV1> sessions = getEms().getSessions(eventId).getSession();

        for (SessionV1 session : sessions) {
            print(getCommandLine(), session);
        }
    }
}
