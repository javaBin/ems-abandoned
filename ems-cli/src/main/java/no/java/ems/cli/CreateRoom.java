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
package no.java.ems.cli;

import no.java.ems.client.RoomClient;
import no.java.ems.domain.Room;
import org.apache.commons.cli.Options;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @author last modified by $Author: $
 * @version $Id: $
 */
public class CreateRoom extends AbstractCli {
    private static final String OPTION_ROOM_NAME = "room-name";
    private static final String OPTION_EVENT_ID = "event-id";

    public static void main(String[] args) throws Exception {
        new CreateRoom().doMain(args);
    }


    @Override
    protected Options addOptions(Options defaultOptions) {
        defaultOptions.addOption(null, OPTION_ROOM_NAME, true, "Room Name");
        defaultOptions.addOption(null, OPTION_EVENT_ID, true, "Event Id");
        return defaultOptions;
    }

    protected CreateRoom() {
        super("create-room");
    }

    protected void work() throws Exception {
        if (!assertIsPresent(OPTION_ROOM_NAME) ||
                !assertIsPresent(OPTION_EVENT_ID)) {
            usage();
            return;
        }
        RoomClient eventsClient = getEms().getRoomClient();

        Room room = new Room();
        room.setName(getCommandLine().getOptionValue(OPTION_ROOM_NAME));
        String eventId = getCommandLine().getOptionValue(OPTION_EVENT_ID);
        eventsClient.createRoom(eventId, room);
        System.err.println("room.getId() = " + room.getId());
    }
}