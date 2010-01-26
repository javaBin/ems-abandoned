package no.java.ems.server.resources.v1;

import no.java.ems.server.URIBuilder;
import no.java.ems.server.domain.ObjectType;
import no.java.ems.server.search.SearchRequest;
import no.java.ems.server.search.SearchResponse;
import no.java.ems.server.search.SearchService;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Properties;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
@Component
@Path("/1/search/")
@Produces("application/vnd.javabin.search+xml")
public class SearchResource {
    private SearchService searchService;
    private VelocityEngine velocityEngine;

    @Context
    private UriInfo uriInfo;

    @Autowired
    public SearchResource(SearchService searchService) {
        this.searchService = searchService;
    }

    private Response form() {
        VelocityContext context = new VelocityContext();
        context.put("action", uriInfo.getBaseUriBuilder().path("/1/search").build());
        context.put("types", ObjectType.values());
        String form = render(context, "/jersey/search-form.vm");

        return Response.ok(form).build();
    }

    private String render(VelocityContext context, final String path) {
        StringWriter writer = new StringWriter();
        VelocityEngine engine = getVelocityEngine();
        try {
            engine.evaluate(context, writer, "form", new InputStreamReader(getClass().getResourceAsStream(path)));
        } catch (IOException e) {
            throw new WebApplicationException(404);
        }
        return writer.toString();
    }

    private VelocityEngine getVelocityEngine() {
        if (velocityEngine == null) {
            try {
                VelocityEngine engine = new VelocityEngine(new Properties());
                engine.init();
                velocityEngine = engine;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return velocityEngine;
    }

    @GET
    @Produces("application/xhtml+xml")
    public Response query(@QueryParam("q") String query, @QueryParam("type") String type, @QueryParam("offset") int start, @QueryParam("limit") int rows) {
        if (StringUtils.isBlank(query) && StringUtils.isBlank(type)) {
            return form();
        }
        else {
            SearchRequest request = new SearchRequest();
            request.setOffset(start);
            request.setLimit(rows == 0 ? 10 : rows);
            if (!StringUtils.isBlank(type)) {
                ObjectType objectType;
                try {
                    objectType = ObjectType.valueOf(type);
                } catch (IllegalArgumentException e) {
                    return Response.status(400).build();
                }
                request.setObjectType(objectType);
            }
            request.setText(query);
            SearchResponse response = searchService.search(request, new URIBuilder(uriInfo.getBaseUriBuilder()));
            VelocityContext context = new VelocityContext();
            context.put("result", response);
            String rendered = render(context, "/jersey/search-result-xhtml.vm");
            return Response.ok(rendered).build();
        }

    }
}
