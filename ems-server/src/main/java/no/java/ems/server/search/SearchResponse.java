package no.java.ems.server.search;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.List;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
* @version $Id$
*/
public class SearchResponse {
    private long hitCount;

    private long executionTime;

    private List<Hit> hits;

    public static class Hit {
        SearchService.ObjectType type;
        String id;
        String internalId;

        public Hit(SearchService.ObjectType type, String id, String internalId) {
            this.type = type;
            this.id = id;
            this.internalId = internalId;
        }

        public SearchService.ObjectType getType() {
            return type;
        }

        public String getId() {
            return id;
        }

        public String getInternalId() {
            return internalId;
        }
    }

    public SearchResponse(long hitCount, long executionTime, List<Hit> hits) {
        this.hitCount = hitCount;
        this.executionTime = executionTime;
        this.hits = hits;
    }

    public long getHitCount() {
        return hitCount;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public List<Hit> getHits() {
        return hits;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
