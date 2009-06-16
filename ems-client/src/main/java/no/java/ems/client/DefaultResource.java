package no.java.ems.client;

import org.apache.commons.lang.Validate;
import org.codehaus.httpcache4j.Headers;
import fj.data.Option;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
* @version $Revision: #5 $ $Date: 2008/09/15 $
*/
public class DefaultResource implements Resource {
    private final ResourceHandle handle;
    private final Headers headers;
    private final Object data;

    public DefaultResource(ResourceHandle handle, Headers headers, Object data) {
        this.headers = headers;
        Validate.notNull(handle, "Resource Handle may not be null");
        this.handle = handle;
        this.data = data;
    }

    public ResourceHandle getResourceHandle() {
        return handle;
    }

    public Headers getHeaders() {
        return headers;
    }

    public <T> Option<T> getData(Class<T> type) {
        if (data == null) {
            return Option.none();
        }
        return Option.some(type.cast(data));
    }
}
