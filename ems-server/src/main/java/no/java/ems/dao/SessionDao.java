package no.java.ems.dao;

import no.java.ems.domain.Session;
import org.joda.time.LocalDate;

import java.util.List;

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
public interface SessionDao {

    List<Session> getSessions(String eventId);

    List<String> getSessionIdsByEventId(String eventId);

    List<String> findSessionsBySpeakerName(String eventId, String name);

    List<String> findSessionsByTitle(String eventId, String title);

    List<String> findSessionsByDate(String eventId, LocalDate date);

    Session getSession(String id);

    void saveSession(Session session);

    void deleteSession(String id);
}
