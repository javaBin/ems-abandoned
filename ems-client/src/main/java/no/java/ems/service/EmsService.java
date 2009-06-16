package no.java.ems.service;

import no.java.ems.client.*;
import no.java.ems.domain.*;
import org.joda.time.LocalDate;

import java.util.List;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public interface EmsService {

    List<Person> getContacts();

    Person getContact(String id);

    Person saveContact(Person contact);

    void deleteContact(String id);

    List<Event> getEvents();

    Event getEvent(String id);

    Event saveEvent(Event event);

    void deleteEvent(String id);

    List<Session> getSessions(String eventId);

    Session getSession(String id);

    Session saveSession(Session session);

    void deleteSession(String id);

    Binary saveBinary(Binary binary);

    Binary getBinary(String id);

    void deleteBinary(String concat);

    List<Session> findSessionsBySpeakerName(String eventId, String name);

    List<Session> findSessionsByEvent(String eventId);

    List<Session> findSessionByDate(String eventId, LocalDate date);

    List<Session> findSessionsByTitle(String eventId, String title);

    void saveRoom(String eventId, Room room);

    List<Room> getRooms();

    // -----------------------------------------------------------------------
    // Search
    // -----------------------------------------------------------------------

    List<? extends AbstractEntity> search(String eventId, String query);

    // -----------------------------------------------------------------------
    // Authentication
    // -----------------------------------------------------------------------

    void setCredentials(String username, String password);

    // -----------------------------------------------------------------------
    // Getters for the clients
    // -----------------------------------------------------------------------

    EventsClient getEventsClient();

    PeopleClient getPeopleClient();

    SessionsClient getSessionsClient();

    BinaryClient getBinaryClient();

    RoomClient getRoomClient();
    // -----------------------------------------------------------------------
    //

    // -----------------------------------------------------------------------

    void setRequestCallback(RequestCallback requestCallback);
}
