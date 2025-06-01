package net.microfalx.heimdall.llm.lucene;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.AccessLevel;
import lombok.Setter;
import net.microfalx.bootstrap.search.Searcher;
import net.microfalx.bootstrap.search.SearcherOptions;
import net.microfalx.heimdall.llm.api.LlmException;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.UriUtils;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.KnnFloatVectorField;
import org.apache.lucene.document.StoredValue;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.microfalx.bootstrap.search.SearchUtils.SEARCH_METRICS;
import static net.microfalx.heimdall.llm.lucene.LuceneFields.CONTENT_FIELD_NAME;
import static net.microfalx.heimdall.llm.lucene.LuceneFields.TOKEN_COUNT_FIELD_NAME;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isEmpty;
import static org.apache.lucene.search.Sort.RELEVANCE;

class LuceneContentRetriever implements ContentRetriever {

    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneContentRetriever.class);

    private final LuceneEmbeddingStore embeddingStore;

    private Searcher searcher;
    private final Object lock = new Object();

    static ThreadLocal<QueryParams> queryParams = ThreadLocal.withInitial(QueryParams::new);

    public LuceneContentRetriever(LuceneEmbeddingStore embeddingStore) {
        requireNonNull(embeddingStore);
        this.embeddingStore = embeddingStore;
    }

    @Override
    public List<Content> retrieve(Query query) {
        requireNonNull(query);
        return retrieve(query, null);
    }

    public List<Content> retrieve(Query query, Embedding embedding) {
        requireNonNull(query);
        String queryText = query.text();
        org.apache.lucene.search.Query luceneQuery = buildQuery(queryText, embedding);
        Searcher searcher = getSearcher(false);
        TopFieldDocs topDocs = searcher.doWithSearcher("Query", s -> s.search(luceneQuery, queryParams.get().maxResults, RELEVANCE, true));
        return extractContent(topDocs);
    }

    private List<Content> extractContent(TopFieldDocs topDocs) {
        return getSearcher(false).doWithSearcher("Extract", indexSearcher -> {
            List<Content> hits = new ArrayList<>();
            int docCount = 0;
            int tokenCount = 0;
            double minScore = queryParams.get().minScore;
            int maxResults = queryParams.get().maxResults;
            int maxTokens = queryParams.get().maxTokens;
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                if (scoreDoc.score < minScore) continue;
                Document document = searcher.getDocument(scoreDoc.doc);
                String contentUri = document.get(LuceneFields.CONTENT_URI_FIELD_NAME);
                String content = null;
                if (contentUri != null) {
                    Resource resource = ResourceFactory.resolve(UriUtils.parseUri(contentUri));
                    try {
                        content = resource.loadAsString();
                    } catch (IOException e) {
                        throw new LlmException("Failed to load content from resource: " + contentUri, e);
                    }
                } else {
                    content = document.get(CONTENT_FIELD_NAME);
                }
                if (isEmpty(content)) continue;
                if (docCount++ > maxResults) break;
                // Check token count
                IndexableField tokenCountField = document.getField(TOKEN_COUNT_FIELD_NAME);
                if (tokenCountField != null) {
                    int docTokens = tokenCountField.numericValue().intValue();
                    // There may be smaller documents to come after this that we can accommodate
                    if (tokenCount + docTokens > maxTokens) continue;
                    tokenCount = tokenCount + docTokens;
                }
                Embedding embedding = null;
                IndexableField embeddingField = document.getField(LuceneFields.EMBEDDING_FIELD_NAME);
                if (embeddingField instanceof KnnFloatVectorField vectorField) {
                    embedding = new Embedding(vectorField.vectorValue());
                }
                // Add all other document fields to metadata
                Metadata metadata = createTextSegmentMetadata(document);
                // Finally, add text segment to the list
                TextSegment textSegment = TextSegment.from(content, metadata);
                hits.add(new LuceneContent(textSegment, withScore(scoreDoc)).setEmbedding(embedding));
            }
            return hits;
        });
    }

    @SuppressWarnings("StringEquality")
    private org.apache.lucene.search.Query buildQuery(String query, Embedding embedding) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        if (query != null && query != LuceneContent.DUMMY_TEXT) {
            try {
                QueryParser parser = new QueryParser(CONTENT_FIELD_NAME, new StandardAnalyzer());
                org.apache.lucene.search.Query fullTextQuery = parser.parse(query);
                builder.add(fullTextQuery, BooleanClause.Occur.SHOULD);
            } catch (ParseException e) {
                LOGGER.warn(String.format("Could not create query <%s>", query), e);
            }
        }
        if (embedding != null && embedding.vector().length > 0) {
            final org.apache.lucene.search.Query vectorQuery = new KnnFloatVectorQuery(LuceneFields.EMBEDDING_FIELD_NAME,
                    embedding.vector(), queryParams.get().maxResults);
            builder.add(vectorQuery, BooleanClause.Occur.SHOULD);
        }
        if (!queryParams.get().onlyMatches) {
            builder.add(new MatchAllDocsQuery(), BooleanClause.Occur.SHOULD);
        }
        return builder.build();
    }

    private Metadata createTextSegmentMetadata(Document document) {
        Metadata metadata = new Metadata();
        for (IndexableField field : document) {
            String fieldName = field.name();
            // skip our standard fields
            if (StringUtils.containsInArray(fieldName, LuceneFields.FIELDS)) continue;
            StoredValue storedValue = field.storedValue();
            StoredValue.Type type = storedValue.getType();
            switch (type) {
                case INTEGER:
                    metadata.put(fieldName, storedValue.getIntValue());
                    break;
                case LONG:
                    metadata.put(fieldName, storedValue.getLongValue());
                    break;
                case FLOAT:
                    metadata.put(fieldName, storedValue.getFloatValue());
                    break;
                case DOUBLE:
                    metadata.put(fieldName, storedValue.getDoubleValue());
                    break;
                case STRING:
                    metadata.put(fieldName, storedValue.getStringValue());
                    break;
                default:
                    // No-op
            }
        }
        return metadata;
    }

    private Embedding embedQuery(String queryText) {
        return new Embedding(embeddingStore.getLlmService().embed(queryText).getVector());
    }

    private Map<ContentMetadata, Object> withScore(ScoreDoc scoreDoc) {
        Map<ContentMetadata, Object> contentMetadata = new HashMap<>();
        contentMetadata.put(ContentMetadata.SCORE, (double) scoreDoc.score);
        return contentMetadata;
    }

    static class QueryParams {
        @Setter(AccessLevel.PROTECTED)
        private boolean onlyMatches = true;
        @Setter(AccessLevel.PROTECTED)
        private int maxResults = 10;
        @Setter(AccessLevel.PROTECTED)
        private int maxTokens = Integer.MAX_VALUE;
        @Setter(AccessLevel.PROTECTED)
        private double minScore = 0;
        @Setter(AccessLevel.PROTECTED)
        private Filter filter;
    }

    private Searcher getSearcher(boolean reopen) {
        synchronized (lock) {
            reopen = reopen || (searcher != null && (searcher.isStale() || !searcher.isOpen()));
            if (searcher == null || reopen) {
                if (searcher != null) releaseSearcher();
                LOGGER.debug("Open searcher");
                SearcherOptions options = SearcherOptions.builder()
                        .directory(embeddingStore.getDirectory()).threadPool(embeddingStore.getThreadPool())
                        .analyzer(new StandardAnalyzer()).metrics(LuceneEmbeddingStore.SEARCH_METRICS)
                        .build();
                searcher = embeddingStore.getSearchService().createSearcher(options);
            }
            return searcher;
        }
    }

    private void releaseSearcher() {
        LOGGER.debug("Release searcher");
        synchronized (lock) {
            if (searcher != null) {
                SEARCH_METRICS.time("Release", (t) -> searcher.release());
                searcher = null;
            }
        }
    }
}
