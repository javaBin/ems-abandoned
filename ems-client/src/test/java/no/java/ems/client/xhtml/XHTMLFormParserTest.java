package no.java.ems.client.xhtml;

import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.List;

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
        List<Form> form = parser.parse();
        assertForms(form, 2);
    }

    @Test
    public void testParseCorrectNestedForm() throws XMLStreamException {
        InputStream stream = getClass().getResourceAsStream("/search-form.xhtml");
        assertNotNull("No from available", stream);
        XHTMLFormParser parser = new XHTMLFormParser(new InputStreamPayload(stream, MIMEType.valueOf("application/xhtml+xml")));
        List<Form> form = parser.parse();
        assertForms(form, 2);
    }

    private void assertForms(List<Form> forms, final int expected) {
        assertNotNull("Form was not parsed correctly", forms);
        assertEquals("Form was not parsed correctly", 1, forms.size());
        Form form = forms.get(0);
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
