package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.openai4j.embeddings.*;

import java.util.List;

/**
 * Einfaches Beispiel: Ruft Embeddings für zwei Sätze ab
 * und zeigt Dimension, Token-Verbrauch und Cosine-Similarity.
 */
public class EmbeddingsExample {

    public static void main(String[] args) {
        String s1 = "OpenAI provides powerful language models.";
        String s2 = "Large language models are offered by OpenAI.";

        /* 1) Vektor holen */
        GptEmbeddingsResponse r = GptEmbeddingsRequest.builder()
                .model(EmbeddingModel.TEXT_EMBEDDING_3_SMALL)
                .addInput(s1)
                .addInput(s2)
                .execute();

        List<double[]> vecs = r.embeddingsFloat();
        System.out.printf("Erhaltene Vektoren: %d, Dimension: %d%n",
                vecs.size(), vecs.getFirst().length);
        System.out.printf("Prompt-Tokens: %d, Total: %d%n",
                r.promptTokens(), r.totalTokens());

        /* 2) Cosine-Similarity */
        double sim = new GptCosineSimilarity(EmbeddingModel.TEXT_EMBEDDING_3_SMALL)
                .similarity(s1, s2);
        System.out.printf("Cosine-Similarity: %.4f%n", sim);
    }
}
