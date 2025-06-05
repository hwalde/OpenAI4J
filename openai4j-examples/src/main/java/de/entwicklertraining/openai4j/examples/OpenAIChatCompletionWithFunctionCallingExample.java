package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.openai4j.*;
import de.entwicklertraining.openai4j.chat.completion.OpenAIChatCompletionRequest;
import de.entwicklertraining.openai4j.chat.completion.OpenAIChatCompletionResponse;

public class OpenAIChatCompletionWithFunctionCallingExample {
    public static void main(String[] args) throws Exception {
        // Define functions with a fluent builder
        OpenAIToolDefinition weatherFunction = OpenAIToolDefinition.builder("get_local_weather")
                .description("Get weather information for a location.")
                // Removed the ".build()" call on OpenAIJsonSchema
                .parameter("location", OpenAIJsonSchema.stringSchema("Name of the city"), true)
                .callback(context -> {
                    String location = context.arguments().getString("location");
                    // Implement your logic to get weather here
                    String weatherResult = "Sunny in " + location + " with a high of 25°C.";
                    return OpenAIToolResult.of(weatherResult);
                })
                .build();

        OpenAIToolDefinition timeFunction = OpenAIToolDefinition.builder("get_current_time")
                .description("Get the current local time in a given city.")
                // Removed the ".build()" call on OpenAIJsonSchema
                .parameter("location", OpenAIJsonSchema.stringSchema("Name of the city"), true)
                .callback(context -> {
                    String location = context.arguments().getString("location");
                    // Implement your logic to get current time here
                    String currentTime = "The current time in " + location + " is 14:20:00 Uhr.";
                    return OpenAIToolResult.of(currentTime);
                })
                .build();

        // Generate OpenAIClient
        OpenAIClient client = new OpenAIClient();

        // Build the request and execute it
        OpenAIChatCompletionResponse finalResponse = client.chat().completion()
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
