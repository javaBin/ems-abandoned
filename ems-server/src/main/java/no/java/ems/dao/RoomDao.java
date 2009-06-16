package no.java.ems.dao;

import no.java.ems.domain.Room;

import java.util.List;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface RoomDao {
    Room getRoom(String id);

    List<Room> getRooms();

    void save(Room room);
}
