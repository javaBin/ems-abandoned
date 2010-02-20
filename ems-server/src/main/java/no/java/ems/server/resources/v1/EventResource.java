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

import fj.F2;
import fj.F;
import static fj.Function.curry;
import fj.data.Option;
import fj.data.List;
import static fj.data.Option.some;
import no.java.ems.external.v1.*;
import no.java.ems.server.URIBuilder;
import no.java.ems.server.domain.*;
import no.java.ems.server.f.ExternalV1F;
import static no.java.ems.server.f.ExternalV1F.eventV1;
import org.apache.commons.lang.Validate;
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
                map(eventV1).
                map(eventIdV1).
                map(eventRoomURLV1).
                foldLeft(aggregator, new EventListV1())).
                map(EmsV1F.eventListJaxbElement).
                map(curry(ResourcesF.<EventListV1, Event>multipleOkResponseBuilder(), list)).
                orSome(ResourcesF.notFound).build();
    }

    @GET
    @Path("{eventId}")
    public Response getEvent(@Context UriInfo info, @Context Request request, @PathParam("eventId") String id) {
        uriBuilder = new URIBuilder(info.getBaseUriBuilder());
        Option<Event> event = emsServer.getEventOption(id);
        Response.ResponseBuilder builder = event.
                map(eventV1).
                map(eventIdV1).
                map(eventRoomURLV1).
                map(EmsV1F.eventJaxbElement).
                map(curry(ResourcesF.<EventV1>singleResponseBuilderWithTagChecking(), event, request)).
                some();
        builder = builder.header("Link", String.format("<%s>;rel=sessions", uriBuilder.sessions().sessions(id)));
        return builder.build();
    }

    @POST
    public Response addEvent(@Context UriInfo info, EventV1 entity) {
        uriBuilder = new URIBuilder(info.getBaseUriBuilder());
        Event input = ExternalV1F.event.f(entity);
        emsServer.saveEvent(input);
        return Response.created(uriBuilder.forObject(input)).build();
    }

    @PUT
    @Path("{eventId}")
    public Response saveEvent(
            @Context UriInfo info,
            @PathParam("eventId") String id,
            @Context HttpHeaders headers,
            EventV1 entity) {
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
                    map(ExternalV1F.event).some(); 

            original.sync(input);
            emsServer.saveEvent(original);
            response = Response.ok();
        } else {
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

    F2<EventListV1, EventV1, EventListV1> aggregator = new F2<EventListV1, EventV1, EventListV1>() {
        public EventListV1 f(EventListV1 eventListV1, EventV1 eventV1) {
            eventListV1.getEvent().add(eventV1);
            return eventListV1;
        }
    };

    private F<EventV1, EventV1> eventIdV1 = new F<EventV1, EventV1>() {
        public EventV1 f(EventV1 eventV1) {
            eventV1.setUri(uriBuilder.events().eventUri(eventV1.getUuid()).toString());
            return eventV1;
        }
    };
  
    private F<EventV1, EventV1> eventRoomURLV1 = new F<EventV1, EventV1>() {
        public EventV1 f(EventV1 eventV1) {
            if (eventV1.getRooms() != null) {
                for (RoomV1 roomV1 : eventV1.getRooms().getRoom()) {
                    roomV1.setUri(uriBuilder.rooms().room(roomV1.getUuid()).toString());
                }
            }
            return eventV1;
        }
    };

    private F<EventV1 , EventV1> eventRoomID = new F<EventV1, EventV1>() {
        public EventV1 f(EventV1 eventV1) {
            if (eventV1.getRooms() != null) {
                for (RoomV1 roomV1 : eventV1.getRooms().getRoom()) {
                    URI roomURI = uriBuilder.rooms().rooms();
                    URI uri = roomURI.relativize(URI.create(roomV1.getUri()));
                    roomV1.setUuid(uri.toString());
                }
            }
            return eventV1;
        }
    };
}
