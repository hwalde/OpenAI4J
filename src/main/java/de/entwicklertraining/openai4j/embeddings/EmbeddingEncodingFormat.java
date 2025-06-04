package de.entwicklertraining.openai4j.embeddings;

/**
 * Encoding options for the embedding vector returned by the API.
 * <ul>
 *   <li>{@code float} – array of 32&#8209;bit floating point numbers</li>
 *   <li>{@code base64} – the same vector encoded as a Base64 string</li>
 * </ul>
 */
public enum EmbeddingEncodingFormat {
    FLOAT("float"),
    BASE64("base64");

    private final String value;

    EmbeddingEncodingFormat(String value) {
        this.value = value;
    }

    /**
     * Returns the string literal used in API requests.
     */
    public String value() {
        return value;
    }
}
