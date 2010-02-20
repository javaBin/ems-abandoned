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

package no.java.ems.server.resources.v1;

import no.java.ems.server.URIBuilder;
import no.java.ems.server.domain.*;
import static no.java.ems.server.f.ExternalV1F.sessionV1;
import no.java.ems.server.f.ExternalV1F;
import no.java.ems.external.v1.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import fj.data.List;
import fj.data.Option;
import static fj.data.Option.some;
import static fj.Function.curry;
import fj.F2;
import fj.F;
import org.joda.time.LocalDate;

import java.net.URI;
import java.io.InputStream;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
@Produces(MIMETypes.SESSION_LIST_MIME_TYPE)
public class SessionResource {
    private URIBuilder uriBuilder;
    private EmsServer emsServer;

    public SessionResource(EmsServer emsServer, final URIBuilder uriBuilder) {
        this.uriBuilder = uriBuilder;
        this.emsServer = emsServer;
    }

    @GET
    public Response getSessions(@PathParam("eventId") String eventId) {
        List<Session> sessions = emsServer.getSessions(eventId);
        return some(sessions.
                map(sessionV1).
                map(sessionEventIdV1).
                map(sessionPersonIdV1).
                map(curry(sessionIdV1, eventId)).
                foldLeft(sessionAggregator, new SessionListV1())).
                map(EmsV1F.sessionListJaxbElement).
                map(curry(ResourcesF.<SessionListV1, Session>multipleOkResponseBuilder(), sessions)).
                orSome(ResourcesF.notFound).build();
    }
    @GET
    @Produces("text/uri-list")
    public Response getSessionsAsUriList(@PathParam("eventId") String eventId) {
        List<Session> sessions = emsServer.getSessions(eventId);
        StringBuilder builder = new StringBuilder();
        return some(sessions.foldLeft(uriListBuilder, builder)).
                map(ResourcesF.uriListOkResponseBuilder()).
                orSome(ResourcesF.notFound).build();
    }


    @GET
    @Path("by-date/{year}/{month}/{day}")
    //TODO: replace by the JSON thing...
    public Response getSessionsByDate(@PathParam("eventId") String eventId, @PathParam("year") int year, @PathParam("month") int month, @PathParam("day") int day) {
        List<Session> sessions = emsServer.getSessionsByDate(eventId, new LocalDate(year, month, day));
        return some(sessions.
                map(sessionV1).
                map(sessionEventIdV1).
                map(sessionPersonIdV1).
                map(curry(sessionIdV1, eventId)).
                foldLeft(sessionAggregator, new SessionListV1())).
                map(EmsV1F.sessionListJaxbElement).
                map(curry(ResourcesF.<SessionListV1, Session>multipleOkResponseBuilder(), sessions)).
                orSome(ResourcesF.notFound).build();

    }

    @GET
    @Path("by-title/{title}")
    //TODO: replace by the JSON thing...
    public Response getSessionsByTitle(@PathParam("eventId") String eventId, @PathParam("title") String title) {
        List<Session> sessions = emsServer.getSessionsByTitle(eventId, title);
        return some(sessions.
                map(sessionV1).
                map(sessionEventIdV1).
                map(sessionPersonIdV1).
                map(curry(sessionIdV1, eventId)).
                foldLeft(sessionAggregator, new SessionListV1())).
                map(EmsV1F.sessionListJaxbElement).
                map(curry(ResourcesF.<SessionListV1, Session>multipleOkResponseBuilder(), sessions)).
                orSome(ResourcesF.notFound).build();
    }

    @GET
    @Produces(MIMETypes.SESSION_MIME_TYPE)
    @Path("{sessionId}")
    public Response getSession(@Context Request request,
            @PathParam("eventId") String eventId,
            @PathParam("sessionId") String sessionId) {

        Option<Session> sessionOption = emsServer.getSession(eventId, sessionId);        
        return sessionOption.
                map(sessionV1).
                map(sessionEventIdV1).
                map(sessionPersonIdV1).
                map(curry(sessionIdV1, eventId)).
                map(EmsV1F.sessionJaxbElement).
                map(curry(ResourcesF.<SessionV1>singleResponseBuilderWithTagChecking(), sessionOption, request)).
                orSome(ResourcesF.notFound).build();
    }

    @POST
    @Consumes(MIMETypes.SESSION_MIME_TYPE)
    @Produces(MIMETypes.SESSION_MIME_TYPE)
    public Response addSession(@PathParam("eventId") String id, SessionV1 entity) {
        Session input = Option.some(entity).
                map(personURItoId).
                map(ExternalV1F.session).some();

        emsServer.saveSession(id, input);

        URI location = URI.create(input.getId());
        return Response.created(location).build();
    }

    @POST
    @Consumes("*/*")
    @Produces("*/*")
    @Path("{sessionId}/attachment")    
    public Response addAttachment(@PathParam("eventId") String eventId,
                                   @PathParam("sessionId") String sessionId,
                                   @HeaderParam("Content-Disposition") String dispositionHeader,
                                   @Context HttpHeaders headers,
                                   InputStream stream) {
        Option<Session> sessionOption = emsServer.getSession(eventId, sessionId);
        if (sessionOption.isSome()) {
            String filename = ResourcesF.getFileName(dispositionHeader);

            if (filename == null) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }

            Binary binary = emsServer.createBinary(stream, filename, headers.getMediaType().toString());
            Session session = sessionOption.some();
            session.addAttachment(binary);
            emsServer.saveSession(eventId, session);
            return Response.created(uriBuilder.binaries().binary(binary.getId())).build();
        }
        return Response.status(404).build();
    }

    @PUT
    @Consumes(MIMETypes.SESSION_MIME_TYPE)
    @Path("{sessionId}")
    public Response saveSession(
            @PathParam("eventId") String eventId,
            @PathParam("sessionId") String sessionId,
            @Context HttpHeaders headers,
            SessionV1 entity) {
        Response.ResponseBuilder response;
        Option<Session> sessionOption = emsServer.getSession(eventId, sessionId);
        if (sessionOption.isSome()) {
            Session original = sessionOption.some();
            if (ResourcesF.matches(original, headers)) {
                Session input = Option.some(entity).
                        map(personURItoId).
                        map(ExternalV1F.session).some();
                original.sync(input);
                emsServer.saveSession(eventId, original);
                response = Response.ok();
            }
            else {
                response = Response.status(Response.Status.PRECONDITION_FAILED);
                response.tag(Integer.toHexString(original.getRevision()));
            }
        }
        else {
            response = Response.status(404);
        }

        return response.build();
    }

    @POST
    @Path("{sessionId}/speaker/{personId}/photo")
    @Consumes("image/*")
    public Response addPhoto(@PathParam("eventId") String eventId,
                             @PathParam("sessionId") String sessionId,
                             @PathParam("personId") String personId,
                             @HeaderParam("Content-Disposition") String dispositionHeader,
                             @Context HttpHeaders headers,
                             InputStream stream) {
        Option<Session> option = emsServer.getSession(eventId, sessionId);
        if (option.isNone()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        Session session = option.some();
        Speaker found = null;
        for (Speaker speaker : session) {
            if (personId.equals(speaker.getPersonId())) {
                found = speaker;
            }
        }
        if (found == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        String filename = ResourcesF.getFileName(dispositionHeader);

        if (filename == null) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }

        Binary binary = emsServer.createBinary(stream, filename, headers.getMediaType().toString());
        found.setPhoto(binary);
        emsServer.saveSession(eventId, session);
        return Response.created(uriBuilder.binaries().binary(binary.getId())).build();
    }

    private F2<SessionListV1, SessionV1, SessionListV1> sessionAggregator = new F2<SessionListV1, SessionV1, SessionListV1>() {
        public SessionListV1 f(SessionListV1 sessionListV1, SessionV1 sessionV1) {
            sessionListV1.getSession().add(sessionV1);
            return sessionListV1;
        }
    };

    private F2<String, SessionV1, SessionV1> sessionIdV1 = new F2<String, SessionV1, SessionV1>() {
        public SessionV1 f(String eventId, SessionV1 sessionV1) {
            URI uri = uriBuilder.forObject(eventId, sessionV1.getUuid(), ObjectType.session);
            sessionV1.setEventUuid(eventId);
            sessionV1.setUri(uri.toString());
            for (URIBinaryV1 binaryV1 : sessionV1.getAttachments().getBinary()) {
                binaryV1.setUri(uriBuilder.binaries().binary(binaryV1.getUri()).toString());
            }
            return sessionV1;
        }
    };

    private F<SessionV1, SessionV1> sessionEventIdV1 = new F<SessionV1, SessionV1>() {
        public SessionV1 f(SessionV1 session) {
            session.setEventUri(uriBuilder.events().eventUri(session.getEventUuid()).toString());
            return session;
        }
    };

    private F<SessionV1, SessionV1> sessionPersonIdV1 = new F<SessionV1, SessionV1>() {
        public SessionV1 f(SessionV1 session) {
            if (session.getSpeakers() != null) {
                for (SpeakerV1 speakerV1 : session.getSpeakers().getSpeaker()) {
                    speakerV1.setPersonUri(uriBuilder.forObject(null, speakerV1.getPersonUuid(), ObjectType.person).toString());
                    URIBinaryV1 photo = speakerV1.getPhoto();
                    if (photo != null) {
                        photo.setUri(uriBuilder.binaries().binary(photo.getUri()).toString());
                    }
                }
            }
            return session;
        }
    };

    private F<SessionV1, SessionV1> personURItoId = new F<SessionV1, SessionV1>() {
        public SessionV1 f(SessionV1 session) {
            if (session.getSpeakers() != null) {
                for (SpeakerV1 speaker : session.getSpeakers().getSpeaker()) {
                    URI personURI = uriBuilder.people().people();
                    URI uri = personURI.relativize(URI.create(speaker.getPersonUri()));
                    speaker.setPersonUuid(uri.toString());
                }
            }
            return session;
        }
    };

    private F2<StringBuilder, Session, StringBuilder> uriListBuilder = new F2<StringBuilder, Session, StringBuilder>() {
        public StringBuilder f(StringBuilder builder, Session session) {
            URI uri = uriBuilder.forObject(session);
            builder.append(uri).append("\r\n");
            return builder;
        }
    };


}
