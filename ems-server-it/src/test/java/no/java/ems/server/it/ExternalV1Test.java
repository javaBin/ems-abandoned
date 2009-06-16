package no.java.ems.server.it;

import no.java.ems.external.v1.EmsV1Client;
import no.java.ems.external.v1.EventListV1;
import no.java.ems.external.v1.RestletEmsV1Client;
import org.apache.commons.httpclient.HttpClient;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.cache.InMemoryResponseCreator;
import org.codehaus.httpcache4j.cache.MemoryCacheStorage;
import org.codehaus.httpcache4j.client.HTTPClientResponseResolver;
import org.codehaus.httpcache4j.resolver.ResponseCreator;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ExternalV1Test extends AbstractIntegrationTest {

    @Test
    public void testGetEvents() throws Exception {
        ResponseCreator creator = new InMemoryResponseCreator();
        HTTPClientResponseResolver clientResponseResolver = new HTTPClientResponseResolver(new HttpClient(), creator);
        HTTPCache cache = new HTTPCache(new MemoryCacheStorage(1000), clientResponseResolver);

        EmsV1Client client = new RestletEmsV1Client(cache, IncogitoIntegrationTest.baseUri);

        EventListV1 events = client.getEvents();

        assertNotNull(events);
        assertTrue(events.getEvent().size() > 0);
    }
}
