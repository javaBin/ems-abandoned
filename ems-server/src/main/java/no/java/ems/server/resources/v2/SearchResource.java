package no.java.ems.server.resources.v2;

import com.sun.syndication.feed.atom.*;
import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import no.java.ems.external.v2.MIMETypes;
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
import java.net.URI;
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
    private static final String ATOM = "application/atom+xml";
    private static final String XHTML = "application/xhtml+xml";
    private int rows;

    @Autowired
    public SearchResource(SearchService searchService) {
        this.searchService = searchService;
        rows = 10;
    }

    @GET
    @Path("form")
    @Produces(XHTML)
    public Response form(@Context UriInfo info) {
        URIBuilder uriBuilder = new URIBuilder(info.getBaseUriBuilder());
        VelocityContext context = new VelocityContext();
        context.put("action", uriBuilder.search().getURI());
        context.put("types", ObjectType.values());
        String form = render(context, "/jersey/search-form.vm");

        return Response.ok(form).type(XHTML).build();
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
    @Produces(ATOM)
    public Response query(@Context UriInfo info, @QueryParam("q") String query, @QueryParam("type") String type, @QueryParam("pw") int page) {
        URIBuilder uriBuilder = new URIBuilder(info.getBaseUriBuilder());
        if (StringUtils.isBlank(query) && StringUtils.isBlank(type)) {
            return Response.status(400).build();
        }
        else if (StringUtils.isBlank(type)) {
            return Response.status(400).build();
        }
        SearchRequest request = new SearchRequest();
        if (page == 0) {
            page = 1;
        }
        request.setOffset(page-1);
        request.setLimit(rows);
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
        if (!isPageWithinResult(page, response.getHitCount())) {
            return Response.status(404).build();
        }

        Feed feed = new Feed("atom_1.0");
        URI self = uriBuilder.search().getURI(query, type, page);

        feed.setAuthors(Collections.singletonList(createPerson("EMS")));
        feed.setId(self.toString());
        feed.setEncoding("UTF-8");
        feed.setTitle(response.getHitCount() == 0 ? "No hits" : response.getHitCount() + " hit(s)");
        ArrayList<Link> links = new ArrayList<Link>();
        links.add(createLink("search", uriBuilder.search().form(), XHTML));
        links.add(createLink("self", self, ATOM));

        if (page > 1) {
            links.add(createLink("previous", uriBuilder.search().getURI(query, type, page), ATOM));
        }
        if (hasNext(page, response.getHitCount())) {
            links.add(createLink("next", uriBuilder.search().getURI(query, type, page), ATOM));
        }
        feed.setOtherLinks(links);

        List<Entry> entries = new ArrayList<Entry>();
        for (SearchResponse.Hit hit : response.getHits()) {
            entries.add(toEntry(hit));
        }
        feed.setEntries(entries);
        SyndFeedOutput out = new SyndFeedOutput();
        try {
            return Response.ok(out.outputString(new SyndFeedImpl(feed))).build();
        } catch (FeedException e) {
            throw new WebApplicationException(Response.status(500).entity(e.getMessage()).build());
        }
    }

    private boolean isPageWithinResult(int page, long hitCount) {
        return page == 1 || rows <= hitCount || hasNext(page, hitCount);
    }

    private Person createPerson(String name) {
        Person person = new Person();
        person.setName(name);        
        return person;
    }

    private boolean hasNext(int page, long hitCount) {
        return (page * rows) < hitCount;
    }

    private Link createLink(String relation, URI uri, final String mimeType) {
        Link link = new Link();
        link.setRel(relation);
        link.setType(mimeType);
        link.setHref(uri.toString());
        return link;
    }

    private Entry toEntry(SearchResponse.Hit hit) {
        Entry entry = new Entry();
        entry.setAuthors(Collections.singletonList(createPerson("EMS")));
        entry.setTitle(hit.getTitle());
        Content summary = new Content();
        summary.setValue(hit.getSummary());
        entry.setSummary(summary);
        entry.setOtherLinks(Arrays.asList(createLink("edit", hit.getURI(), findMimeType(hit.getType()))));
        entry.setCategories(Arrays.asList(createTypeCategory(hit.getType())));
        return entry;
    }

    private Category createTypeCategory(ObjectType type) {
        Category cat = new Category();
        cat.setScheme("http://java.no/categories/ems/type");
        cat.setTerm(type.name());
        return cat;
    }

    private String findMimeType(ObjectType type) {
        String mimeType;
        switch (type) {
            case person:
                mimeType = MIMETypes.PERSON_MIME_TYPE;
                break;
            case session:
                mimeType = MIMETypes.SESSION_MIME_TYPE;
                break;
            case event:
                mimeType = MIMETypes.EVENT_MIME_TYPE;
                break;
            default:
                mimeType = "application/octet-stream";
                break;
        }
        return mimeType;
    }
}
