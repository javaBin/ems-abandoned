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

import fj.F2;
import fj.F;

import static fj.Function.curry;

import fj.data.Option;
import fj.data.List;

import static fj.data.Option.some;

import no.java.ems.external.v2.*;
import no.java.ems.server.URIBuilder;
import no.java.ems.server.domain.*;
import no.java.ems.server.f.ExternalV2F;

import static no.java.ems.server.f.ExternalV2F.eventV2;

import org.apache.commons.lang.Validate;
import org.codehaus.httpcache4j.LinkDirectiveBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.net.URI;

@Path("2/events/")
@Component
@Produces(MIMETypes.EVENT_MIME_TYPE)
@Consumes(MIMETypes.EVENT_MIME_TYPE)
public class EventResource {

    private final EmsServer emsServer;
    private URIBuilder uriBuilder;

    @Autowired
    public EventResource(EmsServer emsServer) {
        Validate.notNull(emsServer, "Ems server may not be null");
        this.emsServer = emsServer;
    }

    @GET
    @Produces(MIMETypes.EVENT_LIST_MIME_TYPE)
    public Response getEvents(@Context UriInfo info) {
        uriBuilder = new URIBuilder(info.getBaseUriBuilder());
        List<Event> list = emsServer.getEvents();
        return some(list.
                map(eventV2).
                map(eventIdV2).
                map(eventRoomURLV2).
                map(sessionsURLV2).
                foldLeft(aggregator, new EventListV2())).
                map(EmsV2F.eventListJaxbElement).
                map(curry(ResourcesF.<EventListV2, Event>multipleOkResponseBuilder(), list)).
                orSome(ResourcesF.notFound).build();
    }

    @GET
    @Path("{eventId}")
    public Response getEvent(@Context UriInfo info, @Context Request request, @PathParam("eventId") String id) {
        uriBuilder = new URIBuilder(info.getBaseUriBuilder());
        Option<Event> event = emsServer.getEventOption(id);
        Response.ResponseBuilder builder = event.
                map(eventV2).
                map(eventIdV2).
                map(eventRoomURLV2).
                map(sessionsURLV2).
                map(EmsV2F.eventJaxbElement).
                map(curry(ResourcesF.<EventV2>singleResponseBuilderWithTagChecking(), event, request)).
                map(curry(sessionsLinkHeader, id)).
                some();
        return builder.build();
    }

    @POST
    public Response addEvent(@Context UriInfo info, EventV2 entity) {
        uriBuilder = new URIBuilder(info.getBaseUriBuilder());
        Event input = ExternalV2F.event.f(entity);
        emsServer.saveEvent(input);
        return Response.created(uriBuilder.forObject(input)).build();
    }

    @PUT
    @Path("{eventId}")
    public Response saveEvent(
            @Context UriInfo info,
            @PathParam("eventId") String id,
            @Context HttpHeaders headers,
            EventV2 entity) {
        uriBuilder = new URIBuilder(info.getBaseUriBuilder());
        Response.ResponseBuilder response;
        Option<Event> eventOption = emsServer.getEventOption(id);
        if (eventOption.isSome()) {
            Event original = eventOption.some();
            if (!ResourcesF.matches(original, headers)) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }
            Event input = some(entity).
                    map(eventRoomID).
                    map(ExternalV2F.event).some();

            original.sync(input);
            emsServer.saveEvent(original);
            response = Response.ok();
        }
        else {
            response = Response.serverError();
        }

        return response.build();
    }


    @Path("{eventId}/sessions")
    public SessionResource getSessionResource(@Context UriInfo info) {
        uriBuilder = new URIBuilder(info.getBaseUriBuilder());
        return new SessionResource(emsServer, uriBuilder);
    }

    // -----------------------------------------------------------------------
    // -----------------------------------------------------------------------
    // Helpers

    F2<EventListV2, EventV2, EventListV2> aggregator = new F2<EventListV2, EventV2, EventListV2>() {
        public EventListV2 f(EventListV2 eventListV2, EventV2 eventV2) {
            eventListV2.getEvent().add(eventV2);
            return eventListV2;
        }
    };

    private F<EventV2, EventV2> eventIdV2 = new F<EventV2, EventV2>() {
        public EventV2 f(EventV2 eventV2) {
            eventV2.setUri(uriBuilder.events().eventUri(eventV2.getUuid()).toString());
            return eventV2;
        }
    };

    private F<EventV2, EventV2> eventRoomURLV2 = new F<EventV2, EventV2>() {
        public EventV2 f(EventV2 eventV2) {
            if (eventV2.getRooms() != null) {
                for (RoomV2 roomV2 : eventV2.getRooms().getRoom()) {
                    roomV2.setUri(uriBuilder.rooms().room(roomV2.getUuid()).toString());
                }
            }
            return eventV2;
        }
    };

    private F<EventV2, EventV2> eventRoomID = new F<EventV2, EventV2>() {
        public EventV2 f(EventV2 eventV2) {
            if (eventV2.getRooms() != null) {
                for (RoomV2 roomV2 : eventV2.getRooms().getRoom()) {
                    URI roomURI = uriBuilder.rooms().rooms();
                    URI uri = roomURI.relativize(URI.create(roomV2.getUri()));
                    roomV2.setUuid(uri.toString());
                }
            }
            return eventV2;
        }
    };

    private F<EventV2, EventV2> sessionsURLV2 = new F<EventV2, EventV2>() {
        public EventV2 f(EventV2 eventV2) {
            eventV2.setSessionsUri(uriBuilder.sessions().sessions(eventV2.getUuid()).toString());
            return eventV2;
        }
    };

    private F2<String, Response.ResponseBuilder, Response.ResponseBuilder> sessionsLinkHeader = new F2<String, Response.ResponseBuilder, Response.ResponseBuilder>() {
        public Response.ResponseBuilder f(String eventId, Response.ResponseBuilder responseBuilder) {
            URI value = uriBuilder.sessions().sessions(eventId);
            LinkDirectiveBuilder directiveBuilder = LinkDirectiveBuilder.create(value);
            responseBuilder.header("Link", directiveBuilder.build().toString());
            return responseBuilder;
        }
    };
}
