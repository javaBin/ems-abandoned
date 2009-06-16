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

import no.java.ems.server.search.SearchService;
import no.java.ems.server.search.SolrSearchService;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @author last modified by $Author: $
 * @version $Id: $
 */
public class SolrSearchingIntegrationTest extends AbstractSearchingIntegrationTest {
    protected SearchService createSearchService() throws Exception {
        File luceneHome = PlexusTestCase.getTestFile("target/solr");
        FileUtils.deleteDirectory(luceneHome);
        return new SolrSearchService(luceneHome);
    }
}