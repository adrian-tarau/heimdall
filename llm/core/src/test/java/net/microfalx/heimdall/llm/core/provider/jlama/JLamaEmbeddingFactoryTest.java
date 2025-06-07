package net.microfalx.heimdall.llm.core.provider.jlama;

import net.microfalx.heimdall.llm.api.Embedding;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JLamaEmbeddingFactoryTest {

    private Provider provider;

    @BeforeEach
    void setup() throws Exception {
        provider = new JLamaProviderFactory().createProvider();
    }

    @Test
    void embedE5Base() {
        Embedding embedding = createEmbedding("jlama-e5-base-v2", "This is a test embedding");
        assertEquals(768, embedding.getDimension());
    }

    @Test
    void embedE5Small() {
        Embedding embedding = createEmbedding("jlama-e5-small-v2", "This is a test embedding");
        assertEquals(384, embedding.getDimension());
    }

    private Embedding createEmbedding(String modelId, String text) {
        JLamaEmbeddingFactory factory = new JLamaEmbeddingFactory();
        Model model = loadModel(modelId);
        return factory.createEmbedding(model, text);
    }

    private Model loadModel(String modelId) {
        return provider.getModel(modelId);
    }

}