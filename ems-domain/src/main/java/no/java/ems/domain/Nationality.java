package no.java.ems.domain;

import org.apache.commons.lang.Validate;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class Nationality extends ValueObject {

    private final String isoCode;

    /**
     * @throws IllegalArgumentException if ISO code is {@code null}.
     */
    public Nationality(final String isoCode) {
        Validate.notNull(isoCode, "ISO code may not be null.");
        this.isoCode = isoCode;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public String getIndexingValue() {
        return isoCode;
    }

    public static Nationality valueOf(final String isoCode) {
        return isoCode == null || isoCode.length() == 0 ? null : new Nationality(isoCode);
    }

}
