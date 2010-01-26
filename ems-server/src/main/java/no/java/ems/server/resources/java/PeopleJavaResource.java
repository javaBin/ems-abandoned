package no.java.ems.server.resources.java;

import no.java.ems.server.domain.*;
import static no.java.ems.server.resources.ResourceUtil.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import javax.ws.rs.*;
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

        List<Object> people = new ArrayList<Object>();

        for (Person person: emsServer.getPeople()) {
            people.add(ExternalEmsDomainJavaF.personToExternal.f(person));
        }

        return people;
    }
}
