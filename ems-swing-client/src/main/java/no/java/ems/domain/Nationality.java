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
