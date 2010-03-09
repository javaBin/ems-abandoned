package no.java.ems.client;

import no.java.ems.domain.search.ObjectType;
import org.codehaus.httpcache4j.util.URIBuilder;

import java.net.URI;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class SearchForm {
    private String queryText;
    private ObjectType type;
    private URI uriBase;

    public SearchForm(URI uriBase, String queryText, ObjectType type) {
        this.queryText = queryText;
        this.type = type;
        this.uriBase = uriBase;
    }

    public URI toSearchURI() {
        return URIBuilder.fromURI(uriBase).addParameter("q", queryText).addParameter("type", type.name()).toURI();
    }
}
