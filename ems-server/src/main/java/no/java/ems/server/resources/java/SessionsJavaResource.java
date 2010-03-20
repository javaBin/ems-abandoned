package no.java.ems.server.resources.java;

import fj.data.Option;
import no.java.ems.server.domain.EmsServer;
import no.java.ems.server.domain.Session;

import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
@Produces("application/x-java-serialized-object")
@Consumes("application/x-java-serialized-object")
public class SessionsJavaResource {
    private final EmsServer emsServer;

    public SessionsJavaResource(EmsServer emsServer) {
        this.emsServer = emsServer;
    }

    @GET
    public List<Object> getSessions(@PathParam("eventId") String eventId) {
        List<Object> sessions = new ArrayList<Object>();

        fj.data.List<Session> sessionList = emsServer.getSessions(eventId);
        sessions.addAll(sessionList.map(ExternalEmsDomainJavaF.sessionToExternal).toCollection());
        return sessions;
    }

    @GET
    @Path("{sessionId}")
    public Object getSession(@PathParam("eventId") String eventId, @PathParam("sessionId") String sessionId) {
        Option<no.java.ems.domain.Session> option = emsServer.getSession(eventId, sessionId).map(ExternalEmsDomainJavaF.sessionToExternal);
        if (option.isSome()) {
            return option.some();
        }
        throw new WebApplicationException(404);
    }


}
