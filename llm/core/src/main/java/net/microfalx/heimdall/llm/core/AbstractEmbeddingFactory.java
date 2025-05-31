package net.microfalx.heimdall.llm.core;

import net.microfalx.heimdall.llm.api.Embedding;
import net.microfalx.heimdall.llm.api.Model;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for embedding factories.
 */
public abstract class AbstractEmbeddingFactory implements Embedding.Factory {

    /**
     * Creates an embedding instance.
     *
     * @param model     the model to use for embedding
     * @param embedding the embedding data
     * @return a new instance
     */
    protected final Embedding create(Model model, dev.langchain4j.data.embedding.Embedding embedding) {
        requireNonNull(model);
        requireNonNull(embedding);
        return new EmbeddingImpl(model, embedding);
    }

    protected static class EmbeddingImpl implements Embedding {

        private final Model model;
        final dev.langchain4j.data.embedding.Embedding embedding;

        protected EmbeddingImpl(Model model, dev.langchain4j.data.embedding.Embedding embedding) {
            requireNonNull(model);
            requireNonNull(embedding);
            this.model = model;
            this.embedding = embedding;
        }

        @Override
        public Model getModel() {
            return model;
        }

        @Override
        public float[] getVector() {
            return embedding.vector();
        }

        @Override
        public int getDimension() {
            return embedding.dimension();
        }

        @Override
        public void normalize() {
            embedding.normalize();
        }

    }
}
