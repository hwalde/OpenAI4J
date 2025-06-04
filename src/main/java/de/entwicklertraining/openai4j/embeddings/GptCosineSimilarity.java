package de.entwicklertraining.openai4j.embeddings;

import de.entwicklertraining.openai4j.GptClient;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Convenience helper to compute the cosine similarity between two texts using
 * OpenAI embeddings.  Embedding vectors are cached so repeated calls for the
 * same text avoid additional API requests.
 */
public final class GptCosineSimilarity {

    private final EmbeddingModel              model;
    private final Map<String,double[]>        cache = new ConcurrentHashMap<>();
    private final GptClient client;

    /**
     * Creates an instance using the default embedding model.
     */
    public GptCosineSimilarity(GptClient client) {
        this(client, EmbeddingModel.TEXT_EMBEDDING_3_SMALL);
    }
    /**
     * Creates an instance specifying the embedding model to use.
     */
    public GptCosineSimilarity(GptClient client, EmbeddingModel model) {
        this.client = client;
        this.model = model;
    }

    /**
     * Calculates the cosine similarity of the two supplied texts.
     */
    public double similarity(String a, String b) {
        double[] va = embeddingFor(a);
        double[] vb = embeddingFor(b);
        System.out.println(a);
        System.out.println(Arrays.toString(va));
        System.out.println(b);
        System.out.println(Arrays.toString(vb));
        return cosine(va, vb);
    }

    /* ---------- Helper methods ---------- */

    private double[] embeddingFor(String text) {
        return cache.computeIfAbsent(text, t -> {
            GptEmbeddingsResponse resp = client.embeddings()
                    .model(model)
                    .addInput(t)
                    .encodingFormat(EmbeddingEncodingFormat.FLOAT)
                    .execute();
            return resp.firstEmbedding();
        });
    }

    private static double cosine(double[] x, double[] y) {
        if (x.length != y.length) throw new IllegalArgumentException("Vector dimensions differ");
        double dot = 0.0, nx = 0.0, ny = 0.0;
        for (int i = 0; i < x.length; i++) {
            dot += x[i] * y[i];
            nx  += x[i] * x[i];
            ny  += y[i] * y[i];
        }
        return (nx == 0 || ny == 0) ? 0.0 : dot / (Math.sqrt(nx) * Math.sqrt(ny));
    }

    /* Optional: clear cache */
    public void clearCache() { cache.clear(); }

    /* Debug – current cache size */
    public int cacheSize() { return cache.size(); }
}
