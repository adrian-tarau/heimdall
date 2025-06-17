package net.microfalx.heimdall.llm.lucene;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.AccessLevel;
import lombok.Getter;
import net.microfalx.bootstrap.search.*;
import net.microfalx.bootstrap.search.Document;
import net.microfalx.heimdall.llm.api.LlmService;
import net.microfalx.metrics.Metrics;
import net.microfalx.threadpool.ThreadPool;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static dev.langchain4j.internal.Utils.randomUUID;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.isNotEmpty;

public class LuceneEmbeddingStore implements IndexListener, EmbeddingStore<TextSegment> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneEmbeddingStore.class);

    static Metrics INDEX_METRICS = Metrics.of("Lucene Embedding");
    static Metrics SEARCH_METRICS = Metrics.of("Lucene Embedding");

    @Getter(AccessLevel.PROTECTED)
    private final LlmService llmService;
    @Getter(AccessLevel.PROTECTED)
    private final IndexService indexService;
    @Getter(AccessLevel.PROTECTED)
    private final SearchService searchService;
    private Encoding encoding;

    private Indexer indexer;
    private ThreadPool threadPool;
    private boolean enabled = false;

    private volatile LuceneContentRetriever contentRetriever;

    public LuceneEmbeddingStore(LlmService llmService, IndexService indexService, SearchService searchService) {
        requireNonNull(indexService);
        requireNonNull(indexService);
        requireNonNull(searchService);
        this.llmService = llmService;
        this.indexService = indexService;
        this.searchService = searchService;
        initIndex();
        initEncodings();
    }

    public ThreadPool getThreadPool() {
        return threadPool != null ? threadPool : indexService.getThreadPool();
    }

    public Indexer getIndexer() {
        return indexer;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LuceneEmbeddingStore setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
        return this;
    }

    public LuceneEmbeddingStore setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public void afterIndexing(Collection<Document> documents) {
        requireNonNull(documents);
        if (!enabled) return;
        for (Document document : documents) {
            getThreadPool().execute(() -> index(document));
        }
    }

    public void index(Document document) {
        AtomicReference<Embedding> embedding = new AtomicReference<>();
        AtomicReference<TextSegment> content = new AtomicReference<>();
        INDEX_METRICS.time("Extract", (t) -> {
            TextExtractor textExtractor = new TextExtractor(getIndexService().getContentService(), document);
            try {
                content.set(TextSegment.from(textExtractor.execute()));
                net.microfalx.heimdall.llm.api.Embedding embed = llmService.embed(content.get().text());
                embedding.set(new Embedding(embed.getVector()));
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to create embedding from document {}", document.getId());
            }
        });
        if (embedding.get() != null && content.get() != null) {
            INDEX_METRICS.time("Index", (t) -> {
                try {
                    add(document.getId(), embedding.get(), content.get(), document.getBodyUri());
                } catch (Exception e) {
                    LOGGER.atError().setCause(e).log("Failed to index embedding for {}", document.getId());
                }
            });
        }
    }

    @Override
    public String add(Embedding embedding) {
        requireNonNull(embedding);
        String id = randomUUID();
        add(id, embedding, null, null);
        return id;
    }

    @Override
    public void add(String id, Embedding embedding) {
        requireNotEmpty(id);
        requireNonNull(embedding);
        add(id, embedding, null, null);
    }

    @Override
    public String add(Embedding embedding, TextSegment textSegment) {
        requireNonNull(embedding);
        requireNonNull(textSegment);
        String id = UUID.randomUUID().toString();
        add(id, embedding, null, null);
        return id;
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings) {
        List<String> ids = embeddings.stream().map(embedding -> randomUUID()).toList();
        addAll(ids, embeddings, null);
        return ids;
    }

    @Override
    public void addAll(List<String> ids, List<Embedding> embeddings, List<TextSegment> embedded) {
        requireNonNull(ids);
        requireNonNull(embeddings);
        requireNonNull(embedded);
        if (ids.size() != embeddings.size() || ids.size() != embedded.size()) {
            throw new IllegalArgumentException("All lists must have the same size");
        }
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            Embedding embedding = embeddings.get(i);
            TextSegment content = embedded.get(i);
            requireNotEmpty(id);
            requireNonNull(embedding);
            add(id, embedding, content, null);
        }
    }

    public void add(String id, Embedding embedding, TextSegment content, URI contentUri) {
        org.apache.lucene.document.Document document = toDocument(id, embedding, content, contentUri);
        indexer.doWithIndex("Add Embedding", index -> {
            index.addDocument(document);
            return null;
        });
    }

    @Override
    public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request) {
        requireNonNull(request);
        LuceneContentRetriever.queryParams.set(new LuceneContentRetriever.QueryParams()
                .setMaxResults(request.maxResults()).setMinScore(request.minScore())
                .setMaxResults(request.maxResults()).setFilter(request.filter()));
        LuceneContentRetriever contentRetriever = getContentRetriever();
        LOGGER.debug("Search using, request filter {}", request.filter());
        List<EmbeddingMatch<TextSegment>> results = new ArrayList<>();
        List<Content> contents = contentRetriever.retrieve(Query.from(LuceneContent.DUMMY_TEXT), request.queryEmbedding());
        for (Content content : contents) {
            try {
                Map<ContentMetadata, Object> metadata = content.metadata();
                Double score;
                if (metadata != null && metadata.containsKey(ContentMetadata.SCORE)) {
                    score = (Double) metadata.get(ContentMetadata.SCORE);
                } else {
                    score = Double.NaN;
                }
                TextSegment textSegment = content.textSegment();
                String id;
                if (textSegment != null && textSegment.metadata() != null) {
                    id = textSegment.metadata().getString(LuceneFields.ID_FIELD_NAME);
                } else {
                    LOGGER.debug("Generating new random id");
                    id = randomUUID();
                }
                Embedding embedding = null;
                if (content instanceof LuceneContent luceneContent) {
                    embedding = luceneContent.getEmbedding();
                }
                EmbeddingMatch<TextSegment> result = new EmbeddingMatch<>(score, id, embedding, textSegment);
                results.add(result);
            } catch (Exception e) {
                LOGGER.error("Could not convert content to results", e);
            }
        }
        return new EmbeddingSearchResult<>(results);
    }

    public void close() {
        if (indexer != null) indexer.release();
    }

    private void initIndex() {
        IndexerOptions options = (IndexerOptions) IndexerOptions.create(LuceneFields.INDEX_NAME)
                .analyzer(new StandardAnalyzer()).metrics(INDEX_METRICS)
                .tag("embedding").tag("lucene")
                .name("Embedding").description("An index for storing embeddings and their associated text segments")
                .build();
        indexer = indexService.createIndexer(options);
    }

    private void initEncodings() {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        encoding = registry.getEncoding(EncodingType.CL100K_BASE);
    }

    public LuceneContentRetriever getContentRetriever() {
        if (contentRetriever == null) {
            synchronized (this) {
                contentRetriever = new LuceneContentRetriever(this);
            }
        }
        return contentRetriever;
    }

    private org.apache.lucene.document.Document toDocument(String id, Embedding embedding, TextSegment content, URI contentUri) {
        requireNonNull(id);
        String text = content != null ? content.text() : null;
        int tokens = encoding.countTokens(text);
        org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();
        document.add(new StringField(LuceneFields.ID_FIELD_NAME, id, Field.Store.YES));
        if (contentUri != null) {
            document.add(new StringField(LuceneFields.CONTENT_URI_FIELD_NAME, contentUri.toString(), Field.Store.YES));
        } else if (isNotEmpty(text)) {
            document.add(new TextField(LuceneFields.CONTENT_FIELD_NAME, text, Field.Store.YES));
        }
        if (embedding != null) {
            float[] vector = embedding.vector();
            if (vector != null && vector.length > 0) {
                document.add(new KnnFloatVectorField(LuceneFields.EMBEDDING_FIELD_NAME, vector));
            }
        }
        document.add(new IntField(LuceneFields.TOKEN_COUNT_FIELD_NAME, tokens, Field.Store.YES));
        if (content != null) {
            Map<String, Object> metadataMap = content.metadata().toMap();
            if (metadataMap != null) {
                for (Map.Entry<String, Object> entry : metadataMap.entrySet()) {
                    document.add(toField(entry));
                }
            }
        }
        return document;
    }

    private Field toField(Map.Entry<String, Object> entry) {
        String fieldName = entry.getKey();
        var fieldValue = entry.getValue();
        Field field;
        if (fieldValue instanceof String string) {
            field = new StringField(fieldName, string, Field.Store.YES);
        } else if (fieldValue instanceof Integer number) {
            field = new IntField(fieldName, number, Field.Store.YES);
        } else if (fieldValue instanceof Long number) {
            field = new LongField(fieldName, number, Field.Store.YES);
        } else if (fieldValue instanceof Float number) {
            field = new FloatField(fieldName, number, Field.Store.YES);
        } else if (fieldValue instanceof Double number) {
            field = new DoubleField(fieldName, number, Field.Store.YES);
        } else {
            field = new StringField(fieldName, String.valueOf(fieldValue), Field.Store.YES);
        }
        return field;
    }
}
