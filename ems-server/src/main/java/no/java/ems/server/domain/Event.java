package no.java.ems.server.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class Event extends AbstractEntity implements Comparable<Event> {

    private String name;
    private LocalDate date;
    private List<Room> rooms = new ArrayList<Room>();
    private List<Interval> timeslots = new ArrayList<Interval>();

    public Event() {
    }

    public Event(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = Collections.unmodifiableList(rooms);
    }

    public List<Interval> getTimeslots() {
        return timeslots;
    }

    public void setTimeslots(List<Interval> timeslots) {
        this.timeslots = Collections.unmodifiableList(timeslots);
    }

    /**
     * Compares based on the events date.
     */
    public int compareTo(final Event other) {
        return new CompareToBuilder().append(date, other == null ? null : other.getDate()).toComparison();
    }

    public void sync(Event other) {
        setName(other.getName());
        setDate(other.getDate());
        setRooms(other.getRooms());
        setTimeslots(other.getTimeslots());        
    }
}
