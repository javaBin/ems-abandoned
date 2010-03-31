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

package no.java.ems.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.net.URI;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class Event extends AbstractEntity implements Comparable<Event> {

    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Room> rooms = new ArrayList<Room>();
    private Venue venue = new Venue("Oslo Spektrum");
    private List<Interval> timeslots = new ArrayList<Interval>();
    private URI sessionURI;

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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(final LocalDate startDate) {
        firePropertyChange("startDate", this.startDate, this.startDate = startDate);
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
        return new CompareToBuilder().append(startDate, other == null ? null : other.getStartDate()).toComparison();
    }

    public void sync(final Event other) {
        super.sync(other);
        setName(other.getName());
        setStartDate(other.getStartDate());
        setEndDate(other.getEndDate());
        setRooms(other.getRooms());
        setTimeslots(other.getTimeslots());
    }

    public URI getSessionURI() {
        return sessionURI;
    }

    public void setSessionURI(URI sessionURI) {
        firePropertyChange("sessionURI", this.sessionURI, this.sessionURI = sessionURI);
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        firePropertyChange("endDate", this.endDate, this.endDate = endDate);
    }
}
