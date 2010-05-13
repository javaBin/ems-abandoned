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

package no.java.ems.external.v2;

import fj.F;
import fj.F2;
import fj.Unit;
import fj.data.*;
import static fj.data.List.iterableList;
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
public class EmsV2F {
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

    public static final F<IntervalV2, Interval> toInterval = new F<IntervalV2, Interval>() {
        public Interval f(IntervalV2 intervalV2) {
            XMLGregorianCalendar c = intervalV2.start;

            DateTimeZone timeZone = DateTimeZone.forTimeZone(c.getTimeZone(FIELD_UNDEFINED));
            ReadableInstant start = new DateTime(c.getYear(), c.getMonth(), c.getDay(), c.getHour(), c.getMinute(),
                    c.getSecond(), c.getMillisecond(), timeZone);

            return new Interval(start, Minutes.minutes(intervalV2.duration).toStandardDuration());
        }
    };

    public static final F<Interval, IntervalV2> toIntervalV2 = new F<Interval, IntervalV2>() {
        public IntervalV2 f(Interval interval) {
            IntervalV2 intervalV2 = objectFactory.createIntervalV2();
            intervalV2.setStart(toXmlGregorianCalendarFromDateTime.f(interval.getStart()));
            intervalV2.setDuration(Minutes.minutesIn(interval).getMinutes());
            return intervalV2;
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

    public static final F2<EventV2, XMLGregorianCalendar, Unit> setDate = new F2<EventV2, XMLGregorianCalendar, Unit>() {
        public Unit f(EventV2 eventV2, XMLGregorianCalendar xmlGregorianCalendar) {
            eventV2.setDate(xmlGregorianCalendar);
            return Unit.unit();
        }
    };

    // -----------------------------------------------------------------------
    // JaxbElement creators
    // -----------------------------------------------------------------------

    public static final F<EventV2, JAXBElement<EventV2>> eventJaxbElement = new F<EventV2, JAXBElement<EventV2>>() {
        public JAXBElement<EventV2> f(EventV2 eventV2) {
            return objectFactory.createEvent(eventV2);
        }
    };

    public static final F<EventListV2, JAXBElement<EventListV2>> eventListJaxbElement = new F<EventListV2, JAXBElement<EventListV2>>() {
        public JAXBElement<EventListV2> f(EventListV2 eventListV2) {
            return objectFactory.createEvents(eventListV2);
        }
    };

    public static final F<PersonV2, JAXBElement<PersonV2>> personJaxbElement = new F<PersonV2, JAXBElement<PersonV2>>() {
        public JAXBElement<PersonV2> f(PersonV2 personV2) {
            return objectFactory.createPerson(personV2);
        }
    };

    public static final F<PersonListV2, JAXBElement<PersonListV2>> personListJaxbElement = new F<PersonListV2, JAXBElement<PersonListV2>>() {
        public JAXBElement<PersonListV2> f(PersonListV2 personListV2) {
            return objectFactory.createPeople(personListV2);
        }
    };

    public static final F<RoomV2, JAXBElement<RoomV2>> roomJaxbElement = new F<RoomV2, JAXBElement<RoomV2>>() {
        public JAXBElement<RoomV2> f(RoomV2 eventV2) {
            return objectFactory.createRoom(eventV2);
        }
    };

    public static final F<RoomListV2, JAXBElement<RoomListV2>> roomListJaxbElement = new F<RoomListV2, JAXBElement<RoomListV2>>() {
        public JAXBElement<RoomListV2> f(RoomListV2 roomListV2) {
            return objectFactory.createRooms(roomListV2);
        }
    };

    public static final F<SessionV2, JAXBElement<SessionV2>> sessionJaxbElement = new F<SessionV2, JAXBElement<SessionV2>>() {
        public JAXBElement<SessionV2> f(SessionV2 sessionV2) {
            return objectFactory.createSession(sessionV2);
        }
    };

    public static F<SessionListV2, JAXBElement<SessionListV2>> sessionListJaxbElement = new F<SessionListV2, JAXBElement<SessionListV2>>() {
        public JAXBElement<SessionListV2> f(SessionListV2 sessionListV2) {
            return objectFactory.createSessions(sessionListV2);
        }
    };

    public static class SessionListV2F {
        public static final F<SessionListV2, List<SessionV2>> getSession = new F<SessionListV2, List<SessionV2>>() {
            public List<SessionV2> f(SessionListV2 sessionList) {
                return iterableList(sessionList.getSession());
            }
        };
    }

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
