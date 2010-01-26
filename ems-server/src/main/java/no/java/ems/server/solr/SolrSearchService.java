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

package no.java.ems.server.solr;

import no.java.ems.server.URIBuilder;
import no.java.ems.server.domain.*;
import no.java.ems.server.search.SearchRequest;
import no.java.ems.server.search.SearchResponse;
import no.java.ems.server.search.SearchService;
import no.java.ems.server.solr.ResourceToSolrTranslator;
import org.apache.commons.lang.StringUtils;
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
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.schema.IndexSchema;
import org.xml.sax.SAXException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
@Component
public class SolrSearchService implements SearchService {

    private static final String SOLR_BASE = "/solr";
    private static final String SOLR_CONFIG = "/solr/conf/solrconfig.xml";
    private static final String SOLR_SCHEMA = "/solr/conf/schema.xml";

    private SolrServer solrServer;
    private Log log = LogFactory.getLog(getClass());

    @Autowired
    public SolrSearchService(EmsServerConfiguration configuration) throws Exception {
        solrServer = createSolrServer(configuration.getSolrHome());
    }

    private SolrServer createSolrServer(File solrHome) throws ParserConfigurationException, IOException, SAXException {
        SolrConfig config = new SolrConfig(null, null, getClass().getResourceAsStream(SOLR_CONFIG));
        IndexSchema schema = new IndexSchema(config, null, getClass().getResourceAsStream(SOLR_SCHEMA));

        CoreContainer coreContainer = new CoreContainer();

        SolrCore core = new SolrCore("EMS", solrHome.getAbsolutePath(), config, schema, new CoreDescriptor(coreContainer, "EMS", SOLR_BASE));
        coreContainer.register("EMS", core, false);
        return new EmbeddedSolrServer(coreContainer, "EMS");
    }

    // -----------------------------------------------------------------------
    // Search Service Implementation
    // -----------------------------------------------------------------------

    public IndexStatistics getIndexStatistics() {
        IndexStatistics stats = new IndexStatistics();
        try {
            QueryResponse docs = solrServer.query(new SolrQuery("*:*"));
            stats.numberOfDocuments = docs.getResults().getNumFound();
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public void update(Object entity) {
        if (!(entity instanceof AbstractEntity)) {
            return;
        }
        
        if (entity instanceof Session) {
            index((Session) entity);
        }
        else if (entity instanceof Person) {
            index((Person) entity);
        }
        else if (entity instanceof Event) {
            index((Event) entity);
        }
    }

    private void index(Session entity) {
        try {
            solrServer.deleteById(entity.getId());
            solrServer.add(createIndexDocument(entity));
            solrServer.commit();
        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void index(Person entity) {
        try {
            solrServer.deleteById(entity.getId());
            solrServer.add(createIndexDocument(entity));
            solrServer.commit();
        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void index(Event entity) {
        try {
            solrServer.deleteById(entity.getId());
            solrServer.add(createIndexDocument(entity));
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

    public SearchResponse search(SearchRequest request, URIBuilder uriBuilder) {
        SolrQuery query = new SolrQuery();
        query.setRows(request.getLimit());
        query.setStart(request.getOffset() * request.getLimit());
        BooleanQuery aggregate = new BooleanQuery();
        if (!StringUtils.isBlank(request.getText())) {
            TermQuery termQuery = new TermQuery(new Term("", request.getText()));
            aggregate.add(termQuery, BooleanClause.Occur.MUST);
        }
        if (!StringUtils.isBlank(request.getEventId())) {
            aggregate.add(new TermQuery(new Term("eventid", request.getEventId())), BooleanClause.Occur.MUST);
        }
        if (request.getObjectType() != null) {
            aggregate.add(new BooleanClause(new TermQuery(new Term("type", request.getObjectType().name())), BooleanClause.Occur.MUST));
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
                ObjectType type = extractType(result);
                String id = String.valueOf(result.getFieldValue("id"));
                URI uri;
                if (type == ObjectType.session) {
                    String eventId = String.valueOf(result.getFieldValue("eventid"));
                    uri = uriBuilder.forObject(eventId, id, type);
                }
                else {
                    uri = uriBuilder.forObject(null, id, type);
                }
                hits.add(new SearchResponse.Hit(type, uri, (String)result.getFieldValue("title")));
            }
            return new SearchResponse(results.getNumFound(), queryResponse.getElapsedTime(), hits);
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ObjectType extractType(SolrDocument result) {
        String typeValue = (String) result.getFieldValue("type");
        ObjectType type;
        if (!StringUtils.isBlank(typeValue)) {
            type = ObjectType.valueOf(typeValue);
        }
        else {
            type = null;
        }
        return type;
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
