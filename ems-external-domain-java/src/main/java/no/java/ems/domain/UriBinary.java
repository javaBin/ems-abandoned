package no.java.ems.domain;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class UriBinary extends Binary {

    private final URI uri;

    public UriBinary(final String id, final String fileName, final String mimeType, final long size, final URI uri) {
        super(id, fileName, mimeType, size);
        Validate.notNull(uri, "URI may not be null");
        try {
            uri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URI: " + uri, e);
        }
        this.uri = uri;
    }

    public InputStream getDataStream() {
        try {
            return uri.toURL().openStream();
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public boolean equals(final Object other) {
        if (other != null && getClass().equals(other.getClass())) {
            UriBinary otherBinary = (UriBinary)other;
            return new EqualsBuilder()
                    .append(getFileName(), otherBinary.getFileName())
                    .append(getSize(), otherBinary.getSize())
                    .append(getUri(), otherBinary.getUri())
                    .append(getMimeType() == null ? null : getMimeType(), otherBinary.getMimeType() == null ? null : otherBinary.getMimeType())
                    .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getFileName())
                .append(getSize())
                .append(getUri())
                .append(getMimeType() == null ? null : getMimeType())
                .toHashCode();
    }

}
