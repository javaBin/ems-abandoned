package no.java.ems.client;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.Parser;
import org.codehaus.httpcache4j.MIMEType;

import java.io.InputStream;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class FeedHandler implements Handler {
    private final MIMEType supported = MIMEType.valueOf("application/atom+xml");
    
    public boolean supports(MIMEType type) {
        return supported.includes(type);
    }

    public Feed handle(InputStream payload) {
        Parser parser = Abdera.getNewParser();
        Document<Feed> feed = parser.parse(payload);
        return feed.getRoot();
    }

    public boolean needStreamAfterHandle() {
        return false;
    }
}
