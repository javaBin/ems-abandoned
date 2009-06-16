package no.java.ems.cli;

import no.java.ems.domain.Session;
import org.apache.commons.cli.Options;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
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
        options.addOption(null, OPTION_SESSION_ID, true, "The id of the session to show.");
        options.addOption(parseable);

        return options;
    }

    public void work() {
        String sessionId = getCommandLine().getOptionValue(OPTION_SESSION_ID);
        Session session = getEms().getSession(sessionId);

        PrintUtil.print(getCommandLine(), session);
    }
}
