package net.microfalx.heimdall.llm.api;

/**
 * Represents a dense vector embedding of a text.
 */
public interface Embedding {

    /**
     * Returns the model used for embedding.
     *
     * @return a non-null instance
     */
    Model getModel();

    /**
     * Returns the vector.
     *
     * @return the vector.
     */
    float[] getVector();

    /**
     * Returns the dimension of the vector.
     *
     * @return a positive integer representing the dimension of the vector.
     */
    int getDimension();

    /**
     * Normalizes the vector.
     */
    void normalize();

    /**
     * A factory interface used to create embedding marker.
     */
    interface Factory {

        /**
         * Creates an embedding for the given model.
         *
         * @param model the model to use for embedding
         * @return the maker instance
         */
        Embedding createEmbedding(Model model, String text);
    }

}
