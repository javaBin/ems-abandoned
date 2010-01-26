package no.java.ems.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Period;

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
        firePropertyChange("name", this.name, this.name = name);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(final LocalDate date) {
        firePropertyChange("date", this.date, this.date = date);
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        firePropertyChange("rooms", this.rooms, this.rooms = Collections.unmodifiableList(rooms));
    }

    public List<Interval> getTimeslots() {
        return timeslots;
    }

    public void setTimeslots(List<Interval> timeslots) {
        firePropertyChange("timeslots", this.timeslots, this.timeslots = Collections.unmodifiableList(timeslots));
    }

    /**
     * Compares based on the events date.
     */
    public int compareTo(final Event other) {
        return new CompareToBuilder().append(date, other == null ? null : other.getDate()).toComparison();
    }

    public void sync(final Event other) {
        super.sync(other);
        setName(other.getName());
        setDate(other.getDate());
        setRooms(other.getRooms());
        setTimeslots(other.getTimeslots());
    }
}
