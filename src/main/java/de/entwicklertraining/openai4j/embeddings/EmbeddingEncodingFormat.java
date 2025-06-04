package de.entwicklertraining.openai4j.embeddings;

/**
 * Encoding options for the embedding vector returned by the API.
 * <ul>
 *   <li>{@code float} – array of 32&#8209;bit floating point numbers</li>
 *   <li>{@code base64} – the same vector encoded as a Base64 string</li>
 * </ul>
 */
public enum EmbeddingEncodingFormat {
    /** floating point vector */
    FLOAT("float"),
    /** base64 encoded vector */
    BASE64("base64");

    private final String value;

    EmbeddingEncodingFormat(String value) {
        this.value = value;
    }

    /**
     * Returns the string literal used in API requests.
     *
     * @return API literal for this encoding
     */
    public String value() {
        return value;
    }
}
