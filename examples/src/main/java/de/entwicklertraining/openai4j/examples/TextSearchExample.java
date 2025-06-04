package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.openai4j.OpenAIClient;
import de.entwicklertraining.openai4j.embeddings.OpenAICosineSimilarity;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Demonstriert eine semantische Textsuche mit OpenAI-Embeddings
 * unter Verwendung von OpenAICosineSimilarity.
 *
 * 1. Wir haben ein paar Dokumente und eine Query als Strings.
 * 2. Für jedes Dokument rufen wir OpenAICosineSimilarity.similarity(doc, query) auf.
 * 3. Wir sortieren nach Score und geben die Top-3 Treffer aus.
 */
public final class TextSearchExample {

    public static void main(String[] args) {
        List<String> documents = List.of(
            "OpenAI develops powerful AI models.",
            "The Eiffel Tower is located in Paris.",
            "Large language models enable natural language understanding.",
            "Java 24 introduced virtual threads.",
            "Paris is the capital of France."
        );
        String query = "Where is the Eiffel Tower?";

        OpenAIClient client = new OpenAIClient();

        OpenAICosineSimilarity similarity = new OpenAICosineSimilarity(client); // Default: text-embedding-3-small

        var scored = documents.stream()
            .map(doc -> new Result(doc, similarity.similarity(doc, query)))
            .sorted(Comparator.comparingDouble(Result::score).reversed())
            //.limit(3)
            .collect(Collectors.toList());

        System.out.println("Top Treffer für Query: \"" + query + "\":");
        scored.forEach(r -> System.out.printf("  %.4f  %s%n", r.score(), r.text()));
    }

    private record Result(String text, double score) {}
}
