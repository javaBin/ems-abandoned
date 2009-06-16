package no.java.ems.client;

import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.payload.Payload;
import org.apache.commons.io.IOUtils;

import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class URIListHandler implements Handler{
    private static final MIMEType URI_LIST = MIMEType.valueOf("text/uri-list");

    public boolean supports(MIMEType type) {
        return URI_LIST.includes(type);
    }

    public Object handle(Payload payload) {
        List<URI> uris = new ArrayList<URI>();
        InputStream stream = payload.getInputStream();
        try {
            String value = IOUtils.toString(stream);
            String[] list = value.split("\r\n");
            for (String uri : list) {
                if (uri.charAt(0) == '#') {
                    continue;
                }
                uris.add(URI.create(uri));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return uris;
    }
}
