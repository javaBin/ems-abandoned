package no.java.ems.domain.search;

import java.net.URI;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class SearchResult {
    private URI uri;
    private String title;
    private ObjectType objectType;
    private String summary;

    public SearchResult(URI uri, String title, ObjectType objectType, String summary) {
        this.uri = uri;
        this.title = title;
        this.objectType = objectType;
        this.summary = summary;
    }

    public URI getUri() {
        return uri;
    }

    public String getTitle() {
        return title;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public String getSummary() {
        return summary;
    }
}
