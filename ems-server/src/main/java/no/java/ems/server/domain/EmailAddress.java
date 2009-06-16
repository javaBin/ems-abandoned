package no.java.ems.server.domain;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * Email address value object. See <a href="http://www.regular-expressions.info/email.html">this page</a>
 * for a discussion on the regular expression used for validating email addresses.
 *
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class EmailAddress extends ValueObject implements Comparable<EmailAddress> {

    private final String emailAddress;

    /**
     * @throws IllegalArgumentException if the email address is {@code null} or incorrectly formatted.
     */
    public EmailAddress(final String emailAddress) {
        Validate.notNull(emailAddress, "Email address may not be null.");
        if (!emailAddress.matches("(?i)\\b[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b")) {
            throw new IllegalArgumentException("Not a valid email address: " + emailAddress);
        }
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * @throws IllegalArgumentException if name is {@code null} or <em>empty</em>.
     */
    public String getEmailAddress(final String name) {
        Validate.notNull(name, "Name may not be null.");
        Validate.notEmpty(name, "Name may not be empty.");
        return String.format("\"%s\" <%s>", name, emailAddress);
    }

    public static boolean isValidEmailAddress(String emailAddress) {
        try {
            new EmailAddress(emailAddress);
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    /**
     * Compares based on the persons email address.
     */
    public int compareTo(final EmailAddress other) {
        return new CompareToBuilder().append(emailAddress, other == null ? null : other.getEmailAddress()).toComparison();
    }

    public String getIndexingValue() {
        return emailAddress;
    }
}
