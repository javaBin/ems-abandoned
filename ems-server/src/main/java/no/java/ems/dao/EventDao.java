package no.java.ems.dao;

import no.java.ems.server.domain.Event;

import java.util.ArrayList;

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
public interface EventDao {

    Event getEvent(String id);

    Event getEventByName(String simpleName);

    ArrayList<Event> getEvents();

    void saveEvent(Event event);

    void deleteEvent(String id);
}
