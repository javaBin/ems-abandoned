package no.java.ems.server.it;

import no.java.ems.dao.EventDao;
import no.java.ems.dao.RoomDao;
import no.java.ems.dao.SessionDao;
import no.java.ems.server.domain.Event;
import no.java.ems.server.domain.Session;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static fj.data.Option.some;

/**
 * @author Trygve Laugstol
 */
public class SessionDaoIntegrationTest extends AbstractIntegrationTest {

    private final String eventName = this.getClass().getSimpleName();
    private static LocalDate sep11 = new LocalDate(2008, 9, 11);
    private static LocalDate sep12 = new LocalDate(2008, 9, 12);
    private static LocalDate sep13 = new LocalDate(2008, 9, 13);

    private SessionDao sessionDao;

    @Test
    public void testFindByDate() throws Exception {
        File emsHome = PlexusTestCase.getTestFile("target/ems-home");
        FileUtils.deleteDirectory(emsHome);

        getDerbyService().maybeCreateTables(false);
        EventDao eventDao = getEventDao();
        RoomDao roomDao = getRoomDao();
        sessionDao = getSessionDao();

        Event event = eventDao.getEventByName(eventName);

        if (event == null) {
            roomDao.save(room1);
            roomDao.save(room2);
            roomDao.save(room3);

            event = new Event();
            event.setName(eventName);
            event.setDate(sep12);
            event.setRooms(Arrays.asList(room1, room2, room3));
            eventDao.saveEvent(event);

            insertSessions(event.getId(), sep11);
            insertSessions(event.getId(), sep12);
            insertSessions(event.getId(), sep13);
        }

        List<String> list = sessionDao.findSessionsByDate(event.getId(), sep12);
        assertNotNull(list);
        assertEquals(3, list.size());
        for (int i = 0; i < list.size(); i++) {
            String id = list.get(i);

            Session session = sessionDao.getSession(event.getId(), id);

            assertEquals(12, session.getTimeslot().some().getStart().getDayOfMonth());
            assertNotNull(session.getRoom());
            assertNotNull(session.getRoom().getId());
            if (i == 0) {
                assertTrue(EqualsBuilder.reflectionEquals(room1, session.getRoom()));
            } else if (i == 1) {
                assertTrue(EqualsBuilder.reflectionEquals(room2, session.getRoom()));
            } else if (i == 2) {
                assertTrue(EqualsBuilder.reflectionEquals(room3, session.getRoom()));
            }
        }
    }

    public void insertSessions(String eventId, LocalDate date) {
        LocalDateTime dateTime = date.toLocalDateTime(LocalTime.MIDNIGHT);

        dateTime = dateTime.plusHours(9);

        Session session1 = new Session();
        session1.setEventId(eventId);
        session1.setTitle("Session 1");
        session1.setTimeslot(some(new Interval(dateTime.toDateTime(), Minutes.minutes(60))));
        session1.setRoom(room1);
        sessionDao.saveSession(session1);

        dateTime = dateTime.plusHours(1);

        Session session2 = new Session();
        session2.setEventId(eventId);
        session2.setTitle("Session 2");
        session2.setTimeslot(some(new Interval(dateTime.toDateTime(), Minutes.minutes(60))));
        session2.setRoom(room2);
        sessionDao.saveSession(session2);

        dateTime = dateTime.plusHours(1);

        Session session3 = new Session();
        session3.setEventId(eventId);
        session3.setTitle("Session 3");
        session3.setTimeslot(some(new Interval(dateTime.toDateTime(), Minutes.minutes(60))));
        session3.setRoom(room3);
        sessionDao.saveSession(session3);
    }
}
