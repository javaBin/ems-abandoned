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

package no.java.ems.server.f;

import junit.framework.TestCase;
import org.joda.time.LocalDate;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import no.java.ems.external.v1.EmsV1F;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ExternalV1FTest extends TestCase {
    public void testXmlDate() throws DatatypeConfigurationException {
        LocalDate date = new LocalDate(2008, 9, 12);
        XMLGregorianCalendar xmlDate = EmsV1F.toXmlGregorianCalendar.f(date);
    }
}
