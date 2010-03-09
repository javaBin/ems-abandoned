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

import org.codehaus.httpcache4j.MIMEType;
import org.apache.commons.io.IOUtils;

import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class URIListHandler implements Handler{
    private static final MIMEType URI_LIST = MIMEType.valueOf("text/uri-list");

    public boolean supports(MIMEType type) {
        return URI_LIST.includes(type);
    }

    public Object handle(InputStream payload) {
        List<URI> uris = new ArrayList<URI>();
        try {
            String value = IOUtils.toString(payload);
            String[] list = value.split("\r\n");
            for (String uri : list) {
                if (uri.charAt(0) == '#') {
                    continue;
                }
                uris.add(URI.create(uri));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return uris;
    }
}
