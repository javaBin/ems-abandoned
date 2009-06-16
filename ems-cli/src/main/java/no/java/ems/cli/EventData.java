package no.java.ems.cli;

import no.java.ems.domain.Session;

import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class EventData {
    private String eventId;

    private List<Session> sessions;

    private <T> List<T> get(List<T> list) {
        if(list == null) {
            return new ArrayList<T>();
        }

        return list;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public List<Session> getSessions() {
        return get(sessions);
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }
}
