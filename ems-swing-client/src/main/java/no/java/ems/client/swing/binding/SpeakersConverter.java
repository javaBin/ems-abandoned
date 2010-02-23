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

package no.java.ems.client.swing.binding;

import no.java.ems.client.ResourceHandle;
import no.java.ems.client.swing.Entities;
import no.java.ems.domain.Person;
import no.java.ems.domain.Speaker;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class SpeakersConverter extends ListConverter<Speaker> {

    protected Speaker fromString(final String text) {
        throw new UnsupportedOperationException();
    }

    protected String toString(final Speaker speaker) {
        Person person = Entities.getInstance().getContact(new ResourceHandle(speaker.getPersonURI()));
        return person == null ? speaker.getPersonURI() + "???" : person.getName();
    }

}
