package net.microfalx.heimdall.llm.lucene;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.search.Document;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.Indexer;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.bootstrap.test.AbstractBootstrapServiceTestCase;
import net.microfalx.heimdall.llm.api.LlmService;
import net.microfalx.lang.JvmUtils;
import net.microfalx.resource.Resource;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ContentService.class, IndexService.class, SearchService.class})
class LuceneEmbeddingStoreTest extends AbstractBootstrapServiceTestCase {

    private static final float[] EMBEDDING1 = {0.1f, 0.2f, 0.3f, 0.4f, 0.5f};
    private static final float[] EMBEDDING2 = {0.5f, 0.4f, 0.3f, 0.2f, 0.1f};

    @MockitoBean(answers = Answers.RETURNS_SMART_NULLS)
    private LlmService llmService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private IndexService indexService;

    @Autowired
    private SearchService searchService;

    private LuceneEmbeddingStore embeddingStore;
    private File directory;


    @BeforeEach
    void setup() {
        directory = JvmUtils.getTemporaryDirectory("lucene-embedding", "index");
        embeddingStore = new LuceneEmbeddingStore(llmService, indexService, searchService, directory);
        net.microfalx.heimdall.llm.api.Embedding embeddings = createEmbeddings();
        when(llmService.embed(anyString())).thenReturn(embeddings);
    }

    @AfterEach
    void destroy() throws IOException {
        if (directory != null && directory.exists()) FileUtils.deleteDirectory(directory);
    }

    @Test
    void initialize() {
        assertNotNull(embeddingStore.getLlmService());
    }

    @Test
    void addText() {
        embeddingStore.add(new Embedding(EMBEDDING1), TextSegment.textSegment("This is a test text to be indexed"));
        assertAtLeastOneDocumentIndexed();
    }

    @Test
    void addDocument() throws IOException {
        embeddingStore.index(createDocument("doc1"));
        assertAtLeastOneDocumentIndexed();
    }

    @Test
    void search() throws IOException {
        embeddingStore.index(createDocument("doc1"));
        embeddingStore.getIndexer().commit();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(new Embedding(EMBEDDING1))
                .build();
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
        assertThat(result.matches().size()).isGreaterThan(0);
    }

    private void assertAtLeastOneDocumentIndexed() {
        Indexer indexer = embeddingStore.getIndexer();
        assertThat(indexer.getDocumentCount() + indexer.getPendingDocumentCount()).isGreaterThan(0);
    }

    private Document createDocument(String id) throws IOException {
        Document document = Document.create(id);
        document.setBody(Resource.temporary("embedding", null).copyFrom(Resource.text("This is a test text to be indexed")));
        return document;
    }

    private net.microfalx.heimdall.llm.api.Embedding createEmbeddings() {
        net.microfalx.heimdall.llm.api.Embedding embedding = Mockito.mock(net.microfalx.heimdall.llm.api.Embedding.class);
        when(embedding.getVector()).thenReturn(EMBEDDING1);
        when(embedding.getDimension()).thenReturn(EMBEDDING1.length);
        return embedding;
    }

}