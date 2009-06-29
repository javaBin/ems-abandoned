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

import no.java.ems.server.domain.Session;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LuceneSearchService implements SearchService {
    private Log log = LogFactory.getLog(getClass());

    private IndexReader indexReader;

    private Directory directory;

    public LuceneSearchService(File indexHome) throws IOException {

        boolean exist = indexHome.exists();
        directory = FSDirectory.getDirectory(indexHome);

        log.info("Storing indexes in " + indexHome.getAbsolutePath());

        // This is a bit odd but seems to be required to use an IndexReader on an empty index
        if(!exist){
            log.warn("Initializing index directory");
            new IndexWriter(indexHome, new StandardAnalyzer(), true).close();
        }

        indexReader = IndexReader.open(directory);
    }

    // -----------------------------------------------------------------------
    // SearchService Implementation
    // -----------------------------------------------------------------------

    public synchronized IndexStatistics getIndexStatistics() {
        IndexStatistics statistics = new IndexStatistics();
        statistics.numberOfDocuments = indexReader.numDocs();
        return statistics;
    }

    public synchronized void update(Object o) {
        EmsDocument emsDocument = createDocument(o);

        if (emsDocument == null) {
            return;
        }

        log.info("Indexing object: type=" + emsDocument.type + ", id=" + emsDocument.id + ".");

        // Unless all objects have an unique id this won't work.
        Term term = new Term("id", emsDocument.id);

        try {
            IndexReader indexReader = IndexReader.open(directory);
            indexReader.deleteDocuments(term);
            indexReader.close();

            IndexWriter indexWriter = new IndexWriter(directory, new StandardAnalyzer(), false);
            indexWriter.addDocument(emsDocument.document);
            indexWriter.close();

            reopen();
        } catch (IOException e) {
            throw new RuntimeException("Error while storing object. Type: " + emsDocument.type + ", " +
                "id: " + emsDocument.id + ".", e);
        }
    }

    public synchronized void delete(Object o) {
        EmsDocument emsDocument = createDocument(o);

        if (emsDocument == null) {
            return;
        }

        try {
            Term term = new Term("id", emsDocument.id);

            IndexReader indexReader = IndexReader.open(directory);
            indexReader.deleteDocuments(term);
            indexReader.close();

            reopen();
        } catch (IOException e) {
            throw new RuntimeException("Error while deleting object. Type: " + emsDocument.type + ", " +
                "id: " + emsDocument.id + ".", e);
        }
    }

    public synchronized SearchResponse search(SearchRequest request) {
        if (!StringUtils.isNotBlank(request.getText())) {
            throw new RuntimeException("At least one field in the search request has to be set.");
        }

        PhraseQuery titleQuery = new PhraseQuery();
        titleQuery.add(new Term("title", request.getText()));

        PhraseQuery leadQuery = new PhraseQuery();
        leadQuery.add(new Term("lead", request.getText()));

        PhraseQuery bodyQuery = new PhraseQuery();
        bodyQuery.add(new Term("body", request.getText()));

        TermQuery typeQuery = new TermQuery(new Term("type", ObjectType.session.name()));

        BooleanQuery booleanQuery = new BooleanQuery();

        BooleanQuery query = new BooleanQuery();
        query.add(titleQuery, BooleanClause.Occur.SHOULD);
        query.add(leadQuery, BooleanClause.Occur.SHOULD);
        query.add(bodyQuery, BooleanClause.Occur.SHOULD);

        booleanQuery.add(query, BooleanClause.Occur.MUST);
        booleanQuery.add(typeQuery, BooleanClause.Occur.MUST);

        if (StringUtils.isNotBlank(request.getEventId())) {
            booleanQuery.add(new TermQuery(new Term("eventId", request.getEventId())), BooleanClause.Occur.MUST);
        }

        log.info("Lucene query: " + query.toString());

        try {
            long executionTime = System.currentTimeMillis();

            Searcher searcher = new IndexSearcher(indexReader);

            Hits hits = searcher.search(query);

            ArrayList<SearchResponse.Hit> objectHits = new ArrayList<SearchResponse.Hit>();

            for (int i = 0; i < hits.length(); i++) {
                Document document = hits.doc(i);

                String s = Integer.toString(hits.id(i));
                objectHits.add(new SearchResponse.Hit(ObjectType.valueOf(document.get("type")), document.get("id"), s));
            }

            executionTime = System.currentTimeMillis() - executionTime;

            return new SearchResponse(hits.length(), executionTime, objectHits);
        } catch (IOException e) {
            throw new RuntimeException("Error performing search.", e);
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private void reopen() throws IOException {
        // Re-open the index reader
        IndexReader newIndexReader = IndexReader.open(directory);
        IndexReader oldIndexReader = this.indexReader;

        this.indexReader = newIndexReader;

        try {
            oldIndexReader.close();
        } catch (IOException e) {
            // ignore
        }
    }

    private EmsDocument createDocument(Object o) {
        EmsDocument emsDocument = null;

        if (o instanceof Session) {
            emsDocument = createDocument((Session) o);
        }
        return emsDocument;
    }

    private EmsDocument createDocument(Session session) {
        Document document = new Document();

        document.add(new Field("eventId", session.getEventId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field("title", session.getTitle(), Field.Store.NO, Field.Index.TOKENIZED));
        if (StringUtils.isNotBlank(session.getLead())) {
            document.add(new Field("lead", session.getLead(), Field.Store.NO, Field.Index.TOKENIZED));
        }
        if (StringUtils.isNotBlank(session.getBody())) {
            document.add(new Field("body", session.getBody(), Field.Store.NO, Field.Index.TOKENIZED));
        }

        return new EmsDocument(ObjectType.session, session.getId(), document);
    }

    private static class EmsDocument {
        ObjectType type;
        String id;
        Document document;

        private EmsDocument(ObjectType type, String id, Document document) {
            this.type = type;
            this.id = id;
            this.document = document;

            document.add(new Field("type", type.name(), Field.Store.YES, Field.Index.UN_TOKENIZED));
            document.add(new Field("id", id, Field.Store.YES, Field.Index.UN_TOKENIZED));
        }
    }
}
