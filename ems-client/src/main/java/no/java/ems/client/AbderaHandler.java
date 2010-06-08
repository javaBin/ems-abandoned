package no.java.ems.client;

import org.apache.abdera.Abdera;
import org.codehaus.httpcache4j.MIMEType;

import java.io.InputStream;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class AbderaHandler implements Handler {
    public static final MIMEType ATOM = new MIMEType("application/atom+xml");

    public boolean supports(MIMEType type) {
        return ATOM.includes(type);
    }

    public Object handle(InputStream payload) {
        return Abdera.getInstance().getParser().parse(payload).getRoot();
    }

    public boolean needStreamAfterHandle() {
        return false;
    }
}
