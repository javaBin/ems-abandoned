package no.java.ems.cli;

import no.java.ems.external.v1.RoomV1;
import org.apache.commons.cli.Options;

import java.net.URI;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
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

        RoomV1 room = new RoomV1();
        room.setName(getCommandLine().getOptionValue(OPTION_ROOM_NAME));
        String eventId = getCommandLine().getOptionValue(OPTION_EVENT_ID);
        URI uri = getEms().addRoom(eventId, room);
        System.err.println("room.getId() = " + uri);
    }
}
