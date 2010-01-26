package no.java.ems.server.resources.java;

import no.java.ems.server.domain.*;
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
    public Object getEvent() {

        List<Object> events = new ArrayList<Object>();

        for (Event event : emsServer.getEvents()) {
            events.add(ExternalEmsDomainJavaF.eventToExternal.f(event));
        }

        return events;
    }
}
