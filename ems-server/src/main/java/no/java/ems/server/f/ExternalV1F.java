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

package no.java.ems.server.f;

import fj.F;
import fj.F2;
import static fj.Function.curry;
import fj.Unit;
import static fj.Unit.unit;
import fj.data.List;
import fj.data.Option;
import static fj.data.Option.fromNull;
import static no.java.ems.external.v1.EmsV1F.toLocalDate;
import static no.java.ems.external.v1.EmsV1F.toXmlGregorianCalendar;
import no.java.ems.external.v1.*;
import no.java.ems.server.domain.*;
import org.joda.time.LocalDate;
import org.joda.time.Interval;
import org.joda.time.DateTime;
import org.joda.time.Period;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.math.BigInteger;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 *
 * @version $Id$
 */
public class ExternalV1F {

    private static final ObjectFactory objectFactory = new ObjectFactory();


    private static TagsV1 convertTags(AbstractEntity entity) {
        TagsV1 tags = new TagsV1();
        tags.getTag().addAll(entity.getTags());
        return tags;
    }

    // -----------------------------------------------------------------------
    // Session
    // -----------------------------------------------------------------------

    public static final F<Session, SessionV1> sessionV1 = new F<Session, SessionV1>() {
        public SessionV1 f(Session session) {
            SessionV1 sessionV1 = objectFactory.createSessionV1();
            sessionV1.setUuid(session.getId());
            sessionV1.setEventUuid(session.getEventId());
            sessionV1.setTitle(session.getTitle());
            sessionV1.setBody(cleanString("session", session.getId(), session.getBody()));
            sessionV1.setNotes(session.getNotes());
            sessionV1.setEquipment(session.getEquipment());
            sessionV1.setExpectedAudience(session.getExpectedAudience());
            if (session.getLanguage() != null) {
                sessionV1.setLanguage(session.getLanguage().getIsoCode());
            }
            sessionV1.setFeedback(session.getFeedback());            
            sessionV1.setFormat(sessionFormatV1.f(session.getFormat()).orSome((SessionFormat) null));
            sessionV1.setState(sessionStateV1.f(session.getState()).orSome((SessionState) null));
            sessionV1.setLevel(sessionLevelV1.f(session.getLevel()).orSome((SessionLevel) null));
            KeywordsV1 keywords = new KeywordsV1();
            keywords.getKeyword().addAll(session.getKeywords());
            sessionV1.setKeywords(keywords);
            sessionV1.setTags(convertTags(session));
            sessionV1.setTimeslot(session.getTimeslot().map(EmsV1F.toIntervalV1).orSome((IntervalV1) null));
            sessionV1.setSpeakers(new SpeakerListV1());
            sessionV1.getSpeakers().getSpeaker().addAll(List.iterableList(session.getSpeakers()).map(speakerV1).toCollection());
            sessionV1.setAttachments(new BinaryListV1());
            sessionV1.getAttachments().getBinary().addAll(List.iterableList(session.getAttachments()).map(binaryV1).toCollection());
            return sessionV1;
        }
    };

    public static final F<SessionV1, Session> session = new F<SessionV1, Session>() {
        public Session f(SessionV1 session) {
            Session newSession = new Session();
            newSession.setEventId(session.getEventUuid());
            newSession.setTitle(session.getTitle());
            newSession.setNotes(session.getNotes());
            newSession.setBody(session.getBody());
            newSession.setEquipment(session.getEquipment());
            newSession.setExpectedAudience(session.getExpectedAudience());
            newSession.setFeedback(session.getFeedback());
            if (session.getLanguage() != null) {
                newSession.setLanguage(new Language(session.getLanguage()));
            }
            newSession.setFormat(sessionFormat.f(session.getFormat()).orSome(Session.Format.Presentation));
            newSession.setState(sessionState.f(session.getState()).orSome(Session.State.Pending));
            newSession.setLevel(sessionLevel.f(session.getLevel()).orSome(Session.Level.Introductory));
            newSession.setKeywords(session.getKeywords() == null ? Collections.<String>emptyList() : new ArrayList<String>(session.getKeywords().getKeyword()));
            newSession.setTags(session.getTags() == null ? Collections.<String>emptyList() : new ArrayList<String>(session.getTags().getTag()));
            newSession.setTimeslot(fromNull(session.getTimeslot()).map(EmsV1F.toInterval));
            if (session.getSpeakers() != null) {
                List<Speaker> list = List.iterableList(session.getSpeakers().getSpeaker()).map(speaker);
                newSession.setSpeakers(new ArrayList<Speaker>(list.toCollection()));
            }
            return newSession;
        }
    };

    private static F<SpeakerV1, Speaker> speaker = new F<SpeakerV1, Speaker>() {
        public Speaker f(SpeakerV1 speakerV1) {
            Speaker speaker = new Speaker(speakerV1.getPersonUuid(), speakerV1.getName());
            speaker.setDescription(speakerV1.getDescription());            
            return speaker;
        }
    };

    private static F<Speaker, SpeakerV1> speakerV1 = new F<Speaker, SpeakerV1>() {
        public SpeakerV1 f(Speaker speaker) {
            SpeakerV1 speakerV1 = objectFactory.createSpeakerV1();
            speakerV1.setName(speaker.getName());
            speakerV1.setPersonUuid(speaker.getPersonId());
            if (speaker.getPhoto() != null) {
                speakerV1.setPhoto(convertBinary(speaker.getPhoto()));
            }
            speakerV1.setDescription(cleanString("speaker", speaker.getId(), speaker.getDescription()));
            return speakerV1;
        }
    };

    public static final F<Session.Format, Option<SessionFormat>> sessionFormatV1 = new F<Session.Format, Option<SessionFormat>>() {
        public Option<SessionFormat> f(Session.Format format) {
            return fromNull(format).map(new F<Session.Format, SessionFormat>() {
                public SessionFormat f(Session.Format format) {
                    switch (format) {
                        case BoF:
                            return SessionFormat.BO_F;
                        case Course:
                            return SessionFormat.COURSE;
                        case PanelDebate:
                            return SessionFormat.PANEL_DEBATE;
                        case Presentation:
                            return SessionFormat.PRESENTATION;
                        case Quickie:
                            return SessionFormat.QUICKIE;
                        default:
                            throw new RuntimeException("Unknown format: " + format);
                    }
                }
            });
        }
    };

    public static final F<SessionFormat, Option<Session.Format>> sessionFormat = new F<SessionFormat, Option<Session.Format>>() {
        public Option<Session.Format> f(final SessionFormat format) {
            return fromNull(format).map(new F<SessionFormat, Session.Format>() {
                public Session.Format f(SessionFormat format) {
                    switch(format) {
                        case BO_F:
                            return Session.Format.BoF;
                        case COURSE:
                            return Session.Format.Course;
                        case PANEL_DEBATE:
                            return Session.Format.PanelDebate;
                        case PRESENTATION:
                            return Session.Format.Presentation;
                        case QUICKIE:
                            return Session.Format.Quickie;
                        default:
                            throw new RuntimeException("Unknown format: " + format);
                    }
                }
            });
        }
    };


    public static final F<Session.State, Option<SessionState>> sessionStateV1 = new F<Session.State, Option<SessionState>>() {
        public Option<SessionState> f(Session.State state) {
            return fromNull(state).map(new F<Session.State, SessionState>() {
                public SessionState f(Session.State state) {
                    switch (state) {
                        case Approved:
                            return SessionState.APPROVED;
                        case Pending:
                            return SessionState.PENDING;
                        case Rejected:
                            return SessionState.REJECTED;
                        default:
                            throw new RuntimeException("Unknown state: " + state);
                    }
                }
            });
        }
    };
    public static final F<SessionState, Option<Session.State>> sessionState = new F<SessionState, Option<Session.State>>() {
        public Option<Session.State> f(SessionState state) {
            return fromNull(state).map(new F<SessionState, Session.State>() {
                public Session.State f(SessionState state) {
                    switch (state) {
                        case APPROVED:
                            return Session.State.Approved;
                        case PENDING:
                            return Session.State.Pending;
                        case REJECTED:
                            return Session.State.Rejected;
                        default:
                            throw new RuntimeException("Unknown state: " + state);
                    }
                }
            });
        }
    };public static final F<Session.Level, Option<SessionLevel>> sessionLevelV1 = new F<Session.Level, Option<SessionLevel>>() {
        public Option<SessionLevel> f(Session.Level level) {
            return fromNull(level).map(new F<Session.Level, SessionLevel>() {
                public SessionLevel f(Session.Level level) {
                    switch (level) {
                        case Introductory:
                            return SessionLevel.INTRODUCTORY;
                        case Introductory_Intermediate:
                            return SessionLevel.INTRODUCTORY_INTERMEDIATE;
                        case Intermediate:
                            return SessionLevel.INTERMEDIATE;
                        case Intermediate_Advanced:
                            return SessionLevel.INTERMEDIATE_ADVANCED;
                        case Advanced:
                            return SessionLevel.ADVANCED;
                        default:
                            throw new RuntimeException("Unknown level: " + level);
                    }
                }
            });
        }
    };

    public static final F<SessionLevel, Option<Session.Level>> sessionLevel = new F<SessionLevel, Option<Session.Level>>() {
        public Option<Session.Level> f(SessionLevel level) {
            return fromNull(level).map(new F<SessionLevel, Session.Level>() {
                public Session.Level f(SessionLevel level) {
                    switch (level) {
                        case INTRODUCTORY:
                            return Session.Level.Introductory;
                        case INTRODUCTORY_INTERMEDIATE:
                            return Session.Level.Introductory_Intermediate;
                        case INTERMEDIATE:
                            return Session.Level.Intermediate;
                        case INTERMEDIATE_ADVANCED:
                            return Session.Level.Intermediate_Advanced;
                        case ADVANCED:
                            return Session.Level.Advanced;
                        default:
                            throw new RuntimeException("Unknown level: " + level);
                    }
                }
            });
        }
    };
    

    // -----------------------------------------------------------------------
    // Person
    // -----------------------------------------------------------------------

    public static F<Person, PersonV1> personV1 = new F<Person, PersonV1>() {
        public PersonV1 f(Person person) {
            PersonV1 personV1 = objectFactory.createPersonV1();
            personV1.setUuid(person.getId());
            personV1.setName(person.getName());
            if (person.getLanguage() != null) {
                personV1.setLanguage(person.getLanguage().getIsoCode());
            }
            if (person.getNationality() != null) {
                personV1.setNationality(person.getNationality().getIsoCode());
            }                        
            personV1.setDescription(person.getDescription());
            personV1.setTags(convertTags(person));
            EmailAddressListV1 emails = new EmailAddressListV1();
            for (EmailAddress address : person.getEmailAddresses()) {
                emails.getEmailAddress().add(address.getEmailAddress());
            }
            personV1.setEmailAddresses(emails);
            Binary photo = person.getPhoto();
            if (photo != null) {
                personV1.setPhoto(convertBinary(photo));
            }
            return personV1;
        }
    };

    private static URIBinaryV1 convertBinary(Binary photo) {
        URIBinaryV1 uriBinary = objectFactory.createURIBinaryV1();
        uriBinary.setFilename(photo.getFileName());
        uriBinary.setMimeType(photo.getMimeType());
        uriBinary.setSize(BigInteger.valueOf(photo.getSize()));
        uriBinary.setUri(photo.getId());
        return uriBinary;
    }

/*
    public static F<URIBinaryV1, Binary> binary = new F<URIBinaryV1, Binary>() {
        public Binary f(URIBinaryV1 binaryV1) {
            UriBinary binary = new UriBinary(); 
            uriBinary.setFilename(binaryV1.getFileName());
            uriBinary.setMimeType(binaryV1.getMimeType());
            uriBinary.setSize(BigInteger.valueOf(binaryV1.getSize()));
            uriBinary.setUri(binaryV1.getId());
            return null;
        }
    };
*/

    public static F<Binary, URIBinaryV1> binaryV1 = new F<Binary, URIBinaryV1>() {
        public URIBinaryV1 f(Binary binary) {
            URIBinaryV1 uriBinary = objectFactory.createURIBinaryV1();
            uriBinary.setFilename(binary.getFileName());
            uriBinary.setMimeType(binary.getMimeType());
            uriBinary.setSize(BigInteger.valueOf(binary.getSize()));
            uriBinary.setUri(binary.getId());
            return uriBinary;
        }
    };

    public static F<PersonV1, Person> person = new F<PersonV1, Person>() {
        public Person f(PersonV1 personV1) {
            Person person = new Person();
            person.setId(personV1.getUuid());
            person.setName(personV1.getName());
            if (personV1.getLanguage() != null) {
                person.setLanguage(new Language(personV1.getLanguage()));
            }
            if (personV1.getNationality() != null) {
                person.setNationality(new Nationality(personV1.getNationality()));
            }
            person.setDescription(personV1.getDescription());
            //person.setNotes(personV1.getNotes());
            person.setTags(new ArrayList<String>(personV1.getTags().getTag()));
            EmailAddressListV1 addresses = personV1.getEmailAddresses();
            if (addresses != null) {
                Collection<EmailAddress> addressCollection = List.iterableList(addresses.getEmailAddress()).map(new F<String, EmailAddress>() {
                    public EmailAddress f(String address) {
                        return new EmailAddress(address);
                    }
                }).toCollection();
                person.setEmailAddresses(addressCollection);
            }
            return person;
        }
    };

    // -----------------------------------------------------------------------
    // Event
    // -----------------------------------------------------------------------

    public static final F<Event, EventV1> eventV1 = new F<Event, EventV1>() {
        public EventV1 f(Event event) {
            EventV1 e = objectFactory.createEventV1();
            e.setUuid(event.getId());
            e.setName(event.getName());
            e.setDate(fromNull(event.getDate()).map(toXmlGregorianCalendar).orSome((XMLGregorianCalendar) null));
            e.setTimeslots(objectFactory.createTimeslotListV1());
            List.<Interval>iterableList(event.getTimeslots()).
                    map(EmsV1F.toIntervalV1).
                    map(curry(ExternalV1F.<IntervalV1>add(), e.getTimeslots().getTimeslot()));
            RoomListV1 roomList = objectFactory.createRoomListV1();
            List.<Room>iterableList(event.getRooms()).
                    map(roomV1).
                    foreach(curry(ExternalV1F.<RoomV1>add(), roomList.getRoom()));
            e.setRooms(roomList);
            e.setTags(convertTags(event));
            return e;
        }
    };

    public static final F<EventV1, Event> event = new F<EventV1, Event>() {
        public Event f(EventV1 event) {
            Event e = new Event(event.getName());            
            e.setDate(fromNull(event.getDate()).map(toLocalDate).orSome((LocalDate) null));
            e.setTags(new ArrayList<String>(event.getTags().getTag()));
            List<Interval> intervalList = List.<IntervalV1>iterableList(event.getTimeslots().getTimeslot()).map(EmsV1F.toInterval);
            e.setTimeslots(new ArrayList<Interval>(intervalList.toCollection()));
            RoomListV1 listV1 = event.getRooms();
            e.setRooms(new ArrayList<Room>(List.<RoomV1>iterableList(listV1.getRoom()).map(room).toCollection()));
            return e;
        }
    };    

    public static final F<Room, RoomV1> roomV1 = new F<Room, RoomV1>() {
        public RoomV1 f(Room room) {
            System.out.println("ExternalV1F.roomV1");
            RoomV1 roomV1 = new RoomV1();
            roomV1.setUuid(room.getId());            
            roomV1.setName(room.getName());
            roomV1.setDescription(room.getDescription());
            return roomV1;
        }
    };

    public static final F<RoomV1, Room> room = new F<RoomV1, Room>() {
        public Room f(RoomV1 externalRoom) {
            Room room = new Room();
            room.setName(externalRoom.getName());
            room.setDescription(externalRoom.getDescription());
            return room;
        }
    };

    // -----------------------------------------------------------------------
    // EventList
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // Joda Time
    // -----------------------------------------------------------------------

    public static final F<Interval, DateTime> intervalGetStart = new F<Interval, DateTime>() {
        public DateTime f(Interval interval) {
            return interval.getStart();
        }
    };

    public static final F<Period, Integer> periodGetMinutes = new F<Period, Integer>() {
        public Integer f(Period period) {
            return period.getMinutes();
        }
    };

    public static final F<Interval, Period> intervalToPeriod = new F<Interval, Period>() {
        public Period f(Interval interval) {
            return interval.toPeriod();
        }
    };

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private static <A> F2<java.util.List<A>, A, Unit> add() {
        return new F2<java.util.List<A>, A, Unit>() {
            public Unit f(java.util.List<A> list, A a) {
                System.out.println("ExternalV1F.add");
                list.add(a);
                return unit();
            }
        };
    }

    /**
     * There are a few sessions and speaker descriptions with invalid bytes, clean those out.
     */
    private static String cleanString(String type, String id, String value) {
        if(value == null) {
            return null;
        }

        char[] chars = new char[value.length()];
        char[] newChars = new char[value.length()];
        value.getChars(0, value.length(), chars, 0);

        int j = 0;
        for (char c : chars) {
            if (c == '\u0001') {
                System.out.println(type + " contains bad data (0x01): " + id);
            } else if (c == 0x1b) {
                System.out.println(type + " contains bad data (0x1b): " + id);
            } else if (c == 0x0b) {
                System.out.println(type + " contains bad data (0x0b): " + id);
            } else {
                newChars[j++] = c;
            }
        }

        value = new String(newChars, 0, j);
        return value;
    }
}
