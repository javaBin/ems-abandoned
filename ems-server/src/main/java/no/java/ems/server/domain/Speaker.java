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

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class Speaker extends AbstractEntity {

    private final String personId;
    private final String name;
    private String description;
    private Binary photo;

    /**
     * @param personId person identifer. May not be {@code null}.
     * @param name the name of the person. May not be null.
     * @throws IllegalArgumentException if the person identifier is {@code null}.
     */
    public Speaker(final String personId, final String name) {
        Validate.notNull(personId, "Person identifier may not be null.");
        Validate.notNull(name, "Person name may not be null.");
        this.name = name;
        this.personId = personId;
    }

    public String getPersonId() {
        return personId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Binary getPhoto() {
        return photo;
    }

    public void setPhoto(final Binary photo) {
        this.photo = photo;
    }

    public String getName() {
        return name;
    }
}
