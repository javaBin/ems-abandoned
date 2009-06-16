package no.java.ems.server.restlet;

import no.java.ems.server.search.SearchRequest;
import no.java.ems.server.search.SearchResponse;
import no.java.ems.server.search.SearchService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ObjectRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SessionSearchResource extends GenericListResource {

    private Log log = LogFactory.getLog(getClass());

    private SearchService searchService;
    private static final String EOL = System.getProperty("line.separator");

    public SessionSearchResource(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        getVariants().add(new Variant(MediaType.APPLICATION_JAVA_OBJECT));
        searchService = EmsApplication.getService(context, SearchService.class);
    }

    public Representation getRepresentation(Variant variant) {
        String eid = (String) getRequest().getAttributes().get("eid");
        Form query = getRequest().getResourceRef().getQueryAsForm(CharacterSet.UTF_8);

        String q = query.getFirstValue("q", null);
        int limit = NumberUtils.toInt(query.getFirstValue("limit", "10"));
        int offset = NumberUtils.toInt(query.getFirstValue("offset", "0"));

        SearchResponse searchResponse;
        ArrayList<String> resource;

        if (!StringUtils.isBlank(q)) {
            searchResponse = search(eid, q, limit, offset);

            List<SearchResponse.Hit> list = searchResponse.getHits();
            resource = new ArrayList<String>(list.size());
            for (SearchResponse.Hit hit : list) {
                resource.add(hit.getId());
            }
        } else {
            // TODO: return invalid request
            searchResponse = new SearchResponse(0, 0, new ArrayList<SearchResponse.Hit>());
            resource = new ArrayList<String>();
        }

        if (variant.getMediaType().equals(MediaType.APPLICATION_JAVA_OBJECT)) {
            return new ObjectRepresentation(resource);
        } else if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
            return new StringRepresentation(buildString(searchResponse));
        }
        return super.getRepresentation(variant);
    }

    public boolean allowGet() {
        return true;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private StringBuilder buildString(SearchResponse response) {
        StringBuilder string = new StringBuilder();
        string.append("Hits: ").append(response.getHitCount()).append(EOL);
        string.append("Execution time: ").append(response.getExecutionTime()).append("ms").append(EOL);
        string.append(EOL);
        for (SearchResponse.Hit hit : response.getHits()) {
            string.append(hit.getType()).append(":").append(hit.getId()).append(EOL);
        }
        return string;
    }

    private SearchResponse search(String eid, String q, int limit, int offset) {
        SearchRequest request = new SearchRequest();
        request.setEventId(eid);
        request.setText(q);
        request.setLimit(limit);
        request.setOffset(offset);

        log.info("Search request: " + request);
        SearchResponse response = searchService.search(request);
        log.info("Search response: " + response);

        return response;
    }
}
