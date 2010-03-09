package no.java.ems.external.v2;

import no.java.ems.client.Handler;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.payload.Payload;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
* @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
* @version $Revision: $
*/
public class JAXBHandler<T> implements Handler {
    private final MIMEType mimeType;
    private final Unmarshaller unmarshaller;
    private final Class<T> type;

    public JAXBHandler(JAXBContext context, Class<T> type, MIMEType mimeType) throws JAXBException {
        this.mimeType = mimeType;
        this.type = type;
        unmarshaller = context.createUnmarshaller();
    }

    public boolean supports(MIMEType type) {
        return mimeType.includes(type);
    }

    @SuppressWarnings({"unchecked"})
    public T handle(Payload payload) {
        try {
            Source source = new StreamSource(payload.getInputStream());
            return unmarshaller.unmarshal(source, type).getValue();
        } catch (JAXBException e) {
            throw new RuntimeException("Unable to unmarshall.", e);
        }
    }

    public static <T> JAXBHandler<T> create(JAXBContext context, Class<T> type, MIMEType mimeType) throws JAXBException {
        return new JAXBHandler<T>(context, type, mimeType);
    }
}
