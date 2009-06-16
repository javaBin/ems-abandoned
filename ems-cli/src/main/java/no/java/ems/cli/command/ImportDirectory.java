package no.java.ems.cli.command;

import fj.Effect;
import fj.F;
import fj.F2;
import fj.Function;
import fj.data.List;
import no.java.ems.external.v1.RestletEmsV1Client;
import no.java.ems.external.v1.SessionV1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URI;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ImportDirectory implements Runnable {

    private final RestletEmsV1Client ems;
    private final File dir;
    private final String eventId;
    private final F<InputStream, SessionV1> unmarshallSession;

    public ImportDirectory(RestletEmsV1Client ems, String eventId, File dir) {
        this.ems = ems;
        this.eventId = eventId;
        this.dir = dir;

        unmarshallSession = ems.unmarshallInputStream(SessionV1.class);
    }

    public void run() {
        List<File> files = List.list(dir.listFiles(new FilenameFilter() {
            public boolean accept(File file, String s) {
                return s.endsWith(".xml");
            }
        }));

        System.err.println("Importing " + files.length() + " sessions from " + dir.getAbsolutePath() + "...");

        List<SessionV1> sessions = files.
            map(Function.curry(readSession, eventId));

        System.err.println("Adding " + sessions.length() + " objects...");

        sessions.foreach(addSession);

        System.err.println("Import complete");
    }

    private F2<String, File, SessionV1> readSession = new F2<String, File, SessionV1>() {
        public SessionV1 f(String eventId, File file) {
            try {
                SessionV1 session = unmarshallSession.f(new FileInputStream(file));
                session.setEventUuid(eventId);
                return session;
            } catch (FileNotFoundException e) {
                throw new RuntimeException("File not found: " + file.getAbsolutePath(), e);
            }
        }
    };

    private Effect<SessionV1> addSession = new Effect<SessionV1>() {
        public void e(SessionV1 session) {
            URI uri = ems.addSession(session);

            System.err.println("URI: " + uri);
        }
    };
}
