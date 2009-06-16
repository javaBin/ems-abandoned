package no.java.ems.cli;

import com.thoughtworks.xstream.XStream;
import no.java.ems.client.SessionsClient;
import no.java.ems.domain.Binary;
import no.java.ems.domain.Event;
import no.java.ems.domain.Session;
import no.java.ems.domain.Speaker;
import org.apache.commons.cli.Options;

import java.io.FileReader;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ImportData extends AbstractCli {
    private SessionsClient sessionsClient;

    protected ImportData() {
        super("import-data");
    }

    public static void main(String[] args) throws Exception {
        new ImportData().doMain(args);
    }

    protected Options addOptions(Options options) {
        options.addOption(null, OPTION_EVENT_ID, true, "The event to import data into");
        options.addOption(null, OPTION_FILE, true, "The data file to import");
        return options;
    }

    protected void work() throws Exception {
        if (!assertIsPresent(OPTION_FILE)) {
            usage();
            return;
        }

        String eventId = getDefaultEventId();
        String file = getCommandLine().getOptionValue(OPTION_FILE);

        sessionsClient = getEms().getSessionsClient();

        XStream xstream = new EmsXStream();

        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            EventData data = (EventData) xstream.fromXML(fileReader);

            data.setEventId(eventId);

            importData(data);
        } finally {
            close(fileReader);
        }
    }

    private void importData(EventData data) {
        String eventId = data.getEventId();

        Event event = getEms().getEvent(eventId);
        if (event == null) {
            System.err.println("No such event: " + eventId);
            return;
        }

        System.err.println("Event name: " + event.getName());

        System.err.println("Importing...");
        importSessions(data);
        System.err.println("Import complete");
    }

    private void importSessions(EventData data) {
        List<Session> sessions = data.getSessions();
        System.err.println("Importing " + sessions.size() + " sessions");

        for (Session session : sessions) {
            if (session == null) {
                System.out.println("Warning: session is null!");
                continue;
            }

            session.setEventId(data.getEventId());
            session.setId(null);
            session.setRevision(0);

            session.setAttachements(Collections.<Binary>emptyList());
            session.setSpeakers(Collections.<Speaker>emptyList());

            sessionsClient.createSession(session);
        }
    }
}
