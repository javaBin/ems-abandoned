package no.java.ems.server.it;

import no.java.ems.domain.Session;
import no.java.ems.server.EmsServices;
import no.java.ems.server.search.LuceneSearchService;
import no.java.ems.server.search.SearchRequest;
import no.java.ems.server.search.SearchResponse;
import no.java.ems.server.search.SearchService;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractSearchingIntegrationTest extends Assert {
    private static EmsServices emsServices;

    protected abstract SearchService createSearchService() throws Exception;

    @Test
    public void testBasic() throws Exception {
        SearchService searchService = createSearchService();

        Session session = new Session();
        session.setId("123");
        session.setEventId("jz08");
        session.setTitle("title");
        session.setLead("lead");
        session.setBody("body");

        searchService.update(session);

        SearchRequest request = new SearchRequest();
        request.setText("lead");
        SearchResponse response = searchService.search(request);
        assertNotNull(response);
        assertEquals(1, response.getHitCount());
        assertEquals("123", response.getHits().get(0).getId());
        assertEquals(SearchService.ObjectType.session, response.getHits().get(0).getType());

        // Add the same object again
        searchService.update(session);
        searchService.update(session);
        searchService.update(session);
        searchService.update(session);

        request = new SearchRequest();
        request.setText("lead");
        response = searchService.search(request);
        assertEquals(1, response.getHitCount());
        assertEquals("123", response.getHits().get(0).getId());
        assertEquals(SearchService.ObjectType.session, response.getHits().get(0).getType());
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        File setupDirectory = PlexusTestCase.getTestFile("target/setup");

        FileUtils.deleteDirectory(setupDirectory);

        emsServices = new EmsServices(setupDirectory, 0, true, false, 0, false);
        emsServices.getDerbyService().maybeCreateTables(false);
    }

    @AfterClass
    public static void afterClass() {
        if (emsServices != null) {
            emsServices.stop();
        }
    }
}
