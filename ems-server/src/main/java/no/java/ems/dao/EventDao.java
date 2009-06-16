package no.java.ems.dao;

import no.java.ems.domain.Event;

import java.util.List;

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
public interface EventDao {

    Event getEvent(String id);

    Event getEventByName(String simpleName);

    List<Event> getEvents();

    void saveEvent(Event event);

    void deleteEvent(String id);
}
