package no.java.ems.domain;

import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class Venue extends AbstractEntity {
    private List<Room> rooms = new ArrayList<Room>();
    private String name;
    private int capacity;

    public Venue(String name) {
        Validate.notNull(name, "Name may not be empty");
        this.name = name;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        firePropertyChange("rooms", this.rooms, this.rooms = rooms);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        firePropertyChange("name", this.name, this.name = name);
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        firePropertyChange("capacity", this.capacity, this.capacity = capacity);
    }
}
