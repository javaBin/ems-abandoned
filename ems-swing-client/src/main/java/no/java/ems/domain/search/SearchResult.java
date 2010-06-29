package no.java.ems.domain.search;

import no.java.ems.client.ResourceHandle;

import java.net.URI;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class SearchResult {
    private final ObjectType objectType;
    private URI uri;
    private String title;
    private String summary;

    public SearchResult(ObjectType objectType, URI uri, String title, String summary) {
        this.objectType = objectType;
        this.uri = uri;
        this.title = title;
        this.summary = summary;
    }

    public ResourceHandle getHandle() {
        return new ResourceHandle(uri);
    }

    public URI getUri() {
        return uri;
    }

    public String getTitle() {
        return title;
    }
    
    public String getSummary() {
        return summary;
    }

    public ObjectType getType() {
        return objectType;
    }
}
