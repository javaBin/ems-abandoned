package no.java.ems.server.domain;

import org.apache.commons.lang.Validate;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class Language extends ValueObject {

    private final String isoCode;

    /**
     * @throws IllegalArgumentException if ISO code is {@code null}.
     */
    public Language(final String isoCode) {
        Validate.notNull(isoCode, "ISO code may not be null.");
        this.isoCode = isoCode;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public String getIndexingValue() {
        return isoCode;
    }

    public static Language valueOf(final String isoCode) {
        return isoCode == null || isoCode.length() == 0 ? null : new Language(isoCode);
    }

}
