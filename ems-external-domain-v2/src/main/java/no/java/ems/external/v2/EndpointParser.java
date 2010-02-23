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

package no.java.ems.external.v2;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.codehaus.httpcache4j.*;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;

import no.java.ems.client.RESTfulClient;
import no.java.ems.client.ResourceHandle;
import no.java.ems.client.Resource;
import fj.data.Option;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class EndpointParser {
    private RESTfulClient client;

    public EndpointParser(RESTfulClient client) {
        Validate.notNull(client, "client may not be null");
        this.client = client;
    }


    public Map<String, Endpoint> parse(URI baseURI) {
        Option<Resource> resourceOption = client.read(new ResourceHandle(baseURI), Collections.singletonList(MIMEType.ALL));
        if (resourceOption.isSome()) {
            InputStream data = resourceOption.some().getData(InputStream.class).some();
            try {
                String s = IOUtils.toString(data);
                JSONObject endpointObject = new JSONObject(s);
                Map<String, Endpoint> map = new HashMap<String, Endpoint>();
                Iterator iterator = endpointObject.sortedKeys();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    JSONObject object = endpointObject.getJSONObject(key);
                    map.put(key, new Endpoint(URI.create(object.getString("uri"))));
                }
                return map;
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(data);
            }
        }
        throw new IllegalStateException("Unable to parse Endpoint");
    }

    public static class Endpoint {
        private final URI uri;

        public Endpoint(URI uri) {
            this.uri = uri;
        }

        public URI getURI() {
            return uri;
        }

        public ResourceHandle getHandle() {
            return new ResourceHandle(uri);
        }
    }
}
