package no.java.ems.client;

import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.payload.Payload;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
* @version $Revision: #5 $ $Date: 2008/09/15 $
*/
public class DefaultHandler implements Handler {
    public boolean supports(MIMEType type) {
        return MIMEType.ALL.includes(type);
    }

    public Object handle(Payload payload) {
        return payload.getInputStream();
    }
}
