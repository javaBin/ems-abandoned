package no.java.ems.server.resources.v1;

import no.java.ems.server.search.SearchRequest;
import no.java.ems.server.search.SearchResponse;
import no.java.ems.server.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
@Component
@Path("/1/search/")
public class SearchResource {
    private SearchService searchService;

    @Autowired
    public SearchResource(SearchService searchService) {
        this.searchService = searchService;
    }

    @GET
    public Response form() {
        return Response.ok().build();
    }

    @GET
    @POST
    public Response query(@QueryParam("q") String query) {
        SearchRequest request = new SearchRequest();
        request.setText(query);
        SearchResponse response = searchService.search(request);
        
        return Response.ok().build();
    }
}
