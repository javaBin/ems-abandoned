package no.java.ems.client;

import no.java.ems.domain.*;
import no.java.ems.service.EmsService;
import no.java.ems.service.RequestCallback;
import org.joda.time.LocalDate;
import org.restlet.Client;
import org.restlet.data.Protocol;

import java.util.List;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class RestEmsService implements EmsService {

    private EventsClient eventsClient;
    private PeopleClient peopleClient;
    private SessionsClient sessionsClient;
    private BinaryClient binaryClient;
    private RoomClient roomClient;

    private ClientCache<Session> sessionCache;

    public RestEmsService(String baseURI) {
        this(baseURI, false);
    }

    public RestEmsService(String baseUri, boolean cache) {
        Client client = new Client(Protocol.HTTP);

        eventsClient = new EventsClient(baseUri, client);
        peopleClient = new PeopleClient(baseUri, client);
        sessionsClient = new SessionsClient(baseUri, client);
        binaryClient = new BinaryClient(baseUri, client);
        roomClient = new RoomClient(baseUri, client);
        sessionCache = new ClientCache<Session>(sessionsClient, cache);

        setCredentials(null, null);
        setRequestCallback(null);
    }

    public List<Person> getContacts() {
        return peopleClient.listPeople();
    }

    public Person getContact(String id) {
        return peopleClient.getPerson(id);
    }

    public Person saveContact(Person person) {
        if (person.getId() == null) {
            peopleClient.createPerson(person);
        } else {
            peopleClient.updatePerson(person);
        }
        return person;
    }

    public void deleteContact(String id) {
        peopleClient.deletePerson(peopleClient.getPerson(id));
    }

    public List<Event> getEvents() {
        return eventsClient.listEvents();
    }

    public Event getEvent(String id) {
        return eventsClient.getEvent(id);
    }

    public Event saveEvent(Event event) {
        if (event.getId() == null) {
            eventsClient.createEvent(event);
        } else {
            eventsClient.updateEvent(event);
        }
        return event;
    }

    public void deleteEvent(String id) {
        eventsClient.deleteEvent(eventsClient.getEvent(id));
    }

    public Session getSession(String id) {
        return sessionsClient.getSession(id);
    }

    public Session saveSession(Session session) {
        if (session.getId() == null) {
            sessionsClient.createSession(session);
        } else {
            sessionsClient.updateSession(session);
        }
        sessionCache.evict(session.getId());
        return session;
    }

    public void deleteSession(String id) {
        sessionsClient.deleteSession(id);
        sessionCache.evict(id);
    }

    public List<Session> getSessions(String eventId) {
        return sessionsClient.findSessionsByEvent(eventId);
    }

    public Binary getBinary(String binaryId) {
        return binaryClient.getBinary(binaryId);
    }

    public Binary saveBinary(Binary binary) {
        // Always create the binary, no update at this point
        return binaryClient.createBinary(binary);
    }

    public void deleteBinary(String binaryId) {
        binaryClient.deleteBinary(binaryId);
    }

    public List<Session> findSessionsBySpeakerName(String eventId, String name) {
        return sessionCache.getList(sessionsClient.findSessionsBySpeakerName(eventId, name));
    }

    public List<Session> findSessionsByEvent(String eventId) {
        return sessionCache.getList(sessionsClient.findSessionIdsByEvent(eventId));
    }

    public List<Session> findSessionByDate(String eventId, LocalDate date) {
        return sessionCache.getList(sessionsClient.findSessionsByDate(eventId, date));
    }

    public List<Session> findSessionsByTitle(String eventId, String title) {
        return sessionCache.getList(sessionsClient.findSessionsByTitle(eventId, title));
    }

    public List<? extends AbstractEntity> search(String eventId, String query) {
        return sessionCache.getList(sessionsClient.search(eventId, query));
    }

    // -----------------------------------------------------------------------
    // Authentication
    // -----------------------------------------------------------------------

    public void setCredentials(String username, String password) {
        eventsClient.setCredentials(username, password);
        peopleClient.setCredentials(username, password);
        sessionsClient.setCredentials(username, password);
        binaryClient.setCredentials(username, password);
    }

    // -----------------------------------------------------------------------
    // Getters for the clients
    // -----------------------------------------------------------------------

    public EventsClient getEventsClient() {
        return eventsClient;
    }

    public PeopleClient getPeopleClient() {
        return peopleClient;
    }

    public SessionsClient getSessionsClient() {
        return sessionsClient;
    }

    public BinaryClient getBinaryClient() {
        return binaryClient;
    }


    public RoomClient getRoomClient() {
        return roomClient;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public void setRequestCallback(RequestCallback requestCallback) {
        if(requestCallback == null){
            requestCallback = new RequestCallback();
        }

        eventsClient.setResultEvaluator(requestCallback);
        peopleClient.setResultEvaluator(requestCallback);
        sessionsClient.setResultEvaluator(requestCallback);
        binaryClient.setResultEvaluator(requestCallback);
        roomClient.setResultEvaluator(requestCallback);
    }


    public List<Room> getRooms() {
        return roomClient.listRooms();
    }

    public void saveRoom(String eventId, Room room) {
        if (room.getId() != null) {
            roomClient.createRoom(eventId, room);
        }
        else {
            roomClient.updateRoom(eventId, room);
        }
    }
}
