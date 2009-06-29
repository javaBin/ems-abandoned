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
