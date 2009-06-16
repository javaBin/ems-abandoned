package no.java.ems.cli;

import no.java.ems.external.v1.SessionV1;
import org.apache.commons.cli.Options;

import java.util.List;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Search extends AbstractCli {
    public Search() {
        super("search");
    }

    public static void main(String[] args) throws Exception {
        new Search().doMain(args);
    }

    private static final String OPTION_QUERY = "query";

    protected Options addOptions(Options options) {
        options.addOption(eventId);
        options.addOption(null, OPTION_QUERY, true, "The query");
        return options;
    }

    public void work() throws Exception {
        if (!assertIsPresent(OPTION_EVENT_ID)) {
            usage();
            return;
        }

        String eventId = getCommandLine().getOptionValue(OPTION_EVENT_ID);
        String query = getCommandLine().getOptionValue(OPTION_QUERY);

        List<SessionV1> sessions = getEms().searchForSessions(eventId, query).getSession();

        for (SessionV1 session : sessions) {
            System.out.println(session);
        }
    }
}
