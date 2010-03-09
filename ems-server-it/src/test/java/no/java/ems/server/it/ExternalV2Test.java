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

package no.java.ems.server.it;

import no.java.ems.external.v2.EmsV2Client;
import no.java.ems.external.v2.EventListV2;
import no.java.ems.external.v2.RESTfulEmsV2Client;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ExternalV2Test extends AbstractIntegrationTest {

    @Test
    public void testGetEvents() throws Exception {
        HTTPCache cache = new InMemoryHttpCache();

        EmsV2Client client = new RESTfulEmsV2Client(cache, null, null);
        client.login(baseUri);

        EventListV2 events = client.getEvents();

        assertNotNull(events);
        assertTrue(events.getEvent().size() > 0);
    }
}
