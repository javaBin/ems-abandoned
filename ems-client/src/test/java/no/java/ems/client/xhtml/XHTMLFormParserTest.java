package no.java.ems.client.xhtml;

import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class XHTMLFormParserTest {
    @Test
    public void testParseCorrectForm() throws XMLStreamException {
        InputStream stream = getClass().getResourceAsStream("/search-form.xhtml");
        assertNotNull("No from available", stream);
        XHTMLFormParser parser = new XHTMLFormParser(new InputStreamPayload(stream, MIMEType.valueOf("application/xhtml+xml")));
        Form form = parser.parse();
        assertForm(form, 2);
    }

    @Test
    public void testParseCorrectNestedForm() throws XMLStreamException {
        InputStream stream = getClass().getResourceAsStream("/search-form.xhtml");
        assertNotNull("No from available", stream);
        XHTMLFormParser parser = new XHTMLFormParser(new InputStreamPayload(stream, MIMEType.valueOf("application/xhtml+xml")));
        Form form = parser.parse();
        assertForm(form, 2);
    }

    private void assertForm(Form form, final int expected) {
        assertNotNull("Form was not parsed correctly", form);
        assertEquals(expected, form.getForm().size());
        Options options = form.getOptions("type");
        assertNotNull("No options available", options);
        assertNotNull("Query form not available", form.getTextElement("q"));
    }

    @Test(expected = XMLStreamException.class)
    public void testParseWithNoForm() throws XMLStreamException {
        InputStream stream = getClass().getResourceAsStream("/search-form-failed.xhtml");
        assertNotNull("No from available", stream);
        XHTMLFormParser parser = new XHTMLFormParser(new InputStreamPayload(stream, MIMEType.valueOf("application/xhtml+xml")));
        parser.parse();
    }
}
