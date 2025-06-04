package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.openai4j.OpenAIClient;
import de.entwicklertraining.openai4j.chat.completion.OpenAIChatCompletionRequest;
import de.entwicklertraining.openai4j.chat.completion.OpenAIChatCompletionResponse;
import de.entwicklertraining.openai4j.chat.completion.OpenAIWebSearchOptions;

/**
 * Demonstrates how to use the web_search_options parameter with Chat Completions.
 * This example configures web search options and sends a user query that requires up-to-date information.
 *
 * Note: Make sure the selected model supports web search (e.g., "gpt-4o-search-preview" or as per OpenAI docs).
 */
public class OpenAIChatCompletionWithWebSearchExample {

    public static void main(String[] args) {
        // Configure web search options (e.g., set context size to LOW)
        OpenAIWebSearchOptions webSearchOptions = OpenAIWebSearchOptions.builder()
                .searchContextSize(OpenAIWebSearchOptions.SearchContextSize.LOW)
                .userLocation(
                        OpenAIWebSearchOptions.UserLocation.builder()
                                .approximate(
                                        OpenAIWebSearchOptions.ApproximateLocation.builder().countryCode("DE").build()
                                )
                                .build()
                )
                .build();

        // Generate OpenAIClient
        OpenAIClient client = new OpenAIClient();

        // Build and execute the chat completion request with web search enabled
        OpenAIChatCompletionResponse response = client.chat().completion()
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