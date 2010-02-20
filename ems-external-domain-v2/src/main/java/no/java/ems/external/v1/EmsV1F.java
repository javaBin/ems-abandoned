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

package no.java.ems.external.v1;

import fj.F;
import fj.F2;
import fj.Unit;
import fj.data.Either;
import org.joda.time.*;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import static javax.xml.datatype.DatatypeConstants.FIELD_UNDEFINED;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class EmsV1F {
    private static final ObjectFactory objectFactory = new ObjectFactory();
    private static final DatatypeFactory datatypeFactory;

    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Error initializing datatype factory");
        }
    }

    // -----------------------------------------------------------------------
    // Date tools
    // -----------------------------------------------------------------------

    public static final F<IntervalV1, Interval> toInterval = new F<IntervalV1, Interval>() {
        public Interval f(IntervalV1 intervalV1) {
            XMLGregorianCalendar c = intervalV1.start;

            DateTimeZone timeZone = DateTimeZone.forTimeZone(c.getTimeZone(FIELD_UNDEFINED));
            ReadableInstant start = new DateTime(c.getYear(), c.getMonth(), c.getDay(), c.getHour(), c.getMinute(),
                    c.getSecond(), c.getMillisecond(), timeZone);

            return new Interval(start, Minutes.minutes(intervalV1.duration).toStandardDuration());
        }
    };

    public static final F<Interval, IntervalV1> toIntervalV1 = new F<Interval, IntervalV1>() {
        public IntervalV1 f(Interval interval) {
            IntervalV1 intervalV1 = objectFactory.createIntervalV1();
            intervalV1.setStart(toXmlGregorianCalendarFromDateTime.f(interval.getStart()));
            intervalV1.setDuration(Minutes.minutesIn(interval).getMinutes());
            return intervalV1;
        }
    };

    public final static F<LocalDate, XMLGregorianCalendar> toXmlGregorianCalendar = new F<LocalDate, XMLGregorianCalendar>() {
        public XMLGregorianCalendar f(LocalDate localDate) {
            int hour, minute, second, millisecond, timezone;
            hour = minute = second = millisecond = timezone = FIELD_UNDEFINED;

            return datatypeFactory.newXMLGregorianCalendar(localDate.getYear(), localDate.getMonthOfYear(),
                    localDate.getDayOfMonth(),
                    hour, minute, second, millisecond, timezone);
        }
    };

    public final static F<DateTime, XMLGregorianCalendar> toXmlGregorianCalendarFromDateTime = new F<DateTime, XMLGregorianCalendar>() {
        public XMLGregorianCalendar f(DateTime localDate) {
            return datatypeFactory.newXMLGregorianCalendar(
                    localDate.getYear(),
                    localDate.getMonthOfYear(),
                    localDate.getDayOfMonth(),
                    localDate.getHourOfDay(),
                    localDate.getMinuteOfHour(),
                    localDate.getSecondOfMinute(),
                    localDate.getMillisOfSecond(),
                    FIELD_UNDEFINED
            );
        }
    };

    public final static F<XMLGregorianCalendar, LocalDate> toLocalDate = new F<XMLGregorianCalendar, LocalDate>() {
        public LocalDate f(XMLGregorianCalendar calendar) {
            return LocalDate.fromCalendarFields(calendar.toGregorianCalendar());
        }
    };

    public final static F<XMLGregorianCalendar, LocalDateTime> toLocalDateTime = new F<XMLGregorianCalendar, LocalDateTime>() {
        public LocalDateTime f(XMLGregorianCalendar calendar) {
            return LocalDateTime.fromCalendarFields(calendar.toGregorianCalendar());
        }
    };

    public static final F2<EventV1, XMLGregorianCalendar, Unit> setDate = new F2<EventV1, XMLGregorianCalendar, Unit>() {
        public Unit f(EventV1 eventV1, XMLGregorianCalendar xmlGregorianCalendar) {
            eventV1.setDate(xmlGregorianCalendar);
            return Unit.unit();
        }
    };

    // -----------------------------------------------------------------------
    // JaxbElement creators
    // -----------------------------------------------------------------------

    public static final F<EventV1, JAXBElement<EventV1>> eventJaxbElement = new F<EventV1, JAXBElement<EventV1>>() {
        public JAXBElement<EventV1> f(EventV1 eventV1) {
            return objectFactory.createEvent(eventV1);
        }
    };

    public static final F<EventListV1, JAXBElement<EventListV1>> eventListJaxbElement = new F<EventListV1, JAXBElement<EventListV1>>() {
        public JAXBElement<EventListV1> f(EventListV1 eventListV1) {
            return objectFactory.createEvents(eventListV1);
        }
    };

    public static final F<PersonV1, JAXBElement<PersonV1>> personJaxbElement = new F<PersonV1, JAXBElement<PersonV1>>() {
        public JAXBElement<PersonV1> f(PersonV1 personV1) {
            return objectFactory.createPerson(personV1);
        }
    };

    public static final F<PersonListV1, JAXBElement<PersonListV1>> personListJaxbElement = new F<PersonListV1, JAXBElement<PersonListV1>>() {
        public JAXBElement<PersonListV1> f(PersonListV1 personListV1) {
            return objectFactory.createPeople(personListV1);
        }
    };

    public static final F<RoomV1, JAXBElement<RoomV1>> roomJaxbElement = new F<RoomV1, JAXBElement<RoomV1>>() {
        public JAXBElement<RoomV1> f(RoomV1 eventV1) {
            return objectFactory.createRoom(eventV1);
        }
    };

    public static final F<RoomListV1, JAXBElement<RoomListV1>> roomListJaxbElement = new F<RoomListV1, JAXBElement<RoomListV1>>() {
        public JAXBElement<RoomListV1> f(RoomListV1 roomListV1) {
            return objectFactory.createRooms(roomListV1);
        }
    };

    public static final F<SessionV1, JAXBElement<SessionV1>> sessionJaxbElement = new F<SessionV1, JAXBElement<SessionV1>>() {
        public JAXBElement<SessionV1> f(SessionV1 sessionV1) {
            return objectFactory.createSession(sessionV1);
        }
    };

    public static F<SessionListV1, JAXBElement<SessionListV1>> sessionListJaxbElement = new F<SessionListV1, JAXBElement<SessionListV1>>() {
        public JAXBElement<SessionListV1> f(SessionListV1 sessionListV1) {
            return objectFactory.createSessions(sessionListV1);
        }
    };

    // -----------------------------------------------------------------------
    // Random stuff
    // -----------------------------------------------------------------------

    public static <A> A throwException(Either<Exception, A> either) throws Exception {
        return throwE(either);
    }

    public static <E extends Exception, A> A throwE(Either<E, A> either) throws E {
        if ( either.isLeft() ) {
            throw either.left().value();
        }

        return either.right().value();
    }
}
