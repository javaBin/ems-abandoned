package no.java.ems.cli;

import org.apache.commons.cli.Options;

import java.util.List;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
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

    public void work() {
        if (!assertIsPresent(OPTION_EVENT_ID)) {
            usage();
            return;
        }

        String eventId = getCommandLine().getOptionValue(OPTION_EVENT_ID);
        String query = getCommandLine().getOptionValue(OPTION_QUERY);

        List sessions = notNullList(getEms().search(eventId, query));

        for (Object session : sessions) {
            System.out.println(session);
        }
    }
}
