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
import fj.data.List;
import fj.data.Option;
import no.java.ems.external.v2.EmsV2F;
import no.java.ems.server.domain.Room;
import no.java.ems.external.v2.RoomListV2;
import no.java.ems.external.v2.RoomV2;
import no.java.ems.external.v2.MIMETypes;
import no.java.ems.server.domain.EmsServer;
import static no.java.ems.server.f.ExternalV2F.roomV2;
import org.apache.commons.lang.Validate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import java.util.ArrayList;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Id $
 */
@Path("2/rooms/")
@Component
@Consumes(MIMETypes.ROOM_MIME_TYPE)
public class RoomResource {

    F2<RoomListV2, RoomV2, RoomListV2> aggregator = new F2<RoomListV2, RoomV2, RoomListV2>() {
        public RoomListV2 f(RoomListV2 listV2, RoomV2 roomV2) {
            listV2.getRoom().add(roomV2);
            return listV2;
        }
    };

    private EmsServer emsServer;

    @Autowired
    public RoomResource(EmsServer emsServer) {
        Validate.notNull(emsServer, "EMS Server may not be null");
        this.emsServer = emsServer;
    }    


    @GET
    @Produces(MIMETypes.ROOM_LIST_MIME_TYPE)
    public JAXBElement<RoomListV2> getRooms() {
        List<Room> rooms = List.iterableList(new ArrayList<Room>());
        Option<JAXBElement<RoomListV2>> option = Option.some(rooms.map(roomV2).
                foldLeft(aggregator, new RoomListV2())).
                map(EmsV2F.roomListJaxbElement);
        return option.some();
    }

    @GET
    @Path("{roomId}")
    @Produces(MIMETypes.ROOM_MIME_TYPE)
    public JAXBElement<RoomV2> getRoom(@PathParam("roomId") String roomId) {
        Room room = new Room();
        Option<JAXBElement<RoomV2>> option = Option.some(room).
                map(roomV2).
                map(EmsV2F.roomJaxbElement);
        if (option.isSome()) {
            return option.some();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @POST
    public void addRoom(RoomV2 entity) {
        //return Response.created(URI.create(entity.getId())).build();
    }

    @PUT
    @Path("{roomId}")
    public void saveRoom(@PathParam("roomId") String roomId, RoomV2 entity) {

    }
}