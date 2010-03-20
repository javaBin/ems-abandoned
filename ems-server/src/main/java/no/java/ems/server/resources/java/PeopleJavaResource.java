package no.java.ems.server.resources.java;

import fj.data.Option;
import no.java.ems.server.domain.*;

import static fj.data.Option.some;
import static no.java.ems.server.resources.ResourceUtil.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.*;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Path("people")
@Component
@Produces(APPLICATION_X_JAVA_SERIALIZED_OBJECT_STRING)
@Consumes(APPLICATION_X_JAVA_SERIALIZED_OBJECT_STRING)
public class PeopleJavaResource {
    private final EmsServer emsServer;

    @Autowired
    public PeopleJavaResource(EmsServer emsServer) {
        this.emsServer = emsServer;
    }

    @GET
    public Object getPeople() {
        return new ArrayList<Object>(emsServer.getPeople().map(ExternalEmsDomainJavaF.personToExternal).toCollection());
    }

    @POST
    public Response create(no.java.ems.domain.Person person) {
        Person personOption = some(person).map(ExternalEmsDomainJavaF.externalToPerson).some(); 
        emsServer.savePerson(personOption);
        return Response.created(URI.create(personOption.getId())).build();
    }

    @PUT
    @Path("{personId}")
    public Response create(@PathParam("personId") String personId, no.java.ems.domain.Person person) {
        Person personOption = some(person).map(ExternalEmsDomainJavaF.externalToPerson).some();
        emsServer.savePerson(personOption);
        return Response.ok().build();
    }
}
