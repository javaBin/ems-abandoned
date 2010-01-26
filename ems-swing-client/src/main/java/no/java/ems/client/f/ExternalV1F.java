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

package no.java.ems.client.f;

import fj.F;
import fj.F2;
import static fj.Function.curry;
import fj.Unit;
import static fj.Unit.unit;
import fj.data.Java;
import fj.data.List;
import fj.data.Option;
import static fj.data.Option.fromNull;
import static fj.data.Option.none;
import static fj.data.Option.some;

import no.java.ems.client.ResourceHandle;
import no.java.ems.domain.*;
import static no.java.ems.external.v1.EmsV1F.toLocalDate;
import static no.java.ems.external.v1.EmsV1F.toXmlGregorianCalendar;
import no.java.ems.external.v1.*;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Collection;
import java.net.URI;
import java.math.BigInteger;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
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
            sessionV1.setUuid(session.getDisplayID());
            if (session.getHandle() != null) {
                sessionV1.setUri(session.getHandle().toString());
            }
            sessionV1.setEventUuid(session.getEventHandle().toString());
            sessionV1.setTitle(session.getTitle());
            sessionV1.setBody(session.getBody());
            sessionV1.setNotes(session.getNotes());
            sessionV1.setEquipment(session.getEquipment());
            if (session.getLanguage() != null) {
                sessionV1.setLanguage(session.getLanguage().getIsoCode());
            }
            sessionV1.setExpectedAudience(session.getExpectedAudience());
            sessionV1.setFeedback(session.getFeedback());
            sessionV1.setState(sessionStateV1.f(session.getState()).some());
            sessionV1.setFormat(sessionFormatV1.f(session.getFormat()).orSome((SessionFormat) null));
            sessionV1.setLevel(sessionLevelV1.f(session.getLevel()).orSome((SessionLevel) null));
            KeywordsV1 keywords = new KeywordsV1();
            keywords.getKeyword().addAll(session.getKeywords());
            sessionV1.setKeywords(keywords);
            sessionV1.setTags(convertTags(session));
            sessionV1.setTimeslot(fromNull(session.getTimeslot()).map(EmsV1F.toIntervalV1).orSome((IntervalV1) null));
            List<SpeakerV1> list = List.iterableList(session.getSpeakers()).map(speaker);
            sessionV1.setSpeakers(objectFactory.createSpeakerListV1());
            sessionV1.getSpeakers().getSpeaker().addAll(list.toCollection());
            return sessionV1;
        }
    };

    public static final F<SessionV1, Session> session = new F<SessionV1, Session>() {
        public Session f(SessionV1 session) {
            Session newSession = new Session();
            newSession.setHandle(new ResourceHandle(URI.create(session.getUri())));
            newSession.setDisplayID(session.getUuid());
            newSession.setEventHandle(new ResourceHandle(URI.create(session.getEventUuid())));
            newSession.setTitle(session.getTitle());
            newSession.setBody(session.getBody());
            newSession.setNotes(session.getNotes());
            newSession.setEquipment(session.getEquipment());
            newSession.setExpectedAudience(session.getExpectedAudience());
            newSession.setFeedback(session.getFeedback());
            if (session.getLanguage() != null) {
                newSession.setLanguage(new Language(session.getLanguage()));
            }
            newSession.setState(sessionState.f(session.getState()).orSome((Session.State) null));
            newSession.setFormat(sessionFormat.f(session.getFormat()).orSome((Session.Format) null));
            newSession.setLevel(sessionLevel.f(session.getLevel()).orSome((Session.Level) null));
            newSession.setKeywords(new ArrayList<String>(session.getKeywords().getKeyword()));
            newSession.setTags(new ArrayList<String>(session.getTags().getTag()));
            newSession.setTimeslot(fromNull(session.getTimeslot()).map(EmsV1F.toInterval).orSome((Interval) null));
            if (session.getSpeakers() != null) {
                List<Speaker> list = List.iterableList(session.getSpeakers().getSpeaker()).map(speakerV1);
                newSession.setSpeakers(new ArrayList<Speaker>(list.toCollection()));
            }
            if (session.getAttachments() != null) {
                List<Binary> list = List.iterableList(session.getAttachments().getBinary()).map(uriBinary);
                newSession.setAttachments(new ArrayList<Binary>(list.toCollection()));
            }
            newSession.setModified(false);
            return newSession;
        }
    };

    private static F<SpeakerV1, Speaker> speakerV1 = new F<SpeakerV1, Speaker>() {
        public Speaker f(SpeakerV1 speakerV1) {
            Speaker speaker = new Speaker(URI.create(speakerV1.getPersonUri()), speakerV1.getName());
            speaker.setDescription(speakerV1.getDescription());
            URIBinaryV1 photo = speakerV1.getPhoto();
            speaker.setPhoto(fromNull(photo).map(uriBinary).orSome((Binary)null));            
            speaker.setModified(false);
            return speaker;
        }
    };

    private static F<Speaker, SpeakerV1> speaker = new F<Speaker, SpeakerV1>() {
        public SpeakerV1 f(Speaker speaker) {
            SpeakerV1 speakerV1 = objectFactory.createSpeakerV1();
            speakerV1.setPersonUri(speaker.getPersonURI().toString());
            Option<URIBinaryV1> photo = uriBinaryV1.f(speaker.getPhoto());
            if (photo.isSome()) {
                speakerV1.setPhoto(photo.some());
            }
            speakerV1.setDescription(speaker.getDescription());
            speakerV1.setName(speaker.getName());
            return speakerV1;
        }
    };
    
    public static final F<Binary, Option<URIBinaryV1>> uriBinaryV1 = new F<Binary, Option<URIBinaryV1>>() {
        public Option<URIBinaryV1> f(Binary binary) {
            URIBinaryV1 photo = new URIBinaryV1();
            if (binary instanceof URIBinary) {
                URIBinary uriBinary = (URIBinary) binary;
                photo.setUri(uriBinary.getURI().toString());
                photo.setFilename(uriBinary.getFileName());
                photo.setMimeType(uriBinary.getMimeType());
                photo.setSize(BigInteger.valueOf(uriBinary.getSize()));
                return some(photo);
            }
            return none();
        }
    };

    public static final F<URIBinaryV1, Binary> uriBinary = new F<URIBinaryV1, Binary>() {
        public Binary f(URIBinaryV1 binaryV1) {
            return new URIBinary(
                    binaryV1.getFilename(),
                    binaryV1.getMimeType(),
                    binaryV1.getSize().longValue(),
                    URI.create(binaryV1.getUri())
            );
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
        public Option<Session.Format> f(SessionFormat format) {
            return fromNull(format).map(new F<SessionFormat, Session.Format>() {
                public Session.Format f(SessionFormat format) {
                    switch (format) {
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
    };

    public static final F<Session.Level, Option<SessionLevel>> sessionLevelV1 = new F<Session.Level, Option<SessionLevel>>() {
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
            personV1.setName(person.getName());
            personV1.setUuid(person.getDisplayID());
            if (person.getHandle() != null) {
                personV1.setUri(person.getHandle().toString());
            }
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
            Option<URIBinaryV1> photo = uriBinaryV1.f(person.getPhoto());
            if (photo.isSome()) {
                personV1.setPhoto(photo.some());
            }

            return personV1;
        }
    };

    public static F<PersonV1, Person> person = new F<PersonV1, Person>() {
        public Person f(PersonV1 personV1) {
            Person person = new Person();
            person.setHandle(new ResourceHandle(URI.create(personV1.getUri())));
            person.setDisplayID(personV1.getUuid());
            person.setName(personV1.getName());
            if (personV1.getLanguage() != null) {
                person.setLanguage(new Language(personV1.getLanguage()));
            }
            if (personV1.getNationality() != null) {
                person.setNationality(new Nationality(personV1.getNationality()));
            }
            person.setDescription(personV1.getDescription());
            person.setTags(new ArrayList<String>(personV1.getTags().getTag()));
            EmailAddressListV1 addresses = personV1.getEmailAddresses();
            if (addresses != null) {
                Collection<EmailAddress> addressCollection = List.iterableList(addresses.getEmailAddress()).map(new F<String, EmailAddress>() {
                    public EmailAddress f(String address) {
                        return new EmailAddress(address);
                    }
                }).toCollection();
                if (addressCollection != null) {
                    person.setEmailAddresses(new ArrayList<EmailAddress>(addressCollection));
                }
            }
            person.setPhoto(fromNull(personV1.getPhoto()).map(uriBinary).orSome((Binary)null));
            person.setModified(false);
            return person;
        }
    };


    // -----------------------------------------------------------------------
    // Event
    // -----------------------------------------------------------------------

    public static final F<Event, EventV1> eventV1 = new F<Event, EventV1>() {
        public EventV1 f(Event event) {
            EventV1 e = objectFactory.createEventV1();
            e.setName(event.getName());
            e.setUuid(e.getUuid());
            if (event.getHandle() != null) {
                e.setUri(event.getHandle().toString());
            }
            e.setDate(fromNull(event.getDate()).map(toXmlGregorianCalendar).orSome((XMLGregorianCalendar) null));
            Java.<Room>ArrayList_List().f(new ArrayList<Room>(event.getRooms())).
                    map(roomV1).
                    foreach(curry(ExternalV1F.<RoomV1>add(), e.getRooms().getRoom()));
            e.setTags(convertTags(event));
            return e;
        }
    };

    public static final F<EventV1, Event> event = new F<EventV1, Event>() {
        public Event f(EventV1 event) {
            Event e = new Event(event.getName());
            e.setHandle(new ResourceHandle(URI.create(event.getUri())));
            e.setDisplayID(event.getUuid());
            //TODO: this should not be set here... We need to get the links from the response.
            e.setSessionURI(e.getHandle().getURI());
            e.setDate(fromNull(event.getDate()).map(toLocalDate).orSome((LocalDate) null));
            e.setTags(new ArrayList<String>(event.getTags().getTag()));
            e.setModified(false);
            return e;
        }
    };


    public static final F<Room, RoomV1> roomV1 = new F<Room, RoomV1>() {
        public RoomV1 f(Room room) {
            RoomV1 roomV1 = new RoomV1();
            if (room.getHandle() != null) {
                roomV1.setUri(room.getHandle().toString());
            }
            roomV1.setName(room.getName());
            roomV1.setDescription(room.getDescription());
            return roomV1;
        }
    };

    public static final F<RoomV1, Room> room = new F<RoomV1, Room>() {
        public Room f(RoomV1 externalRoom) {
            Room room = new Room();
            room.setHandle(new ResourceHandle(URI.create(externalRoom.getUri())));
            room.setName(externalRoom.getName());
            room.setDescription(externalRoom.getDescription());
            room.setModified(false);
            return room;
        }
    };

    // -----------------------------------------------------------------------
    // EventList
    // -----------------------------------------------------------------------

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
}
