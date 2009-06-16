package no.java.ems.server.resources.v1;

import fj.F;
import fj.F2;
import static fj.Function.curry;
import fj.data.List;
import fj.data.Option;
import static fj.data.Option.some;
import no.java.ems.external.v1.EmsV1F;
import no.java.ems.external.v1.PersonListV1;
import no.java.ems.external.v1.PersonV1;
import no.java.ems.external.v1.URIBinaryV1;
import no.java.ems.server.domain.*;
import no.java.ems.server.f.ExternalV1F;
import static no.java.ems.server.f.ExternalV1F.personV1;
import static no.java.ems.server.resources.v1.ResourcesF.getEntity;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.JAXBElement;
import java.io.InputStream;
import java.net.URI;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Id $
 */
@Path("1/people/")
@Component
@Consumes("application/xml;type=person")
public class PersonResource {

    F2<PersonListV1, PersonV1, PersonListV1> aggregator = new F2<PersonListV1, PersonV1, PersonListV1>() {
        public PersonListV1 f(PersonListV1 listV1, PersonV1 personV1) {
            listV1.getPerson().add(personV1);
            return listV1;
        }
    };

    static <A> F<JAXBElement<A>, GenericEntity<JAXBElement<A>>> getEntity_() {
        return new F<JAXBElement<A>, GenericEntity<JAXBElement<A>>>() {
            public GenericEntity<JAXBElement<A>> f(JAXBElement<A> ajaxbElement) {
                return getEntity(ajaxbElement);
            }
        };
    }    

    private EmsServer emsServer;
    private UriInfo uriInfo;

    @Autowired
    public PersonResource(EmsServer emsServer) {
        Validate.notNull(emsServer, "EMS Server may not be null");
        this.emsServer = emsServer;
    }

    @GET
    @Produces("application/xml;type=person-list")
    public Response getPeople() {
        List<Person> people = emsServer.getPeople();
        return some(people.
                map(personV1).
                map(personURIsV1).
                foldLeft(aggregator, new PersonListV1())).
                map(EmsV1F.personListJaxbElement).
                map(curry(ResourcesF.<PersonListV1, Person>multipleOkResponseBuilder(), people)).
                orSome(ResourcesF.notFound).build();
    }

    @GET
    @Path("{personId}")
    @Produces("application/xml;type=person")
    public Response getPerson(@Context Request request, @PathParam("personId") String personId) {
        final Option<Person> personOption = emsServer.getPerson(personId);
        return personOption.
                map(personV1).
                map(personURIsV1).
                map(EmsV1F.personJaxbElement).
                map(curry(ResourcesF.<PersonV1>singleResponseBuilderWithTagChecking(), personOption, request)).
                map(curry(photoAlternateURI, personId)).
                orSome(ResourcesF.notFound).build();
    }

    @POST
    public Response addPerson(PersonV1 entity) {
        Option<Person> personOption = some(entity).map(ExternalV1F.person);
        if (personOption.isSome()) {
            Person person = personOption.some();
            emsServer.savePerson(person);
            return Response.created(URI.create(person.getId())).build();
        }
        return Response.serverError().build();
    }

    @POST
    @Path("{personId}/photo")
    @Consumes("image/*")
    public Response addPhoto(@Context HttpHeaders headers,
                             @HeaderParam("Content-Disposition") String dispositionHeader,
                             @PathParam("{personId}") String personId,
                             InputStream entity) {
        Option<Person> personOption = emsServer.getPerson(personId);
        MediaType type = headers.getMediaType();
        if (personOption.isSome()) {
            String filename = ResourcesF.getFileName(dispositionHeader);
            if (filename != null) {
                Binary binary = emsServer.createBinary(entity, filename, type.toString());
                Person person = personOption.some();
                person.setPhoto(binary);
                emsServer.savePerson(person);
                return Response.created(uriInfo.getBaseUriBuilder().path("binaries/{binaryId}").build(binary.getId())).build();
            } else {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }
        }
        return ResourcesF.notFound._1().build();
    }

    @PUT
    @Path("{personId}")
    public Response savePerson(
            @PathParam("personId") String personId,
            @Context HttpHeaders headers,
            PersonV1 entity) {
        Option<Person> option = emsServer.getPerson(personId);
        if (option.isSome()) {
            Person person = option.some();
            if (ResourcesF.matches(person, headers)) {
                Option<Person> personOption = some(entity).map(ExternalV1F.person);
                person.sync(personOption.some());
                emsServer.savePerson(person);
                return Response.ok().build();
            }
            else {
                Response.ResponseBuilder builder = Response.status(Response.Status.PRECONDITION_FAILED);
                builder.tag(Integer.toHexString(person.getRevision()));
                return builder.build();
            }
        }
        return ResourcesF.notFound._1().build();
    }


    F<PersonV1, PersonV1> personURIsV1 = new F<PersonV1, PersonV1>() {
        public PersonV1 f(PersonV1 personV1) {
            personV1.setUrl(uriInfo.getBaseUriBuilder().path("/1/people/{personId}").build(personV1.getUuid()).toString());
            URIBinaryV1 photo = personV1.getPhoto();
            if (photo != null) {
                photo.setUri(uriInfo.getBaseUriBuilder().path("/binaries/{binaryId}").build(photo.getUri()).toString());
            }            
            return personV1;
        }
    };

    F2<String, Response.ResponseBuilder, Response.ResponseBuilder> photoAlternateURI = new F2<String, Response.ResponseBuilder, Response.ResponseBuilder>() {
        public Response.ResponseBuilder f(String personId, Response.ResponseBuilder responseBuilder) {
            responseBuilder.header("Link", String.format("<%s>;rel=photo", uriInfo.getBaseUriBuilder().path("/1/people/{personId}/photo").build(personId)));
            return responseBuilder;
        }
    };    

    @Context
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }
}
