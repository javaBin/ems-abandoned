package no.java.ems.server.restlet;

import no.java.ems.domain.AbstractEntity;
import no.java.ems.server.search.SearchService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public abstract class GenericResource<T extends AbstractEntity> extends Resource {

    protected static final String EOL = System.getProperty("line.separator");

    private final String idString;
    protected Log log = LogFactory.getLog(getClass());
    protected SearchService searchService;

    public GenericResource(Context context, Request request, Response response, String idString) {
        super(context, request, response);
        this.idString = idString;
        searchService = EmsApplication.getService(context, SearchService.class);
    }

    protected T extractObject(Representation entity) {
        InputStream data = null;
        try {
            data = entity.getStream();
            //noinspection unchecked
            return (T) SerializationUtils.deserialize(data);
        } catch (IOException e) {
            throw new RuntimeException("Error extracting Object from request", e);
        } finally {
            IOUtils.closeQuietly(data);
        }
    }

    protected String getId() {
        return (String) super.getRequest().getAttributes().get(idString);
    }

    /**
     * Indexes the given abstract entity with the configured SearchService.
     * 
     * @param entity the entity to index.
     * @param delete {@code true} if the entity should be removed from the index.
     */
    protected void index(T entity, boolean delete) {
        if (!delete) {
            searchService.update(entity);
        }
        else {
            searchService.delete(entity);
        }
    }
}
