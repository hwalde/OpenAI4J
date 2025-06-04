package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.openai4j.*;
import de.entwicklertraining.openai4j.chat.completion.GptChatCompletionRequest;
import de.entwicklertraining.openai4j.chat.completion.GptChatCompletionResponse;

public class GptChatCompletionWithFunctionCallingExample {
    public static void main(String[] args) throws Exception {
        // Define functions with a fluent builder
        GptToolDefinition weatherFunction = GptToolDefinition.builder("get_local_weather")
                .description("Get weather information for a location.")
                // Removed the ".build()" call on GptJsonSchema
                .parameter("location", GptJsonSchema.stringSchema("Name of the city"), true)
                .callback(context -> {
                    String location = context.arguments().getString("location");
                    // Implement your logic to get weather here
                    String weatherResult = "Sunny in " + location + " with a high of 25°C.";
                    return GptToolResult.of(weatherResult);
                })
                .build();

        GptToolDefinition timeFunction = GptToolDefinition.builder("get_current_time")
                .description("Get the current local time in a given city.")
                // Removed the ".build()" call on GptJsonSchema
                .parameter("location", GptJsonSchema.stringSchema("Name of the city"), true)
                .callback(context -> {
                    String location = context.arguments().getString("location");
                    // Implement your logic to get current time here
                    String currentTime = "The current time in " + location + " is 14:20:00 Uhr.";
                    return GptToolResult.of(currentTime);
                })
                .build();

        // Generate GptClient
        GptClient client = new GptClient();

        // Build the request and execute it
        GptChatCompletionResponse finalResponse = client.chat().completion()
                .model("gpt-4o-mini")
                .addSystemMessage("You are a helpful assistant.")
                .addUserMessage("What's the weather in Berlin and also the current time in Berlin?")
                .addTool(weatherFunction)
                .addTool(timeFunction)
                .execute();

        // Print final assistant content
        System.out.println("Final Assistant Response: " + finalResponse.assistantMessage());
    }
}
