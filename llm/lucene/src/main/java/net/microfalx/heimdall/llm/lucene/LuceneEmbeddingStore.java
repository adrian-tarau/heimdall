package net.microfalx.heimdall.llm.lucene;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import net.microfalx.bootstrap.search.Document;
import net.microfalx.bootstrap.search.*;
import net.microfalx.threadpool.ThreadPool;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static dev.langchain4j.internal.Utils.randomUUID;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.isNotEmpty;

public class LuceneEmbeddingStore implements IndexListener, EmbeddingStore<TextSegment> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneEmbeddingStore.class);

    private final IndexService indexService;
    private final SearchService searchService;
    private final File directory;
    private Encoding encoding;

    private Indexer indexer;
    private ThreadPool threadPool;

    public LuceneEmbeddingStore(IndexService indexService, SearchService searchService, File directory) {
        requireNonNull(indexService);
        requireNonNull(searchService);
        requireNonNull(directory);
        this.indexService = indexService;
        this.searchService = searchService;
        this.directory = directory;
        initIndex();
        initEncodings();
    }

    public IndexService getIndexService() {
        return indexService;
    }

    public SearchService getSearchService() {
        return searchService;
    }

    public ThreadPool getThreadPool() {
        return threadPool != null ? threadPool : indexService.getThreadPool();
    }

    public LuceneEmbeddingStore setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
        return this;
    }

    public File getDirectory() {
        return directory;
    }

    @Override
    public void indexed(Collection<Document> documents) {
        requireNonNull(documents);
    }

    @Override
    public String add(Embedding embedding) {
        requireNonNull(embedding);
        String id = randomUUID();
        add(id, embedding, null);
        return id;
    }

    @Override
    public void add(String id, Embedding embedding) {
        requireNotEmpty(id);
        requireNonNull(embedding);
        add(id, embedding, null);
    }

    @Override
    public String add(Embedding embedding, TextSegment textSegment) {
        requireNonNull(embedding);
        requireNonNull(textSegment);
        String id = UUID.randomUUID().toString();
        add(id, embedding, null);
        return id;
    }

    public void add(String id, Embedding embedding, TextSegment content) {
        addAll(Collections.singletonList(id), Collections.singletonList(embedding), Collections.singletonList(content));
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
    }

    @Override
    public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request) {
        return null;
    }

    public void close() {
        if (indexer != null) indexer.release();
    }

    private void initIndex() {
        IndexerOptions options = IndexerOptions.create().setAnalyzer(new StandardAnalyzer());
        indexer = indexService.createIndexer(directory, options);
    }

    private void initEncodings() {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        encoding = registry.getEncoding(EncodingType.CL100K_BASE);
    }

    /**
     * Convert provided id, embedding and text to a Lucene document.
     *
     * @param id        the document id, can be null
     * @param embedding the embedding, can be null
     * @param content   the text content, can be null
     * @return Lucene document
     */
    private org.apache.lucene.document.Document toDocument(String id, Embedding embedding, TextSegment content) {
        requireNonNull(id);
        String text = content != null ? content.text() : null;
        int tokens = encoding.countTokens(text);
        org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();
        document.add(new StringField(LuceneFields.ID_FIELD_NAME, id, Field.Store.YES));
        if (!isNotEmpty(text)) {
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
