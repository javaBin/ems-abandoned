/*
 * Copyright 2009 JavaBin
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

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
