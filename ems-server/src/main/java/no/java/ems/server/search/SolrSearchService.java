package no.java.ems.server.search;

import no.java.ems.domain.AbstractEntity;
import no.java.ems.domain.Event;
import no.java.ems.domain.Person;
import no.java.ems.domain.Session;
import no.java.ems.server.solr.ResourceToSolrTranslator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.schema.IndexSchema;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class SolrSearchService implements SearchService {

    private static final String SOLR_BASE = "/solr";
    private static final String SOLR_CONFIG = "/solr/conf/solrconfig.xml";
    private static final String SOLR_SCHEMA = "/solr/conf/schema.xml";

    private SolrServer solrServer;
    private Log log = LogFactory.getLog(getClass());

    public SolrSearchService(File indexPath) throws Exception {
        Validate.notNull(indexPath, "Index path may not be null");

        solrServer = createSolrServer(indexPath);
    }

    private SolrServer createSolrServer(File indexPath) throws ParserConfigurationException, IOException, SAXException {
        SolrConfig config = new SolrConfig(null, null, getClass().getResourceAsStream(SOLR_CONFIG));
        IndexSchema schema = new IndexSchema(config, null, getClass().getResourceAsStream(SOLR_SCHEMA));

        CoreContainer coreContainer = new CoreContainer();

        SolrCore core = new SolrCore("EMS", indexPath.getAbsolutePath(), config, schema, new CoreDescriptor(coreContainer, "EMS", SOLR_BASE));
        coreContainer.register("EMS", core,  false);
        return new EmbeddedSolrServer(coreContainer, "EMS");
    }

    // -----------------------------------------------------------------------
    // Search Service Implementation
    // -----------------------------------------------------------------------

    public IndexStatistics getIndexStatistics() {
        return new IndexStatistics();
    }

    public void update(Object entity) {
        if (!(entity instanceof AbstractEntity)) {
            return;
        }

        if (!(entity instanceof Session)) {
            return;
        }

        try {
            solrServer.deleteById(((Session) entity).getId());
            solrServer.add(createIndexDocument((Session) entity));
            solrServer.commit();
        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(Object entity) {
        if (!(entity instanceof AbstractEntity)) {
            return;
        }

        try {
            solrServer.deleteById(((AbstractEntity) entity).getId());
            solrServer.commit();
        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SearchResponse search(SearchRequest request) {
        SolrQuery query = new SolrQuery();
        query.setRows(request.getLimit());
        query.setStart(request.getOffset() * request.getLimit());
        BooleanQuery aggregate = new BooleanQuery();
        if (!StringUtils.isBlank(request.getText())) {
            TermQuery termQuery = new TermQuery(new Term("", request.getText()));
            aggregate.add(termQuery, BooleanClause.Occur.SHOULD);
        }
        if (!StringUtils.isBlank(request.getEventId())) {
            aggregate.add(new TermQuery(new Term("eventid", request.getEventId())), BooleanClause.Occur.MUST);
        }
        query.setQuery(aggregate.toString());       

        log.info("QUERY IS: " + query.toString());
        try {
            QueryResponse queryResponse = solrServer.query(query);
            log.info("RESPONSE FROM QUERY WAS: " + queryResponse);
            SolrDocumentList results = queryResponse.getResults();
            log.info("RESULTS WAS: " + results);
            ArrayList<SearchResponse.Hit> hits = new ArrayList<SearchResponse.Hit>();
            for (SolrDocument result : results) {
                hits.add(new SearchResponse.Hit(ObjectType.session, String.valueOf(result.getFieldValue("id")), null));
            }
            return new SearchResponse(results.getNumFound(), queryResponse.getElapsedTime(), hits);
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private SolrInputDocument createIndexDocument(Person resource) {
        Map<String, Object> additionalIndexedProperties = new HashMap<String, Object>();
        additionalIndexedProperties.put("description", resource.getDescription());
        additionalIndexedProperties.put("gender", resource.getGender());
        additionalIndexedProperties.put("language", resource.getLanguage());
        additionalIndexedProperties.put("nationality", resource.getNationality());
        additionalIndexedProperties.put("emailaddresses", resource.getEmailAddresses());
        ResourceToSolrTranslator translator = new ResourceToSolrTranslator();
        translator.add(resource, additionalIndexedProperties);
        return translator.getInputDocument();
    }

    private SolrInputDocument createIndexDocument(Event resource) {
        ResourceToSolrTranslator translator = new ResourceToSolrTranslator();
        translator.add(resource, null);
        return translator.getInputDocument();
    }

    private SolrInputDocument createIndexDocument(Session resource) {
        Map<String, Object> additionalIndexedFields = new HashMap<String, Object>();
        additionalIndexedFields.put("eventid", resource.getEventId());
        additionalIndexedFields.put("slot", resource.getTimeslot());
        additionalIndexedFields.put("state", resource.getState());
        additionalIndexedFields.put("format", resource.getFormat());
        additionalIndexedFields.put("room", resource.getRoom());
        additionalIndexedFields.put("level", resource.getLevel());
        additionalIndexedFields.put("title", resource.getTitle());
        additionalIndexedFields.put("lead_text", resource.getLead());
        additionalIndexedFields.put("body_text", resource.getBody());
        additionalIndexedFields.put("language", resource.getLanguage());
        additionalIndexedFields.put("speakers", resource.getSpeakers());
        additionalIndexedFields.put("published", resource.isPublished());
        ResourceToSolrTranslator translator = new ResourceToSolrTranslator();
        translator.add(resource, additionalIndexedFields);
        return translator.getInputDocument();
    }
}
