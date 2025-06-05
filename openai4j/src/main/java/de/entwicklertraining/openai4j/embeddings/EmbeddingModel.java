package de.entwicklertraining.openai4j.embeddings;

/**
 * Supported embedding models.  The enum names must exactly match the
 * identifiers expected by the OpenAI API.
 */
public enum EmbeddingModel {

    /** Newest small embedding model */
    TEXT_EMBEDDING_3_SMALL("text-embedding-3-small"),
    /** Larger variant with higher quality */
    TEXT_EMBEDDING_3_LARGE("text-embedding-3-large"),
    /** Legacy ada model */
    TEXT_EMBEDDING_ADA_002("text-embedding-ada-002");

    private final String value;

    EmbeddingModel(String value) {
        this.value = value;
    }

    /**
     * Returns the literal model identifier used by the API.
     *
     * @return API model name
     */
    public String value() {
        return value;
    }
}
