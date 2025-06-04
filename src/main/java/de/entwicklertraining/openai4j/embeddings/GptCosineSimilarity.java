package de.entwicklertraining.openai4j.embeddings;

import de.entwicklertraining.openai4j.GptClient;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Liefert Cosine-Similarity zwischen zwei Texten, indem es Open-AI-Embeddings abruft
 * und anschließend lokal das Skalarprodukt normalisiert.
 *
 * Bei wiederholter Verwendung werden Embeddings gecacht, um Kosten zu sparen.
 */
public final class GptCosineSimilarity {

    private final EmbeddingModel              model;
    private final Map<String,double[]>        cache = new ConcurrentHashMap<>();
    private final GptClient client;

    public GptCosineSimilarity(GptClient client) {
        this(client, EmbeddingModel.TEXT_EMBEDDING_3_SMALL);
    }
    public GptCosineSimilarity(GptClient client, EmbeddingModel model) {
        this.client = client;
        this.model = model;
    }

    /** Cosine-Similarity zweier Texte. */
    public double similarity(String a, String b) {
        double[] va = embeddingFor(a);
        double[] vb = embeddingFor(b);
        System.out.println(a);
        System.out.println(Arrays.toString(va));
        System.out.println(b);
        System.out.println(Arrays.toString(vb));
        return cosine(va, vb);
    }

    /* ---------- Hilfsmethoden ---------- */

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

    /* Optional: Cache löschen */
    public void clearCache() { cache.clear(); }

    /* Debug – aktuell gecachte Einträge */
    public int cacheSize() { return cache.size(); }
}
