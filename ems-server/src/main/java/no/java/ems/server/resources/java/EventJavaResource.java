package no.java.ems.server.resources.java;

import fj.data.Option;
import no.java.ems.domain.*;
import no.java.ems.server.domain.*;
import no.java.ems.server.domain.Event;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import javax.ws.rs.*;
import java.util.*;

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

    @Autowired
    public EventJavaResource(EmsServer emsServer) {
        this.emsServer = emsServer;
    }

    @GET
    public List<Object> getEvents() {

        List<Object> events = new ArrayList<Object>();

        for (Event event : emsServer.getEvents()) {
            System.out.println("event.getId() = " + event.getId());
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
}
