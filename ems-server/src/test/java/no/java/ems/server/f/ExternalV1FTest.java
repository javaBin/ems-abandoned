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
