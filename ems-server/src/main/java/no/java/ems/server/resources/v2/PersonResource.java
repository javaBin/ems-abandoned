/*
 * Copyright 2009 JavaBin
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package no.java.ems.server.resources.v2;

import fj.F;
import fj.F2;
import static fj.Function.curry;
import fj.data.List;
import fj.data.Option;
import static fj.data.Option.some;
import no.java.ems.external.v2.*;
import no.java.ems.server.URIBuilder;
import no.java.ems.server.domain.*;
import no.java.ems.server.f.ExternalV2F;
import static no.java.ems.server.f.ExternalV2F.personV2;
import static no.java.ems.server.resources.v2.ResourcesF.getEntity;

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
@Path("2/people/")
@Component
@Consumes(MIMETypes.PERSON_MIME_TYPE)
public class PersonResource {

    F2<PersonListV2, PersonV2, PersonListV2> aggregator = new F2<PersonListV2, PersonV2, PersonListV2>() {
        public PersonListV2 f(PersonListV2 listV2, PersonV2 personV2) {
            listV2.getPerson().add(personV2);
            return listV2;
        }
    };
    private URIBuilder uriBuilder;

    static <A> F<JAXBElement<A>, GenericEntity<JAXBElement<A>>> getEntity_() {
        return new F<JAXBElement<A>, GenericEntity<JAXBElement<A>>>() {
            public GenericEntity<JAXBElement<A>> f(JAXBElement<A> ajaxbElement) {
                return getEntity(ajaxbElement);
            }
        };
    }    

    private EmsServer emsServer;

    @Autowired
    public PersonResource(EmsServer emsServer) {
        Validate.notNull(emsServer, "EMS Server may not be null");
        this.emsServer = emsServer;
    }

    @GET
    @Produces(MIMETypes.PERSON_LIST_MIME_TYPE)
    public Response getPeople(@Context UriInfo info) {
        uriBuilder = new URIBuilder(info.getBaseUriBuilder());
        List<Person> people = emsServer.getPeople();
        return some(people.
                map(personV2).
                map(personURIsV2).
                foldLeft(aggregator, new PersonListV2())).
                map(EmsV2F.personListJaxbElement).
                map(curry(ResourcesF.<PersonListV2, Person>multipleOkResponseBuilder(), people)).
                orSome(ResourcesF.notFound).build();
    }

    @GET
    @Path("{personId}")
    @Produces(MIMETypes.PERSON_MIME_TYPE)
    public Response getPerson(@Context UriInfo info, @Context Request request, @PathParam("personId") String personId) {
        uriBuilder = new URIBuilder(info.getBaseUriBuilder());
        final Option<Person> personOption = emsServer.getPerson(personId);
        return personOption.
                map(personV2).
                map(personURIsV2).
                map(EmsV2F.personJaxbElement).
                map(curry(ResourcesF.<PersonV2>singleResponseBuilderWithTagChecking(), personOption, request)).
                map(curry(photoAlternateURI, personId)).
                orSome(ResourcesF.notFound).build();
    }

    @POST
    public Response addPerson(@Context UriInfo info, PersonV2 entity) {
        uriBuilder = new URIBuilder(info.getBaseUriBuilder());
        Option<Person> personOption = some(entity).map(ExternalV2F.person);
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
    public Response addPhoto(@Context UriInfo info,
                             @Context HttpHeaders headers,
                             @HeaderParam("Content-Disposition") String dispositionHeader,
                             @PathParam("{personId}") String personId,
                             InputStream entity) {
        uriBuilder = new URIBuilder(info.getBaseUriBuilder());
        Option<Person> personOption = emsServer.getPerson(personId);
        MediaType type = headers.getMediaType();
        if (personOption.isSome()) {
            String filename = ResourcesF.getFileName(dispositionHeader);
            if (filename != null) {
                Binary binary = emsServer.createBinary(entity, filename, type.toString());
                Person person = personOption.some();
                person.setPhoto(binary);
                emsServer.savePerson(person);
                return Response.created(uriBuilder.binaries().binary(binary.getId())).build();
            } else {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }
        }
        return ResourcesF.notFound._1().build();
    }

    @PUT
    @Path("{personId}")
    public Response savePerson(
            @Context UriInfo info,
            @PathParam("personId") String personId,
            @Context HttpHeaders headers,
            PersonV2 entity) {
        uriBuilder = new URIBuilder(info.getBaseUriBuilder());
        Option<Person> option = emsServer.getPerson(personId);
        if (option.isSome()) {
            Person person = option.some();
            if (ResourcesF.matches(person, headers)) {
                Option<Person> personOption = some(entity).map(ExternalV2F.person);
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


    F<PersonV2, PersonV2> personURIsV2 = new F<PersonV2, PersonV2>() {
        public PersonV2 f(PersonV2 personV2) {
            personV2.setUri(uriBuilder.people().person(personV2.getUuid()).toString());
            URIBinaryV2 photo = personV2.getPhoto();
            if (photo != null) {
                photo.setUri(uriBuilder.binaries().binary(photo.getUri()).toString());
            }            
            return personV2;
        }
    };

    F2<String, Response.ResponseBuilder, Response.ResponseBuilder> photoAlternateURI = new F2<String, Response.ResponseBuilder, Response.ResponseBuilder>() {
        public Response.ResponseBuilder f(String personId, Response.ResponseBuilder responseBuilder) {            
            responseBuilder.header("Link", String.format("<%s>;rel=photo", uriBuilder.people().personWithPhoto(personId)));
            return responseBuilder;
        }
    };    
}
