package no.java.ems.client.swing.binding;

import no.java.ems.domain.EmailAddress;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class EmailConverter extends ListConverter<EmailAddress> {

    protected EmailAddress fromString(final String emailAddress) {
        return emailAddress == null || emailAddress.isEmpty() ? null : new EmailAddress(emailAddress);
    }

    protected String toString(final EmailAddress emailAddress) {
        return emailAddress == null ? null : emailAddress.getEmailAddress();
    }

}
