package de.entwicklertraining.openai4j.embeddings;

/**
 * Unterstützte Embedding-Modelle.
 *
 * Achtung – die Namen müssen exakt den Open-AI-Bezeichnungen entsprechen.
 */
public enum EmbeddingModel {

    TEXT_EMBEDDING_3_SMALL("text-embedding-3-small"),
    TEXT_EMBEDDING_3_LARGE("text-embedding-3-large"),
    TEXT_EMBEDDING_ADA_002("text-embedding-ada-002");

    private final String value;
    EmbeddingModel(String value) { this.value = value; }

    public String value() { return value; }
}
