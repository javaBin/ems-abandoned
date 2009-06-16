package no.java.ems.server.resources.v1;

import fj.F2;
import fj.data.List;
import fj.data.Option;
import no.java.ems.server.domain.Room;
import no.java.ems.external.v1.EmsV1F;
import no.java.ems.external.v1.RoomListV1;
import no.java.ems.external.v1.RoomV1;
import no.java.ems.server.domain.EmsServer;
import static no.java.ems.server.f.ExternalV1F.roomV1;
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
@Path("1/rooms/")
@Component
@Consumes("application/xml;type=room")
public class RoomResource {

    F2<RoomListV1, RoomV1, RoomListV1> aggregator = new F2<RoomListV1, RoomV1, RoomListV1>() {
        public RoomListV1 f(RoomListV1 listV1, RoomV1 roomV1) {
            listV1.getRoom().add(roomV1);
            return listV1;
        }
    };

    private EmsServer emsServer;

    @Autowired
    public RoomResource(EmsServer emsServer) {
        Validate.notNull(emsServer, "EMS Server may not be null");
        this.emsServer = emsServer;
    }    


    @GET
    @Produces("application/xml;type=room-list")
    public JAXBElement<RoomListV1> getRooms() {
        List<Room> rooms = List.iterableList(new ArrayList<Room>());
        Option<JAXBElement<RoomListV1>> option = Option.some(rooms.map(roomV1).
                foldLeft(aggregator, new RoomListV1())).
                map(EmsV1F.roomListJaxbElement);        
        return option.some();
    }

    @GET
    @Path("{roomId}")
    @Produces("application/xml;type=room")
    public JAXBElement<RoomV1> getRoom(@PathParam("roomId") String roomId) {
        Room room = new Room();
        Option<JAXBElement<RoomV1>> option = Option.some(room).
                map(roomV1).
                map(EmsV1F.roomJaxbElement);
        if (option.isSome()) {
            return option.some();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @POST
    public void addRoom(RoomV1 entity) {
        //return Response.created(URI.create(entity.getId())).build();
    }

    @PUT
    @Path("{roomId}")
    public void saveRoom(@PathParam("roomId") String roomId, RoomV1 entity) {

    }
}