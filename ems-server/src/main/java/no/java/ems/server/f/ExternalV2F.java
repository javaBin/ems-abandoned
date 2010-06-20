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
import static no.java.ems.external.v2.EmsV2F.toLocalDate;
import static no.java.ems.external.v2.EmsV2F.toXmlGregorianCalendar;
import no.java.ems.external.v2.*;
import no.java.ems.server.domain.*;
import org.joda.time.LocalDate;
import org.joda.time.Interval;
import org.joda.time.DateTime;
import org.joda.time.Period;

import javax.xml.datatype.XMLGregorianCalendar;
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
public class ExternalV2F {

    private static final ObjectFactory objectFactory = new ObjectFactory();


    private static TagsV2 convertTags(AbstractEntity entity) {
        TagsV2 tags = new TagsV2();
        tags.getTag().addAll(entity.getTags());
        return tags;
    }

    // -----------------------------------------------------------------------
    // Session
    // -----------------------------------------------------------------------

    public static final F<Session, SessionV2> sessionV2 = new F<Session, SessionV2>() {
        public SessionV2 f(Session session) {
            SessionV2 sessionV2 = objectFactory.createSessionV2();
            sessionV2.setUuid(session.getId());
            sessionV2.setEventUuid(session.getEventId());
            sessionV2.setTitle(session.getTitle());
            sessionV2.setBody(cleanString("session", session.getId(), session.getBody()));
            sessionV2.setNotes(session.getNotes());
            sessionV2.setLead(session.getLead());
            sessionV2.setOutline(session.getOutline());
            sessionV2.setEquipment(session.getEquipment());
            sessionV2.setExpectedAudience(session.getExpectedAudience());
            if (session.getLanguage() != null) {
                sessionV2.setLanguage(session.getLanguage().getIsoCode());
            }
            sessionV2.setFeedback(session.getFeedback());            
            sessionV2.setFormat(sessionFormatV2.f(session.getFormat()).orSome((SessionFormat) null));
            sessionV2.setState(sessionStateV2.f(session.getState()).orSome((SessionState) null));
            sessionV2.setLevel(sessionLevelV2.f(session.getLevel()).orSome((SessionLevel) null));
            KeywordsV2 keywords = new KeywordsV2();
            keywords.getKeyword().addAll(session.getKeywords());
            sessionV2.setKeywords(keywords);
            sessionV2.setTags(convertTags(session));
            sessionV2.setPublished(session.isPublished());
            sessionV2.setTimeslot(session.getTimeslot().map(EmsV2F.toIntervalV2).orSome((IntervalV2) null));
            sessionV2.setSpeakers(new SpeakerListV2());
            sessionV2.getSpeakers().getSpeaker().addAll(List.iterableList(session.getSpeakers()).map(speakerV2).toCollection());
            sessionV2.setAttachments(new BinaryListV2());
            sessionV2.getAttachments().getBinary().addAll(List.iterableList(session.getAttachments()).map(binaryV2).toCollection());
            return sessionV2;
        }
    };

    public static final F<SessionV2, Session> session = new F<SessionV2, Session>() {
        public Session f(SessionV2 session) {
            Session newSession = new Session();
            newSession.setTitle(session.getTitle());
            newSession.setNotes(session.getNotes());
            newSession.setBody(session.getBody());
            newSession.setLead(session.getLead());
            newSession.setOutline(session.getOutline());
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
            newSession.setTimeslot(fromNull(session.getTimeslot()).map(EmsV2F.toInterval));
            if (session.getSpeakers() != null) {
                List<Speaker> list = List.iterableList(session.getSpeakers().getSpeaker()).map(speaker);
                newSession.setSpeakers(new ArrayList<Speaker>(list.toCollection()));
            }
            newSession.setPublished(session.isPublished());
            return newSession;
        }
    };

    public static final F2<String, Session, Session> eventId = new F2<String, Session, Session>() {
        public Session f(String eventId, Session session) {
            session.setEventId(eventId);
            return session;
        }
    };

    private static F<SpeakerV2, Speaker> speaker = new F<SpeakerV2, Speaker>() {
        public Speaker f(SpeakerV2 speakerV2) {
            Speaker speaker = new Speaker(speakerV2.getPersonUuid(), speakerV2.getName());
            speaker.setDescription(speakerV2.getDescription());            
            return speaker;
        }
    };

    private static F<Speaker, SpeakerV2> speakerV2 = new F<Speaker, SpeakerV2>() {
        public SpeakerV2 f(Speaker speaker) {
            SpeakerV2 speakerV2 = objectFactory.createSpeakerV2();
            speakerV2.setName(speaker.getName());
            speakerV2.setPersonUuid(speaker.getPersonId());
            if (speaker.getPhoto() != null) {
                speakerV2.setPhoto(convertBinary(speaker.getPhoto()));
            }
            speakerV2.setDescription(cleanString("speaker", speaker.getId(), speaker.getDescription()));
            return speakerV2;
        }
    };

    public static final F<Session.Format, Option<SessionFormat>> sessionFormatV2 = new F<Session.Format, Option<SessionFormat>>() {
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


    public static final F<Session.State, Option<SessionState>> sessionStateV2 = new F<Session.State, Option<SessionState>>() {
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
    };public static final F<Session.Level, Option<SessionLevel>> sessionLevelV2 = new F<Session.Level, Option<SessionLevel>>() {
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

    public static F<Person, PersonV2> personV2 = new F<Person, PersonV2>() {
        public PersonV2 f(Person person) {
            PersonV2 personV2 = objectFactory.createPersonV2();
            personV2.setUuid(person.getId());
            personV2.setName(person.getName());
            if (person.getLanguage() != null) {
                personV2.setLanguage(person.getLanguage().getIsoCode());
            }
            if (person.getNationality() != null) {
                personV2.setNationality(person.getNationality().getIsoCode());
            }                        
            personV2.setDescription(person.getDescription());
            personV2.setTags(convertTags(person));
            EmailAddressListV2 emails = new EmailAddressListV2();
            for (EmailAddress address : person.getEmailAddresses()) {
                emails.getEmailAddress().add(address.getEmailAddress());
            }
            personV2.setEmailAddresses(emails);
            Binary photo = person.getPhoto();
            if (photo != null) {
                personV2.setPhoto(convertBinary(photo));
            }
            return personV2;
        }
    };

    private static URIBinaryV2 convertBinary(Binary photo) {
        URIBinaryV2 uriBinary = objectFactory.createURIBinaryV2();
        uriBinary.setFilename(photo.getFileName());
        uriBinary.setMimeType(photo.getMimeType());
        uriBinary.setSize(BigInteger.valueOf(photo.getSize()));
        uriBinary.setUri(photo.getId());
        return uriBinary;
    }

/*
    public static F<URIBinaryV2, Binary> binary = new F<URIBinaryV2, Binary>() {
        public Binary f(URIBinaryV2 binaryV2) {
            UriBinary binary = new UriBinary(); 
            uriBinary.setFilename(binaryV2.getFileName());
            uriBinary.setMimeType(binaryV2.getMimeType());
            uriBinary.setSize(BigInteger.valueOf(binaryV2.getSize()));
            uriBinary.setUri(binaryV2.getId());
            return null;
        }
    };
*/

    public static F<Binary, URIBinaryV2> binaryV2 = new F<Binary, URIBinaryV2>() {
        public URIBinaryV2 f(Binary binary) {
            URIBinaryV2 uriBinary = objectFactory.createURIBinaryV2();
            uriBinary.setFilename(binary.getFileName());
            uriBinary.setMimeType(binary.getMimeType());
            uriBinary.setSize(BigInteger.valueOf(binary.getSize()));
            uriBinary.setUri(binary.getId());
            return uriBinary;
        }
    };

    public static F<PersonV2, Person> person = new F<PersonV2, Person>() {
        public Person f(PersonV2 personV2) {
            Person person = new Person();
            person.setId(personV2.getUuid());
            person.setName(personV2.getName());
            if (personV2.getLanguage() != null) {
                person.setLanguage(new Language(personV2.getLanguage()));
            }
            if (personV2.getNationality() != null) {
                person.setNationality(new Nationality(personV2.getNationality()));
            }
            person.setDescription(personV2.getDescription());
            //person.setNotes(personV2.getNotes());
            person.setTags(new ArrayList<String>(personV2.getTags().getTag()));
            EmailAddressListV2 addresses = personV2.getEmailAddresses();
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

    public static final F<Event, EventV2> eventV2 = new F<Event, EventV2>() {
        public EventV2 f(Event event) {
            EventV2 e = objectFactory.createEventV2();
            e.setUuid(event.getId());
            e.setName(event.getName());
            e.setDate(fromNull(event.getDate()).map(toXmlGregorianCalendar).orSome((XMLGregorianCalendar) null));
            e.setTimeslots(objectFactory.createTimeslotListV2());
            List.<Interval>iterableList(event.getTimeslots()).
                    map(EmsV2F.toIntervalV2).
                    map(curry(ExternalV2F.<IntervalV2>add(), e.getTimeslots().getTimeslot()));
            RoomListV2 roomList = objectFactory.createRoomListV2();
            List.<Room>iterableList(event.getRooms()).
                    map(roomV2).
                    foreach(curry(ExternalV2F.<RoomV2>add(), roomList.getRoom()));
            e.setRooms(roomList);
            e.setTags(convertTags(event));
            return e;
        }
    };

    public static final F<EventV2, Event> event = new F<EventV2, Event>() {
        public Event f(EventV2 event) {
            Event e = new Event(event.getName());            
            e.setDate(fromNull(event.getDate()).map(toLocalDate).orSome((LocalDate) null));
            e.setTags(new ArrayList<String>(event.getTags().getTag()));
            if (event.getTimeslots() != null) {
                List<Interval> intervalList = List.<IntervalV2>iterableList(event.getTimeslots().getTimeslot()).map(EmsV2F.toInterval);
                e.setTimeslots(new ArrayList<Interval>(intervalList.toCollection()));
            }
            RoomListV2 listV2 = event.getRooms();
            e.setRooms(new ArrayList<Room>(List.<RoomV2>iterableList(listV2.getRoom()).map(room).toCollection()));
            return e;
        }
    };    

    public static final F<Room, RoomV2> roomV2 = new F<Room, RoomV2>() {
        public RoomV2 f(Room room) {
            RoomV2 roomV2 = new RoomV2();
            roomV2.setUuid(room.getId());            
            roomV2.setName(room.getName());
            roomV2.setDescription(room.getDescription());
            return roomV2;
        }
    };

    public static final F<RoomV2, Room> room = new F<RoomV2, Room>() {
        public Room f(RoomV2 externalRoom) {
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
