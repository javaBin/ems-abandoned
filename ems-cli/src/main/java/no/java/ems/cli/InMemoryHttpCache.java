package no.java.ems.cli;

import org.apache.commons.httpclient.HttpClient;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.cache.MemoryCacheStorage;
import org.codehaus.httpcache4j.cache.InMemoryResponseCreator;
import org.codehaus.httpcache4j.client.HTTPClientResponseResolver;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class InMemoryHttpCache extends HTTPCache {
    public InMemoryHttpCache() {
        super(new MemoryCacheStorage(1000), new HTTPClientResponseResolver(new HttpClient(), new InMemoryResponseCreator()));
    }
}
