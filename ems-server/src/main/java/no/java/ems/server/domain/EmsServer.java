package no.java.ems.server.domain;

import fj.F;
import fj.F2;
import static fj.Function.curry;
import fj.Unit;
import fj.data.Java;
import fj.data.List;
import static fj.data.List.iterableList;
import fj.data.Option;
import no.java.ems.dao.BinaryDao;
import no.java.ems.dao.EventDao;
import no.java.ems.dao.PersonDao;
import no.java.ems.dao.SessionDao;
import no.java.ems.server.domain.AbstractEntity;
import no.java.ems.server.domain.Binary;
import no.java.ems.server.domain.Event;
import no.java.ems.server.domain.Person;
import no.java.ems.server.domain.Session;
import no.java.ems.server.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Component
public class EmsServer implements InitializingBean {
    private final Log log = LogFactory.getLog(getClass());
    private final EventDao eventDao;
    private final SessionDao sessionDao;
    private final SearchService searchService;
    private final PersonDao personDao;
    private final BinaryDao binaryDao;

    @Autowired
    public EmsServer(SearchService searchService, EventDao eventDao, SessionDao sessionDao, PersonDao personDao, BinaryDao binaryDao) {
        this.eventDao = eventDao;
        this.searchService = searchService;
        this.sessionDao = sessionDao;
        this.personDao = personDao;
        this.binaryDao = binaryDao;
    }

    // -----------------------------------------------------------------------
    // Public
    // -----------------------------------------------------------------------

    public Option<Event> getEventOption(String id) {
        try {
            return Option.some(getEvent(id));
        } catch (EmptyResultDataAccessException e) {
            return Option.none();
        }
    }

    public Event getEvent(String id) {
        return eventDao.getEvent(id);
    }

    public List<Event> getEvents() {
        ArrayList<Event> list = eventDao.getEvents();
        return Java.<Event>ArrayList_List().f(list);
    }

    public void saveEvent(Event event) {
        eventDao.saveEvent(event);

        index(event, false);
    }

    public void deleteEvent(final String id) {
        getEventOption(id).map(new F<Event, Unit>() {
            public Unit f(Event event) {
                eventDao.deleteEvent(id);
                index(event, true);
                return Unit.unit();
            }
        });
    }

    public List<Session> getSessions(String eventId) {
        return Java.<Session>ArrayList_List().f(new ArrayList<Session>(sessionDao.getSessions(eventId)));
    }

    public List<Session> getSessionsByDate(String eventId, LocalDate date) {
        return iterableList(sessionDao.findSessionsByDate(eventId, date)).
                map(curry(getSession_(), eventId));
    }

    public List<Session> getSessionsByTitle(String eventId, String title) {
        return iterableList(sessionDao.findSessionsByTitle(eventId, title)).
                map(curry(getSession_(), eventId));
    }

    public Option<Session> getSession(String eventId, String sessionId) {
        try {
            return Option.some(sessionDao.getSession(eventId, sessionId));
        } catch (EmptyResultDataAccessException e) {
            return Option.none();
        }
    }

    public void saveSession(String eventId, Session session) {
        System.out.println("eventId = " + eventId);
        System.out.println("session.getEventId() = " + session.getEventId());
        if (!eventId.equals(session.getEventId())) {
            throw new RuntimeException("eventId != session.eventId");
        }

        eventDao.getEvent(eventId);         // validate that the event actually exist
        session.setEventId(eventId);
        sessionDao.saveSession(session);
        index(session, false);
    }

    public List<Person> getPeople() {
        return Java.<Person>ArrayList_List().f(new ArrayList<Person>(personDao.getPersons()));
    }

    public Option<Person> getPerson(String personId) {
        try {
            return Option.some(personDao.getPerson(personId));
        } catch (EmptyResultDataAccessException e) {
            return Option.none();
        }
    }

    public void savePerson(Person person) {
        personDao.savePerson(person);
    }

    public Binary createBinary(InputStream entity, String filename, String mimeType) {
        return binaryDao.createBinary(entity, filename, mimeType);
    }

    // -----------------------------------------------------------------------
    // Component Lifecycle
    // -----------------------------------------------------------------------

    public void afterPropertiesSet() throws Exception {
        // This has the nice side effect of making sure that the database is initialized and working properly.
        // If this fails the application won't start and no client will get a useful response.
        log.warn("All events:");
        for (Event event : eventDao.getEvents()) {
            log.warn(" o " + event.getName());
        }
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    /**
     * Indexes the given abstract entity with the configured SearchService.
     *
     * @param entity the entity to index.
     * @param delete {@code true} if the entity should be removed from the index.
     */
    protected <T extends AbstractEntity> void index(T entity, boolean delete) {
        if (!delete) {
            searchService.update(entity);
        } else {
            searchService.delete(entity);
        }
    }

    F2<String, String, Session> getSession_() {
        return new F2<String, String, Session>() {
            public Session f(String eventId, String sessionId) {
                return sessionDao.getSession(eventId, sessionId);
            }
        };
    }
}
