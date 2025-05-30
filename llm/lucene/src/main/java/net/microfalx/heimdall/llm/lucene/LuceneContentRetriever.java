package net.microfalx.heimdall.llm.lucene;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import net.microfalx.bootstrap.search.Searcher;
import net.microfalx.bootstrap.search.SearcherOptions;
import net.microfalx.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredValue;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.microfalx.bootstrap.search.SearchUtils.SEARCH_METRICS;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseMessage;

class LuceneContentRetriever implements ContentRetriever {

    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneContentRetriever.class);

    private final LuceneEmbeddingStore embeddingStore;
    private boolean onlyMatches = true;
    private int maxResults = 10;
    private int maxTokens = Integer.MAX_VALUE;
    private float minScore = 0;
    private EmbeddingModel embeddingModel;
    private Searcher searcher;
    private final Object lock = new Object();

    public LuceneContentRetriever(LuceneEmbeddingStore embeddingStore) {
        requireNonNull(embeddingStore);
        this.embeddingStore = embeddingStore;
    }

    @Override
    public List<Content> retrieve(Query query) {
        return List.of();
    }

    private org.apache.lucene.search.Query buildQuery(String query, Embedding embedding) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        if (query != null && !query.isBlank()) {
            try {
                QueryParser parser = new QueryParser(LuceneFields.CONTENT_FIELD_NAME, new StandardAnalyzer());
                org.apache.lucene.search.Query fullTextQuery = parser.parse(query);
                builder.add(fullTextQuery, BooleanClause.Occur.SHOULD);
            } catch (ParseException e) {
                LOGGER.warn(String.format("Could not create query <%s>", query), e);
            }
        } else {
            LOGGER.debug("Query text not provided");
        }

        if (embedding != null && embedding.vector().length > 0) {
            final org.apache.lucene.search.Query vectorQuery = new KnnFloatVectorQuery(LuceneFields.EMBEDDING_FIELD_NAME, embedding.vector(), maxResults);
            builder.add(vectorQuery, BooleanClause.Occur.SHOULD);
        } else {
            LOGGER.debug("Query embedding vector not provided");
        }

        if (!onlyMatches) {
            builder.add(new MatchAllDocsQuery(), BooleanClause.Occur.SHOULD);
            LOGGER.debug("Returning all documents, not just matches");
        }

        BooleanQuery combinedQuery = builder.build();
        return combinedQuery;
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
        Embedding embedding = null;
        if (embeddingModel != null) {
            Response<Embedding> embeddingResponse = embeddingModel.embed(queryText);
            if (embeddingResponse != null) {
                embedding = embeddingResponse.content();
            }
        }
        return embedding;
    }

    private Map<ContentMetadata, Object> withScore(ScoreDoc scoreDoc) {
        Map<ContentMetadata, Object> contentMetadata = new HashMap<>();
        contentMetadata.put(ContentMetadata.SCORE, (double) scoreDoc.score);
        return contentMetadata;
    }

    private RetryTemplate createTemplate(String query) {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.registerListener(new RetryListener() {
            @Override
            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                LOGGER.info("Failed to search for '" + query + ", root cause: " + getRootCauseMessage(throwable));
                releaseSearchHolder();
            }
        });
        return retryTemplate;
    }

    private Searcher getSearcher(boolean reopen) {
        synchronized (lock) {
            reopen = reopen || (searcher != null && searcher.isStale());
            if (searcher == null || reopen) {
                if (searcher != null) releaseSearchHolder();
                SearcherOptions options = SearcherOptions.create().setThreadPool(embeddingStore.getThreadPool())
                        .setAnalyzer(new StandardAnalyzer());
                searcher = embeddingStore.getSearchService().createSearcher(embeddingStore.getDirectory(), options);
            }
            return searcher;
        }
    }

    private void releaseSearchHolder() {
        LOGGER.debug("Release searcher");
        synchronized (lock) {
            if (searcher != null) {
                SEARCH_METRICS.time("Release", (t) -> searcher.release());
                searcher = null;
            }
        }
    }
}
