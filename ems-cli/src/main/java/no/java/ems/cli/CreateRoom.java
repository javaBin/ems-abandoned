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

package no.java.ems.cli;

import no.java.ems.client.ResourceHandle;
import no.java.ems.external.v2.RoomV2;
import org.apache.commons.cli.Options;

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

        RoomV2 room = new RoomV2();
        room.setName(getCommandLine().getOptionValue(OPTION_ROOM_NAME));
        String eventId = getCommandLine().getOptionValue(OPTION_EVENT_ID);
        //ResourceHandle uri = getEms().addRoom(eventId, room);
        //System.err.println("room.getId() = " + uri);
    }
}
