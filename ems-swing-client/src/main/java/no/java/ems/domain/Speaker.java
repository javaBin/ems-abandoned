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

import no.java.ems.client.ResourceHandle;
import no.java.swing.WeakReferencePropertyChangeListener;
import org.apache.commons.lang.Validate;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class Speaker extends ValueObject {

    private final PropertyChangeSupport changeSupport = new SwingPropertyChangeSupport(this, true);
    private final String name;
    private final URI personURI;
    private String description;
    private Binary photo;

    /**
     * @param personURI person identifer. May not be {@code null}.
     * @param name the name of the person. May not be null.
     * @throws IllegalArgumentException if the person identifier is {@code null}.
     */
    public Speaker(final URI personURI, final String name) {
        Validate.notNull(personURI, "Person identifier may not be null.");
        Validate.notNull(name, "Person name may not be null.");
        this.name = name;
        this.personURI = personURI;
    }

    public URI getPersonURI() {
        return personURI;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        changeSupport.firePropertyChange("description", this.description, this.description = description);
    }

    public Binary getPhoto() {
        return photo;
    }

    public void setPhoto(final Binary photo) {
        changeSupport.firePropertyChange("photo", this.photo, this.photo = photo);
    }

    public String getName() {
        return name;
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, new WeakReferencePropertyChangeListener(listener));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        Speaker speaker = (Speaker) o;

        if (personURI != null ? !personURI.equals(speaker.personURI) : speaker.personURI != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (personURI != null ? personURI.hashCode() : 0);
        return result;
    }
}
