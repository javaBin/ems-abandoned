package no.java.ems.client;

import fj.data.Option;
import org.codehaus.httpcache4j.Headers;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
* @version $Revision: #5 $ $Date: 2008/09/15 $
*/
public interface Resource {
    ResourceHandle getResourceHandle();

    <T> Option<T> getData(Class<T> type);

    Headers getHeaders();
}
