package no.java.ems.server.resources.java;

import fj.*;

import static fj.data.Option.*;

import no.java.ems.domain.*;
import no.java.ems.server.domain.*;
import no.java.ems.server.domain.AbstractEntity;
import no.java.ems.server.domain.Binary;
import no.java.ems.server.domain.EmailAddress;
import no.java.ems.server.domain.Event;
import no.java.ems.server.domain.Language;
import no.java.ems.server.domain.Nationality;
import no.java.ems.server.domain.Person;
import no.java.ems.server.domain.Room;
import no.java.ems.server.domain.Session;
import no.java.ems.server.domain.Speaker;
import no.java.ems.server.domain.UriBinary;
import org.joda.time.Interval;

import java.util.*;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ExternalEmsDomainJavaF {

    public static <A, B, L extends List<A>> ArrayList<B> mapArrayList(L as, F<A, B> f) {
        ArrayList<B> bs = new ArrayList<B>();
        for (A a : as) {
            bs.add(f.f(a));
        }
        return bs;
    }

    public static F<Event, no.java.ems.domain.Event> eventToExternal = new F<Event, no.java.ems.domain.Event>() {
        public no.java.ems.domain.Event f(Event event) {
            no.java.ems.domain.Event externalEvent = copy(event, new no.java.ems.domain.Event(event.getName()));
            externalEvent.setDate(event.getDate());
            externalEvent.setRooms(mapArrayList(event.getRooms(), roomToExternal));
            externalEvent.setDate(event.getDate());
            externalEvent.setTimeslots(event.getTimeslots());
            return externalEvent;
        }
    };

    public static F<EmailAddress, no.java.ems.domain.EmailAddress> emailAddressToExternal = new F<EmailAddress, no.java.ems.domain.EmailAddress>() {
        public no.java.ems.domain.EmailAddress f(EmailAddress emailAddress) {
            return new no.java.ems.domain.EmailAddress(emailAddress.getEmailAddress());
        }
    };

    private static F<no.java.ems.domain.EmailAddress, EmailAddress> externalToEmailAddresses = new F<no.java.ems.domain.EmailAddress, EmailAddress>() {
        public EmailAddress f(no.java.ems.domain.EmailAddress emailAddress) {
            return new EmailAddress(emailAddress.getEmailAddress());
        }
    };


    public static F<Room, no.java.ems.domain.Room> roomToExternal = new F<Room, no.java.ems.domain.Room>() {
        public no.java.ems.domain.Room f(Room room) {
            no.java.ems.domain.Room externalRoom = copy(room, new no.java.ems.domain.Room(room.getName()));
            externalRoom.setDescription(room.getDescription());
            return externalRoom;
        }
    };

    public static F<Binary, no.java.ems.domain.Binary> binaryToExternal = new F<Binary, no.java.ems.domain.Binary>() {
        public no.java.ems.domain.Binary f(Binary binary) {
            if (binary instanceof UriBinary) {
                UriBinary uriBinary = (UriBinary) binary;

                return new no.java.ems.domain.UriBinary(binary.getId(), binary.getFileName(),
                                                        binary.getMimeType(), binary.getSize(), uriBinary.getUri());
            }
            else {
                throw new RuntimeException("Can't convert binaries (I'm lazy): " + binary.getClass());
            }
        }
    };

    public static F<no.java.ems.domain.Binary, Binary> externalToBinary = new F<no.java.ems.domain.Binary, Binary>() {
        public Binary f(no.java.ems.domain.Binary binary) {
            if (binary instanceof no.java.ems.domain.UriBinary) {
                no.java.ems.domain.UriBinary uriBinary = (no.java.ems.domain.UriBinary) binary;

                return new UriBinary(binary.getId(), binary.getFileName(),
                                     binary.getMimeType(), binary.getSize(), uriBinary.getUri());
            }
            else {
                throw new RuntimeException("Can't convert binaries (I'm lazy): " + binary.getClass());
            }
        }
    };

    public static F<Language, no.java.ems.domain.Language> languageToExternal = new F<Language, no.java.ems.domain.Language>() {
        public no.java.ems.domain.Language f(Language language) {
            return no.java.ems.domain.Language.valueOf(language.getIsoCode());
        }

    };
    public static F<no.java.ems.domain.Language, Language> externalTolanguage = new F<no.java.ems.domain.Language, Language>() {
        public Language f(no.java.ems.domain.Language language) {
            return Language.valueOf(language.getIsoCode());
        }
    };

    public static F<Nationality, no.java.ems.domain.Nationality> nationalityToExternal = new F<Nationality, no.java.ems.domain.Nationality>() {
        public no.java.ems.domain.Nationality f(Nationality nationality) {
            return no.java.ems.domain.Nationality.valueOf(nationality.getIsoCode());
        }
    };

    private static F<no.java.ems.domain.Nationality, Nationality> externalToNationality = new F<no.java.ems.domain.Nationality, Nationality>() {
        public Nationality f(no.java.ems.domain.Nationality nationality) {
            return Nationality.valueOf(nationality.getIsoCode());
        }
    };
    
    public static F<Speaker, no.java.ems.domain.Speaker> speakerToExternal = new F<Speaker, no.java.ems.domain.Speaker>() {
        public no.java.ems.domain.Speaker f(Speaker speaker) {
            no.java.ems.domain.Speaker s = new no.java.ems.domain.Speaker(speaker.getPersonId(), speaker.getName());
            copy(speaker, s);
            s.setDescription(speaker.getDescription());
            s.setPhoto(fromNull(speaker.getPhoto()).map(binaryToExternal).orSome((no.java.ems.domain.Binary) null));
            return s;
        }
    };

    public static F<no.java.ems.domain.Speaker, Speaker> externalToSpeaker = new F<no.java.ems.domain.Speaker, Speaker>() {
        public Speaker f(no.java.ems.domain.Speaker speaker) {
            Speaker s = new Speaker(speaker.getPersonId(), speaker.getName());
            copy(speaker, s);
            s.setDescription(speaker.getDescription());
            s.setPhoto(fromNull(speaker.getPhoto()).map(externalToBinary).orSome((Binary) null));
            return s;
        }
    };

    public static <A extends AbstractEntity, B extends no.java.ems.domain.AbstractEntity> B copy(A a, B b) {
        b.setId(a.getId());
        b.setRevision(a.getRevision());
        b.setNotes(a.getNotes());
        b.setTags(a.getTags());
        b.setAttachements(mapArrayList(a.getAttachments(), binaryToExternal));
        return b;
    }

    public static <A extends no.java.ems.domain.AbstractEntity, B extends AbstractEntity> B copy(A a, B b) {
        b.setId(a.getId());
        b.setRevision(a.getRevision());
        b.setNotes(a.getNotes());
        b.setTags(a.getTags());
        b.setAttachments(mapArrayList(a.getAttachements(), externalToBinary));
        return b;
    }

    public static F<Person, no.java.ems.domain.Person> personToExternal = new F<Person, no.java.ems.domain.Person>() {
        public no.java.ems.domain.Person f(Person person) {
            no.java.ems.domain.Person externalPerson = copy(person, new no.java.ems.domain.Person(person.getName()));
            externalPerson.setDescription(person.getDescription());
            externalPerson.setGender(no.java.ems.domain.Person.Gender.valueOf(person.getGender().name()));
            externalPerson.setBirthdate(person.getBirthdate());
            externalPerson.setLanguage(fromNull(person.getLanguage()).map(languageToExternal).orSome((no.java.ems.domain.Language) null));
            externalPerson.setNationality(fromNull(person.getNationality()).map(nationalityToExternal).orSome((no.java.ems.domain.Nationality) null));
            externalPerson.setEmailAddresses(mapArrayList(person.getEmailAddresses(), emailAddressToExternal));
            externalPerson.setPhoto(fromNull(person.getPhoto()).map(binaryToExternal).orSome((no.java.ems.domain.Binary) null));
            return externalPerson;
        }
    };

    public static F<no.java.ems.domain.Person, Person> externalToPerson = new F<no.java.ems.domain.Person, Person>() {
        public Person f(no.java.ems.domain.Person person) {
            Person internalPerson = copy(person, new Person(person.getName()));
            internalPerson.setDescription(person.getDescription());
            internalPerson.setGender(Person.Gender.valueOf(person.getGender().name()));
            internalPerson.setBirthdate(person.getBirthdate());
            internalPerson.setLanguage(fromNull(person.getLanguage()).map(externalTolanguage).orSome((Language) null));
            internalPerson.setNationality(fromNull(person.getNationality()).map(externalToNationality).orSome((Nationality) null));
            internalPerson.setEmailAddresses(mapArrayList(person.getEmailAddresses(), externalToEmailAddresses));
            internalPerson.setPhoto(fromNull(person.getPhoto()).map(externalToBinary).orSome((Binary) null));
            return internalPerson;
        }
    };

    public static F<Session, no.java.ems.domain.Session> sessionToExternal = new F<Session, no.java.ems.domain.Session>() {
        public no.java.ems.domain.Session f(Session session) {
            no.java.ems.domain.Session externalSession = copy(session, new no.java.ems.domain.Session(session.getTitle()));

            externalSession.setKeywords(session.getKeywords());
            externalSession.setBody(session.getBody());
            externalSession.setState(no.java.ems.domain.Session.State.valueOf(session.getState().name()));
            externalSession.setLevel(no.java.ems.domain.Session.Level.valueOf(session.getLevel().name()));
            externalSession.setLanguage(fromNull(session.getLanguage()).map(languageToExternal).orSome((no.java.ems.domain.Language) null));
            externalSession.setExpectedAudience(session.getExpectedAudience());
            externalSession.setOutline(session.getOutline());
            externalSession.setEventId(session.getEventId());
            externalSession.setLead(session.getLead());
            externalSession.setTimeslot(session.getTimeslot().orSome((Interval) null));
            externalSession.setRevision(session.getRevision());
            externalSession.setNotes(session.getNotes());
            externalSession.setFormat(no.java.ems.domain.Session.Format.valueOf(session.getFormat().name()));
            externalSession.setPublished(session.isPublished());
            externalSession.setSpeakers(mapArrayList(session.getSpeakers(), speakerToExternal));
            return externalSession;
        }
    };

    public static F<no.java.ems.domain.Session, Session> externalToSession = new F<no.java.ems.domain.Session, Session>() {
        public Session f(no.java.ems.domain.Session session) {
            Session internalSession = copy(session, new Session(session.getTitle()));

            internalSession.setKeywords(session.getKeywords());
            internalSession.setBody(session.getBody());
            internalSession.setState(Session.State.valueOf(session.getState().name()));
            internalSession.setLevel(Session.Level.valueOf(session.getLevel().name()));
            internalSession.setLanguage(fromNull(session.getLanguage()).map(externalTolanguage).orSome((Language) null));
            internalSession.setExpectedAudience(session.getExpectedAudience());
            internalSession.setOutline(session.getOutline());
            internalSession.setEventId(session.getEventId());
            internalSession.setLead(session.getLead());
            internalSession.setTimeslot(fromNull(session.getTimeslot()));
            internalSession.setRevision(session.getRevision());
            internalSession.setNotes(session.getNotes());
            internalSession.setFormat(Session.Format.valueOf(session.getFormat().name()));
            internalSession.setPublished(session.isPublished());
            internalSession.setSpeakers(mapArrayList(session.getSpeakers(), externalToSpeaker));
            return internalSession;
        }
    };
}
