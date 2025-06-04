package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.openai4j.GptClient;
import de.entwicklertraining.openai4j.chat.completion.GptChatCompletionResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates how to use multiple optional parameters together with the Chat Completion endpoint.
 * This example configures a representative subset of options, including temperature, top_p,
 * frequency_penalty, presence_penalty, logit_bias, stop sequences, max_completion_tokens, n, and user.
 *
 * Each option is commented to clarify its purpose.
 */
public class GptChatCompletionWithOptionsExample {

    public static void main(String[] args) {
        // Example logit bias: discourage the token ID for "cat" (-100), encourage "dog" (+50)
        // (Token IDs are model-specific; these are just illustrative.)
        Map<Integer, Integer> logitBias = new HashMap<>();
        logitBias.put(1234, -100); // Strongly discourage token 1234
        logitBias.put(5678, 50);   // Encourage token 5678

        // Generate GptClient
        GptClient client = new GptClient();

        // Build and execute the chat completion request with several options
        GptChatCompletionResponse response = client.chat().completion()
                .model("gpt-4o-mini")
                .addSystemMessage("You are a creative assistant.")
                .addUserMessage("Suggest a name for a new eco-friendly tech startup.")
                // Sampling temperature: higher = more random, lower = more deterministic
                .temperature(0.7)
                // Nucleus sampling: alternative to temperature, controls diversity (0-1)
                .topP(0.85)
                // Penalize repeated tokens (frequency_penalty: -2.0 to 2.0)
                .frequencyPenalty(0.5)
                // Penalize repeated topics (presence_penalty: -2.0 to 2.0)
                .presencePenalty(0.3)
                // Bias specific tokens (token IDs) in the output
                .logitBias(logitBias)
                // Stop generation at any of these sequences
                .stopSequences(List.of("\n", "###"))
                // Limit the total number of tokens in the completion (output + reasoning)
                .maxCompletionTokens(64)
                // Generate 2 completions (n=2)
                .n(2)
                // Pass a user identifier for abuse monitoring
                .user("example-user-123")
                .execute();

        // Print all completions
        System.out.println("Completions:");
        response.getChoices().forEach(choice ->
                System.out.println("- " + choice.getMessage().getContent())
        );
    }
}