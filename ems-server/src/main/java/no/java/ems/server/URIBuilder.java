package no.java.ems.server;

import no.java.ems.server.domain.*;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static javax.ws.rs.core.UriBuilder.fromUri;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Id$
 */
public class URIBuilder {
    private final UriBuilder baseurl;

    public URIBuilder(UriBuilder baseurl) {
        this.baseurl = baseurl.clone().segment("1");
    }

    public URIBuilder(String baseurl) {
        this(fromUri(baseurl));
    }

    public EventsUri events() {
        return new EventsUri(baseurl.clone().segment("events"));
    }

    public SearchUri search() {
        return new SearchUri(baseurl.clone().segment("search"));
    }

    public PeopleUri people() {
        return new PeopleUri(baseurl.clone().segment("people"));
    }

    public URI forObject(AbstractEntity entity) {
        if (entity instanceof Session) {
            Session session = (Session) entity;
            return events().eventUri(session.getEventId()).session(session.getId());
        }
        else if (entity instanceof Person) {
            return people().person(entity.getId());
        }
        else if (entity instanceof Event) {
            return events().eventUri(entity.getId()).get();
        }
        throw new IllegalArgumentException(String.format("Unsupported entity type %s with id %s", entity.getClass().getSimpleName(), entity.getId()));
    }       

    public URI forObject(String parentId, String id, ObjectType type) {
        switch (type) {
            case event:
                return events().eventUri(id).get();
            case person:
                return people().person(id);
            case session:
                return events().eventUri(parentId).session(id);
            default:
                throw new IllegalArgumentException("unknown type");
        }
    }

    public static class EventsUri {
        private final UriBuilder events;

        private EventsUri(UriBuilder events) {
            this.events = events;
        }

        public EventUri eventUri(String id) {
            return new EventUri(events.clone().segment(id));
        }

        public static class EventUri {
            private final UriBuilder event;
            private final UriBuilder sessions;

            private EventUri(UriBuilder event) {
                this.event = event;
                this.sessions = event.clone().segment("sessions");
            }

            public URI sessionList() {
                return sessions.build();
            }

            public String toString() {
                return event.build().toString();
            }

            public URI get() {
                return event.build();
            }

            public URI session(String sessionId) {
                return sessions.clone().segment(sessionId).build();
            }
        }
    }

    public static class SearchUri {
        private final UriBuilder search;

        private SearchUri(UriBuilder search) {
            this.search = search;
        }

        public URI getURI() {
            return search.build();
        }
    }

    public static class PeopleUri {
        private final UriBuilder people;

        private PeopleUri(UriBuilder people) {
            this.people = people;
        }

        public URI people() {
            return people.build();
        }

        public URI person(String personId) {
            return people.segment(personId).build();
        }
    }
}
