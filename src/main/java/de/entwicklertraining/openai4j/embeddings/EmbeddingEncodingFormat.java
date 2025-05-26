package de.entwicklertraining.openai4j.embeddings;

/**
 * Mögliche Kodierungen für den Rückgabewert.
 * „float“ liefert ein Array aus 32-Bit-Gleitkommazahlen,
 * „base64“ liefert den gleichen Vektor als Base-64-String.
 */
public enum EmbeddingEncodingFormat {
    FLOAT("float"),
    BASE64("base64");

    private final String value;
    EmbeddingEncodingFormat(String value) { this.value = value; }

    public String value() { return value; }
}
