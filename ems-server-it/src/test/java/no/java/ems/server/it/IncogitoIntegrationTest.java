package no.java.ems.server.it;

import junit.framework.TestCase;
import no.java.ems.cli.ImportData;
import no.java.ems.client.RestEmsService;
import no.java.ems.client.SessionsClient;
import no.java.ems.dao.EventDao;
import no.java.ems.dao.SessionDao;
import no.java.ems.domain.Event;
import no.java.ems.domain.Session;
import no.java.ems.server.EmsServices;
import org.codehaus.plexus.PlexusTestCase;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;

import java.util.List;

/**
 * Test cases based on the use cases that Incogito has for EMS
 *
 * @author Trygve Laugstol
 */
public class IncogitoIntegrationTest extends TestCase {

    private static EmsServices emsServices;

    private static String eventId;
    private static LocalDate sep12 = new LocalDate(2008, 9, 12);

    private static String baseUri;

    public void testGetScheduleForDay() throws Exception {
        setUp2();

        SessionsClient sessionsClient = new RestEmsService(baseUri).getSessionsClient();

        List<String> sessions = sessionsClient.findSessionsByDate(eventId, sep12);

        for (String sessionId : sessions) {
            assertNotNull(sessionId);
        }

        String title = "Implementing external DSLs in Java";
        List<String> ids = sessionsClient.findSessionsByTitle(eventId, title);
        assertNotNull(ids);
        // uhm, slight issue with duplicate sessions on import
        for (String id : ids) {
            assertEquals(title, sessionsClient.getSession(id).getTitle());
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public void setUp2() throws Exception {
        emsServices = new EmsServices(PlexusTestCase.getTestFile("target/ems-home"), 3010, true, false, 0, false);
        emsServices.getDerbyService().maybeCreateTables(false);

        baseUri = "http://localhost:" + emsServices.getHttpPort() + "/ems";

        EventDao eventDao = emsServices.getEventDao();
        SessionDao sessionDao = emsServices.getSessionDao();
        List<Event> list = eventDao.getEvents();
        if (list.size() == 0) {
            Event event = new Event();
            event.setName("IntegrationZone");
            event.setDate(sep12);
            eventDao.saveEvent(event);
            eventId = event.getId();
        } else {
            eventId = list.get(0).getId();
        }

        System.err.println("eventId = " + eventId);

        ImportData.main(
                new String[]{
                        "--base-uri", baseUri,
                        "--file", getClass().getResource("/data.xml").toURI().getPath(),
                        "--event-id", eventId}
        );

        LocalDateTime date = sep12.toLocalDateTime(LocalTime.MIDNIGHT);
        date = date.plusHours(9);

        for (Session session : sessionDao.getSessions(eventId)) {
            session.setTimeslot(new Interval(date.toDateTime(), Minutes.minutes(60)));
            date.plusMinutes(75);
            sessionDao.saveSession(session);
        }
    }

    public void tearDown() throws Exception {
        emsServices.stop();
    }
}
