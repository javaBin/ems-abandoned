package no.java.ems.external.v1;

import fj.data.Option;
import fj.Unit;
import org.joda.time.LocalDate;

import java.net.URI;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface EmsV1Client {
    Option<EventV1> getEvent(String eventId);

    URI addEvent(EventV1 event);

    EventListV1 getEvents();

    SessionListV1 getSessions(String eventId);

    Option<SessionV1> getSession(String eventId, String sessionId);

    SessionListV1 findSessionsByDate(String eventId, LocalDate date);

    SessionListV1 findSessionsByTitle(String eventId, String title);

    SessionListV1 getSessionsByTitle(String eventId, String title);

    SessionListV1 searchForSessions(String eventId, String query);

    URI addSession(SessionV1 session);

    Unit updateSession(SessionV1 session);

    PersonListV1 getPeople();

    Option<PersonV1> getPerson(String personId);

    URI addPerson(PersonV1 personV1);

    Unit updatePerson(PersonV1 personV1);
}
