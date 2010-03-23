package no.java.ems.server.resources.java;

import fj.F;
import fj.data.*;
import no.java.ems.server.domain.*;
import no.java.ems.server.domain.Event;
import no.java.ems.server.domain.Session;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import javax.ws.rs.*;
import java.util.*;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Component
@Path("events")
@Produces("application/x-java-serialized-object")
@Consumes("application/x-java-serialized-object")
public class EventJavaResource {
    private final EmsServer emsServer;
    private DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyyMMdd");
    
    private final F<Session,String> sessionToId = new F<Session, String>() {
        public String f(Session session) {
            return session.getId();
        }
    };

    @Autowired
    public EventJavaResource(EmsServer emsServer) {
        this.emsServer = emsServer;
    }

    @GET
    public List<Object> getEvents() {

        List<Object> events = new ArrayList<Object>();

        for (Event event : emsServer.getEvents()) {
            events.add(ExternalEmsDomainJavaF.eventToExternal.f(event));
        }

        return events;
    }

    @GET
    @Path("{eventId}")
    public Object getEvent(@PathParam("eventId") String eventId) {
        Option<no.java.ems.domain.Event> eventOption = emsServer.getEventOption(eventId).map(ExternalEmsDomainJavaF.eventToExternal);
        if (eventOption.isSome()) {
            return eventOption.some();
        }
        throw new WebApplicationException(404);
    }

    @Path("{eventId}/sessions")
    public SessionsJavaResource getSessionResource() {
        return new SessionsJavaResource(emsServer);
    }

    @GET
    @Path("{eventId}/sessions-by-date/{date}")
    public List<String> getSessionsByDate(@PathParam("eventId") String eventId, @PathParam("date") String date) {
        fj.data.List<Session> list = emsServer.getSessionsByDate(eventId, dateFormatter.parseDateTime(date).toLocalDate());
        return new ArrayList<String>(list.map(sessionToId).toCollection());
    }

    @GET
    @Path("{eventId}/sessions-by-title/{date}")
    public List<String> getSessionsByTitle(@PathParam("eventId") String eventId, @PathParam("title") String title) {
        fj.data.List<Session> list = emsServer.getSessionsByTitle(eventId, title);
        return new ArrayList<String>(list.map(sessionToId).toCollection());
    }
}
