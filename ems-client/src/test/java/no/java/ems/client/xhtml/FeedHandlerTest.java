package no.java.ems.client.xhtml;

import no.java.ems.client.FeedHandler;
import org.apache.abdera.model.Feed;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class FeedHandlerTest {
    @Test
    public void testNormalFeedParsingWorks() {
        InputStream atomStream = getClass().getResourceAsStream("/search-result-atom.xml");
        Feed feed = new FeedHandler().handle(atomStream);
        Assert.assertNotNull("Feed may not be null", feed);
    }
}
