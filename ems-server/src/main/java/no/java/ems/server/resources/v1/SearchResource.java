package no.java.ems.server.resources.v1;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.*;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
@Component
@Path("/2/search/")
public class SearchResource {
    private SearchService searchService;
    private VelocityEngine velocityEngine;

    @Autowired
    public SearchResource(SearchService searchService) {
        this.searchService = searchService;
    }

    private Response form(URIBuilder uriBuilder) {
        VelocityContext context = new VelocityContext();
        context.put("action", uriBuilder.search().getURI());
        context.put("types", ObjectType.values());
        String form = render(context, "/jersey/search-form.vm");

        return Response.ok(form).type("application/xhtml+xml").build();
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
    @Produces({"application/atom+xml", "application/xhtml+xml"})
    public Response query(@Context UriInfo info, @QueryParam("q") String query, @QueryParam("type") String type, @QueryParam("offset") int start, @QueryParam("limit") int rows) {
        URIBuilder uriBuilder = new URIBuilder(info.getBaseUriBuilder());
        if (StringUtils.isBlank(query) && StringUtils.isBlank(type)) {
            return form(uriBuilder);
        }
        if (rows == 0) {
            rows = 10;
        }
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
        SearchResponse response = searchService.search(request, uriBuilder);
        Feed feed = new Feed("atom_1.0");
        feed.setId(UriBuilder.fromUri(uriBuilder.search().getURI()).queryParam("q", query).queryParam("type", type).queryParam("offset", start).queryParam("limit", rows).build().toString());
        feed.setEncoding("UTF-8");
        List<Entry> entries = new ArrayList<Entry>();
        for (SearchResponse.Hit hit : response.getHits()) {
            entries.add(toEntry(hit));
        }
        feed.setEntries(entries);
        SyndFeedOutput out = new SyndFeedOutput();
        try {
            return Response.ok(out.outputString(new SyndFeedImpl(feed))).type("application/atom+xml").build();
        } catch (FeedException e) {
            throw new WebApplicationException(Response.status(500).entity(e.getMessage()).build());
        }
    }

    private Entry toEntry(SearchResponse.Hit hit) {
        Entry entry = new Entry();
        entry.setTitle(hit.getTitle());
        Content summary = new Content();
        summary.setValue(hit.getSummary());
        entry.setSummary(summary);
        Link editLink = new Link();
        editLink.setRel("edit");
        editLink.setHref(hit.getURI().toString());
        entry.setOtherLinks(Arrays.asList(editLink));
        return entry;
    }
}
