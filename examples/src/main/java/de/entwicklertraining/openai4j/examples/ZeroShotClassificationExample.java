package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.openai4j.OpenAIClient;
import de.entwicklertraining.openai4j.embeddings.OpenAICosineSimilarity;

import java.util.List;

/**
 * Demonstriert Zero-Shot-Klassifikation mit Embeddings
 * unter Verwendung von OpenAICosineSimilarity.
 *
 * Für jede Klasse (Label) und jeden Text ruft es similarity(text, label) auf
 * und ordnet den Text der Klasse mit dem höchsten Score zu.
 */
public final class ZeroShotClassificationExample {

    public static void main(String[] args) {
        List<String> labels = List.of("negative", "positive");
        List<String> texts  = List.of(
            "I absolutely loved this product, it exceeded my expectations!",
            "The shipment arrived broken and the support was useless.",
            "Works fine, nothing special but does the job.",
            "Terrible experience, would not recommend."
        );

        OpenAIClient client = new OpenAIClient();
        OpenAICosineSimilarity similarity = new OpenAICosineSimilarity(client); // cached embeddings

        for (String text : texts) {
            String bestLabel = null;
            double bestScore = -Double.MAX_VALUE;

            for (String label : labels) {
                double score = similarity.similarity(text, label);
                if (score > bestScore) {
                    bestScore = score;
                    bestLabel = label;
                }
            }

            System.out.printf(
                "Text: \"%s\"%n➜ Predicted sentiment: %s (score %.4f)%n%n",
                shorten(text, 60), bestLabel, bestScore
            );
        }
    }

    private static String shorten(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
