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

import fj.*;
import static fj.Function.curry;
import static fj.Unit.unit;
import fj.data.Java;
import fj.data.List;
import fj.data.Option;
import static fj.data.Option.fromNull;
import static fj.data.Option.none;
import static fj.data.Option.some;

import no.java.ems.client.ResourceHandle;
import no.java.ems.domain.*;
import static no.java.ems.external.v2.EmsV2F.toLocalDate;
import static no.java.ems.external.v2.EmsV2F.toXmlGregorianCalendar;
import no.java.ems.external.v2.*;
import org.codehaus.httpcache4j.*;
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
            sessionV2.setUuid(session.getDisplayID());
            if (session.getHandle() != null) {
                sessionV2.setUri(session.getHandle().toString());
            }
            sessionV2.setEventUuid(session.getEventHandle().toString());
            sessionV2.setEventUri(session.getEventHandle().toString());
            sessionV2.setTitle(session.getTitle());
            sessionV2.setBody(session.getBody());
            sessionV2.setLead(session.getLead());
            sessionV2.setOutline(session.getOutline());
            sessionV2.setNotes(session.getNotes());
            sessionV2.setEquipment(session.getEquipment());
            if (session.getLanguage() != null) {
                sessionV2.setLanguage(session.getLanguage().getIsoCode());
            }
            sessionV2.setExpectedAudience(session.getExpectedAudience());
            sessionV2.setFeedback(session.getFeedback());
            sessionV2.setState(sessionStateV2.f(session.getState()).some());
            sessionV2.setFormat(sessionFormatV2.f(session.getFormat()).orSome((SessionFormat) null));
            sessionV2.setLevel(sessionLevelV2.f(session.getLevel()).orSome((SessionLevel) null));
            KeywordsV2 keywords = new KeywordsV2();
            keywords.getKeyword().addAll(session.getKeywords());
            sessionV2.setKeywords(keywords);
            sessionV2.setTags(convertTags(session));
            sessionV2.setTimeslot(fromNull(session.getTimeslot()).map(EmsV2F.toIntervalV2).orSome((IntervalV2) null));
            List<SpeakerV2> list = List.iterableList(session.getSpeakers()).map(speaker);
            sessionV2.setSpeakers(objectFactory.createSpeakerListV2());
            sessionV2.getSpeakers().getSpeaker().addAll(list.toCollection());
            sessionV2.setPublished(session.isPublished());
            return sessionV2;
        }
    };

    public static final F<SessionV2, Session> session = new F<SessionV2, Session>() {
        public Session f(SessionV2 session) {
            Session newSession = new Session();
            newSession.setHandle(new ResourceHandle(URI.create(session.getUri())));
            newSession.setDisplayID(session.getUuid());
            newSession.setEventHandle(new ResourceHandle(URI.create(session.getEventUri())));
            newSession.setTitle(session.getTitle());
            newSession.setBody(session.getBody());
            newSession.setNotes(session.getNotes());
            newSession.setLead(session.getLead());
            newSession.setOutline(session.getOutline());
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
            newSession.setTimeslot(fromNull(session.getTimeslot()).map(EmsV2F.toInterval).orSome((Interval) null));
            if (session.getSpeakers() != null) {
                List<Speaker> list = List.iterableList(session.getSpeakers().getSpeaker()).map(speakerV2);
                newSession.setSpeakers(new ArrayList<Speaker>(list.toCollection()));
            }
            if (session.getAttachments() != null) {
                List<Binary> list = List.iterableList(session.getAttachments().getBinary()).map(uriBinary);
                newSession.setAttachments(new ArrayList<Binary>(list.toCollection()));
            }
            newSession.setPublished(session.isPublished());
            newSession.setModified(false);
            return newSession;
        }
    };

    private static F<SpeakerV2, Speaker> speakerV2 = new F<SpeakerV2, Speaker>() {
        public Speaker f(SpeakerV2 speakerV2) {
            Speaker speaker = new Speaker(URI.create(speakerV2.getPersonUri()), speakerV2.getName());
            speaker.setDescription(speakerV2.getDescription());
            URIBinaryV2 photo = speakerV2.getPhoto();
            speaker.setPhoto(fromNull(photo).map(uriBinary).orSome((Binary)null));            
            return speaker;
        }
    };

    private static F<Speaker, SpeakerV2> speaker = new F<Speaker, SpeakerV2>() {
        public SpeakerV2 f(Speaker speaker) {
            SpeakerV2 speakerV2 = objectFactory.createSpeakerV2();
            speakerV2.setPersonUri(speaker.getPersonURI().toString());
            Option<URIBinaryV2> photo = uriBinaryV2.f(speaker.getPhoto());
            if (photo.isSome()) {
                speakerV2.setPhoto(photo.some());
            }
            speakerV2.setDescription(speaker.getDescription());
            speakerV2.setName(speaker.getName());
            return speakerV2;
        }
    };
    
    public static final F<Binary, Option<URIBinaryV2>> uriBinaryV2 = new F<Binary, Option<URIBinaryV2>>() {
        public Option<URIBinaryV2> f(Binary binary) {
            URIBinaryV2 photo = new URIBinaryV2();
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

    public static final F<URIBinaryV2, Binary> uriBinary = new F<URIBinaryV2, Binary>() {
        public Binary f(URIBinaryV2 binaryV2) {
            return new URIBinary(
                    binaryV2.getFilename(),
                    binaryV2.getMimeType(),
                    binaryV2.getSize().longValue(),
                    URI.create(binaryV2.getUri())
            );
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
    };

    public static final F<Session.Level, Option<SessionLevel>> sessionLevelV2 = new F<Session.Level, Option<SessionLevel>>() {
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
            personV2.setName(person.getName());
            personV2.setUuid(person.getDisplayID());
            if (person.getHandle() != null) {
                personV2.setUri(person.getHandle().toString());
            }
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
            Option<URIBinaryV2> photo = uriBinaryV2.f(person.getPhoto());
            if (photo.isSome()) {
                personV2.setPhoto(photo.some());
            }

            return personV2;
        }
    };

    public static F<PersonV2, Person> person = new F<PersonV2, Person>() {
        public Person f(PersonV2 personV2) {
            Person person = new Person();
            person.setHandle(new ResourceHandle(URI.create(personV2.getUri())));
            person.setDisplayID(personV2.getUuid());
            person.setName(personV2.getName());
            if (personV2.getLanguage() != null) {
                person.setLanguage(new Language(personV2.getLanguage()));
            }
            if (personV2.getNationality() != null) {
                person.setNationality(new Nationality(personV2.getNationality()));
            }
            person.setDescription(personV2.getDescription());
            person.setTags(new ArrayList<String>(personV2.getTags().getTag()));
            EmailAddressListV2 addresses = personV2.getEmailAddresses();
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
            person.setPhoto(fromNull(personV2.getPhoto()).map(uriBinary).orSome((Binary)null));
            person.setModified(false);
            return person;
        }
    };


    // -----------------------------------------------------------------------
    // Event
    // -----------------------------------------------------------------------

    public static final F<Event, EventV2> eventV2 = new F<Event, EventV2>() {
        public EventV2 f(Event event) {
            EventV2 e = objectFactory.createEventV2();
            e.setName(event.getName());
            e.setUuid(event.getDisplayID());
            if (event.getHandle() != null) {
                e.setUri(event.getHandle().getURI().toString());
            }
            if (event.getSessionURI() != null) {
                e.setSessionsUri(event.getSessionURI().toString());
            }
            e.setDate(fromNull(event.getStartDate()).map(toXmlGregorianCalendar).orSome((XMLGregorianCalendar) null));
            e.setRooms(new RoomListV2());
            List.iterableList(event.getRooms()).
                    map(roomV2).
                    foreach(curry(ExternalV2F.<RoomV2>add(), e.getRooms().getRoom()));
            e.setTags(convertTags(event));
            return e;
        }
    };

    public static final F<P2<EventV2, Headers>, Event> eventFromRequest = new F<P2<EventV2, Headers>, Event>() {
        public Event f(P2<EventV2, Headers> p2) {
            // TODO: use the Link header
            URI sessionUri = null;
            return eventFromV2(p2._1(), sessionUri);
        }
    };

    public static final F<EventV2, Event> event = new F<EventV2, Event>() {
        public Event f(EventV2 event) {
            return eventFromV2(event, null);
        }
    };

    private static Event eventFromV2(EventV2 event, URI sessionUri) {
        Event e = new Event(event.getName());
        e.setHandle(new ResourceHandle(URI.create(event.getUri())));
        e.setDisplayID(event.getUuid());
        e.setSessionURI(sessionUri);
        e.setStartDate(fromNull(event.getDate()).map(toLocalDate).orSome((LocalDate) null));
        e.setTags(new ArrayList<String>(event.getTags().getTag()));
        e.setModified(false);
        return e;
    }


    public static final F<Room, RoomV2> roomV2 = new F<Room, RoomV2>() {
        public RoomV2 f(Room room) {
            RoomV2 roomV2 = new RoomV2();
            if (room.getHandle() != null) {
                roomV2.setUri(room.getHandle().toString());
            }
            roomV2.setName(room.getName());
            roomV2.setDescription(room.getDescription());
            return roomV2;
        }
    };

    public static final F<RoomV2, Room> room = new F<RoomV2, Room>() {
        public Room f(RoomV2 externalRoom) {
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
