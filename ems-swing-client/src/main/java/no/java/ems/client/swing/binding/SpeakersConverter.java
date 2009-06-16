package no.java.ems.client.swing.binding;

import no.java.ems.client.swing.Entities;
import no.java.ems.domain.Person;
import no.java.ems.domain.Speaker;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class SpeakersConverter extends ListConverter<Speaker> {

    protected Speaker fromString(final String text) {
        throw new UnsupportedOperationException();
    }

    protected String toString(final Speaker speaker) {
        Person person = Entities.getInstance().getContact(speaker.getPersonId());
        return person == null ? speaker.getPersonId() + "???" : person.getName();
    }

}
