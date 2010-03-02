package no.java.ems.client;

import no.java.ems.client.xhtml.XHTMLFormParser;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.payload.Payload;

import javax.xml.stream.XMLStreamException;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class SearchFormDescriptionHandler implements Handler {
    private final MIMEType mimeType = MIMEType.valueOf("application/xhtml+xml");
    
    public boolean supports(MIMEType type) {
        return mimeType.equals(type);
    }

    public Object handle(Payload payload) {
        XHTMLFormParser form = new XHTMLFormParser(payload);
        try {
            return form.parse();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
}
