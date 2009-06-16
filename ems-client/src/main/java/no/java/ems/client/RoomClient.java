/* 
 * $Header: $ 
 * 
 * Copyright (C) 2008 Escenic. 
 * All Rights Reserved.  No use, copying or distribution of this 
 * work may be made except in accordance with a valid license 
 * agreement from Escenic.  This notice must be 
 * included on all copies, modifications and derivatives of this 
 * work. 
 */
package no.java.ems.client;

import no.java.ems.domain.Room;
import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ObjectRepresentation;

import java.util.List;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @author last modified by $Author: $
 * @version $Id: $
 */
public class RoomClient extends AbstractClient<Room> {
    private static final String ROOMS = "/rooms/";
    private static final String EVENTS = "/events/";

    RoomClient(String baseUri, Client client) {
        super(baseUri, client);
    }

    public List<Room> listRooms() {
        Request request = new Request(Method.GET, baseUri);
        Response response = doRequest(request);
        //noinspection unchecked
        return deserialize(response, List.class);
    }


    public Room get(String id) {
        StringBuilder builder = new StringBuilder(baseUri);
        builder.append(ROOMS).append(id);
        Request request = new Request(Method.GET, builder.toString());
        Response response = doRequest(request);
        return deserialize(response, Room.class);
    }

    public void createRoom(String eventId, Room room) {
        StringBuilder builder = new StringBuilder(baseUri);
        if (eventId != null) {
            builder.append(EVENTS).append(eventId);
        }
        builder.append(ROOMS);
        Request request = new Request(Method.POST, builder.toString());
        request.setEntity(new ObjectRepresentation(room));
        Response response = doRequest(request);
        room.setId(extractId(response));
    }

    public void updateRoom(String eventId, Room room) {
        StringBuilder builder = new StringBuilder(baseUri);
        if (eventId != null) {
            builder.append(EVENTS).append(eventId);
        }
        builder.append(ROOMS).append(room.getId());
        Request request = new Request(Method.PUT, builder.toString());
        request.setEntity(new ObjectRepresentation(room));
        doRequest(request);
    }
}