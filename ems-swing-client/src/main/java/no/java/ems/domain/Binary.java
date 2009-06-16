package no.java.ems.domain;

import org.apache.commons.lang.Validate;

import java.io.InputStream;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public abstract class Binary extends ValueObject {

    private final String fileName;
    private final String mimeType;
    private final long size;

    protected Binary(final String fileName, final String mimeType, final long size) {
        Validate.isTrue(size >= 0, "Size must be >= 0: " + size);
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.size = size;
    }

    public abstract InputStream getDataStream();

    public String getFileName() {
        return fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getSize() {
        return size;
    }

    public String getIndexingValue() {
        return fileName;
    }
}
