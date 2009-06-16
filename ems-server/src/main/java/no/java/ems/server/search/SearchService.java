package no.java.ems.server.search;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface SearchService {
    enum ObjectType {
        session,
        person,
        event
    }

    IndexStatistics getIndexStatistics();

    void update(Object o);

    void delete(Object o);

    SearchResponse search(SearchRequest request);

    class IndexStatistics {
        public int numberOfDocuments;
    }
}
