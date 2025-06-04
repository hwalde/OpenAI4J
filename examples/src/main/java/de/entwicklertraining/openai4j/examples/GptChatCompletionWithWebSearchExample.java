package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.openai4j.GptClient;
import de.entwicklertraining.openai4j.chat.completion.GptChatCompletionRequest;
import de.entwicklertraining.openai4j.chat.completion.GptChatCompletionResponse;
import de.entwicklertraining.openai4j.chat.completion.GptWebSearchOptions;

/**
 * Demonstrates how to use the web_search_options parameter with Chat Completions.
 * This example configures web search options and sends a user query that requires up-to-date information.
 *
 * Note: Make sure the selected model supports web search (e.g., "gpt-4o-search-preview" or as per OpenAI docs).
 */
public class GptChatCompletionWithWebSearchExample {

    public static void main(String[] args) {
        // Configure web search options (e.g., set context size to LOW)
        GptWebSearchOptions webSearchOptions = GptWebSearchOptions.builder()
                .searchContextSize(GptWebSearchOptions.SearchContextSize.LOW)
                .userLocation(
                        GptWebSearchOptions.UserLocation.builder()
                                .approximate(
                                        GptWebSearchOptions.ApproximateLocation.builder().countryCode("DE").build()
                                )
                                .build()
                )
                .build();

        // Generate GptClient
        GptClient client = new GptClient();

        // Build and execute the chat completion request with web search enabled
        GptChatCompletionResponse response = client.chat().completion()
                .model("gpt-4o-search-preview") // Use a model that supports web search
                .addSystemMessage("You are a helpful assistant with access to web search.")
                .addUserMessage("Was sind die Top-Meldungen des Tages?")
                .webSearchOptions(webSearchOptions)
                .execute();

        // Print the assistant's response
        System.out.println("Assistant (with web search):");
        System.out.println(response.assistantMessage());
    }
}