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

package no.java.ems.server.domain;

import org.apache.commons.lang.Validate;

import java.io.InputStream;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public abstract class Binary {

    private final String id;
    private final String fileName;
    private final String mimeType;
    private final long size;

    protected Binary(String id, final String fileName, final String mimeType, final long size) {
        Validate.isTrue(size >= 0, "Size must be >= 0: " + size);
        this.id = id;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.size = size;
    }

    public abstract InputStream getDataStream();

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getSize() {
        return size;
    }
}
