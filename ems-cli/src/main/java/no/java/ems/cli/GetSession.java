package no.java.ems.cli;

import fj.data.Option;
import no.java.ems.external.v1.SessionV1;
import org.apache.commons.cli.Options;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class GetSession extends AbstractCli {
    public GetSession() {
        super("get-session");
    }

    public static void main(String[] args) throws Exception {
        new GetSession().doMain(args);
    }

    protected Options addOptions(Options options) {
        options.addOption(eventId);
        options.addOption(null, OPTION_SESSION_ID, true, "The id of the session to show.");
        options.addOption(parseable);

        return options;
    }

    public void work() throws Exception {
        String sessionId = getCommandLine().getOptionValue(OPTION_SESSION_ID);
        Option<SessionV1> option = getEms().getSession(getDefaultEventId(), sessionId);

        if (option.isNone()) {
            System.err.println("No such session.");
        }

        PrintUtil.print(getCommandLine(), option.some());
    }
}
