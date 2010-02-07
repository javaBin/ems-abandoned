/*
 * Copyright 2009 JavaBin
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package no.java.ems.server.search;

import no.java.ems.server.domain.ObjectType;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.net.URI;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
* @version $Id$
*/
public class SearchResponse {
    private long hitCount;

    private long executionTime;

    private List<Hit> hits;

    public static class Hit {
        private final ObjectType type;
        private final URI uri;
        private final String title;
        private final String summary;

        public Hit(ObjectType type, URI uri, String title, String summary) {
            this.type = type;
            this.uri = uri;
            this.title = title;
            this.summary = summary;
        }

        public ObjectType getType() {
            return type;
        }

        public URI getURI() {
            return uri;
        }

        public String getTitle() {
            return this.title;
        }

        public String getSummary() {
            return summary;
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
