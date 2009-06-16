package no.java.ems.client;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ClientCache<T extends Serializable> {

    private AbstractClient<T> client;

    private Ehcache cache;

    private boolean cacheGets;

    public ClientCache(AbstractClient<T> client, boolean cacheGets) {
        this.client = client;
        this.cacheGets = cacheGets;

        cache = CacheManager.getInstance().getCache("session");

        if (cache == null) {
            throw new RuntimeException("Unable to look up cache.");
        }
    }

    public List<T> getList(List<String> ids) {
        List<T> ts = new ArrayList<T>(ids.size());

        for (String id : ids) {
            ts.add(get(id));
        }

        return ts;
    }

    public T get(String id) {
        Element element = cache.get(id);
        if (element != null && !element.isExpired()) {
            //noinspection unchecked
            return (T) element.getValue();
        }

        T object = client.get(id);

        if (cacheGets) {
            cache.put(new Element(id, object));
        }

        return object;
    }

    public void evict(String id) {
        cache.remove(id);
    }
}
