package no.java.ems.domain;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ByteArrayBinary extends Binary {

    private final byte[] data;

    public ByteArrayBinary(final String id, final String fileName, final String mimeType, final byte[] data) {
        super(id, fileName, mimeType, data == null ? -1 : data.length);
        Validate.notNull(data, "Data may not be null");
        this.data = data;
    }

    public InputStream getDataStream() {
        return new ByteArrayInputStream(data);
    }

    public byte[] getData() {
        return data.clone();
    }

    @Override
    public boolean equals(final Object other) {
        if (other != null && getClass().equals(other.getClass())) {
            ByteArrayBinary otherBinary = (ByteArrayBinary)other;
            return new EqualsBuilder()
                    .append(getFileName(), otherBinary.getFileName())
                    .append(getSize(), otherBinary.getSize())
                    .append(getData(), otherBinary.getData())
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
                .append(getData())
                .append(getMimeType() == null ? null : getMimeType())
                .toHashCode();
    }

}
