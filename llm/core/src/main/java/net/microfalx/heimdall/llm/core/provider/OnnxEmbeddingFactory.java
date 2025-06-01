package net.microfalx.heimdall.llm.core.provider;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import net.microfalx.heimdall.llm.api.Embedding;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.core.AbstractEmbeddingFactory;

public class OnnxEmbeddingFactory extends AbstractEmbeddingFactory {

    @Override
    public Embedding createEmbedding(Model model, String text) {
        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        return create(model, embeddingModel.embed(text).content());
    }
}
