/* 
 * $Header: $ 
 * 
 * Copyright (C) 2008 Escenic. 
 * All Rights Reserved.  No use, copying or distribution of this 
 * work may be made except in accordance with a valid license 
 * agreement from Escenic.  This notice must be 
 * included on all copies, modifications and derivatives of this 
 * work. 
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
