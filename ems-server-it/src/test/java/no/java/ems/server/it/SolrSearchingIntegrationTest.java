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

import no.java.ems.server.domain.EmsServerConfiguration;
import no.java.ems.server.search.SearchService;
import no.java.ems.server.search.SolrSearchService;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;

import fj.data.Option;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @author last modified by $Author: $
 * @version $Id: $
 */
public class SolrSearchingIntegrationTest extends AbstractSearchingIntegrationTest {
    protected SearchService createSearchService() throws Exception {
        File home = PlexusTestCase.getTestFile("target/solr");
        FileUtils.deleteDirectory(home);
        return new SolrSearchService(new EmsServerConfiguration(home, Option.<Integer>none()));
    }
}
