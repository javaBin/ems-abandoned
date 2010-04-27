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
        this.baseurl = baseurl.clone().segment("2");
    }

    public URIBuilder(String baseurl) {
        this(fromUri(baseurl));
    }

    public URIBuilder(URI baseurl) {
        this(fromUri(baseurl));
    }

    public URI baseURI() {
        return baseurl.clone().build();
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

    public SessionsUri sessions() {
        return new SessionsUri(baseurl.clone().segment("events"));
    }

    public RoomsUri rooms() {
        return new RoomsUri(baseurl.clone().segment("rooms"));
    }


    public URI forObject(AbstractEntity entity) {
        if (entity instanceof Session) {
            Session session = (Session) entity;
            return sessions().session(session.getEventId(), session.getId());
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
                return sessions().session(parentId, id);
            default:
                throw new IllegalArgumentException("unknown type");
        }
    }

    public BinariesUri binaries() {
        return new BinariesUri(baseurl.clone().segment("binaries"));
    }

    public static class EventsUri {
        private final UriBuilder events;

        private EventsUri(UriBuilder events) {
            this.events = events;
        }

        public URI getURI() {
            return events.build();
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

        public URI getURI(String query, String type, int offset) {
            return search.clone().queryParam("q", query == null ? "" : query).
                    queryParam("type", type).
                    queryParam("pw", String.valueOf(offset)).
                    build();
        }

        public URI form() {
            return search.clone().segment("form").build();
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

        public URI personWithPhoto(String personId) {
            return people.clone().segment(personId, "photo").build();
        }

        public URI person(String personId) {
            return people.clone().segment(personId).build();
        }
    }

    public static class SessionsUri {
        private final UriBuilder builder;

        private SessionsUri(UriBuilder builder) {
            this.builder = builder;
        }

        public URI sessions(String eventid) {
            return builder.clone().segment(eventid, "sessions").build();
        }

        public URI session(String eventid, String sessionid) {
            return builder.clone().segment(eventid, "sessions", sessionid).build();
        }
    }

    public class RoomsUri {
        private final UriBuilder builder;

        public RoomsUri(UriBuilder builder) {
            this.builder = builder;
        }

        public URI rooms() {
            return builder.build();
        }

        public URI room(String roomId) {
            return builder.clone().segment(roomId).build();
        }
    }

    public class BinariesUri {
        private final UriBuilder builder;

        public BinariesUri(UriBuilder builder) {
            this.builder = builder;
        }

        public URI binaries() {
            return builder.build();
        }

        public URI binary(String binaryId) {
            return builder.clone().segment(binaryId).build();
        }
    }
}
