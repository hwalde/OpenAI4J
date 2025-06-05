package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.openai4j.*;
import de.entwicklertraining.openai4j.chat.completion.OpenAIChatCompletionCallHandler;
import de.entwicklertraining.openai4j.chat.completion.OpenAIChatCompletionRequest;
import de.entwicklertraining.openai4j.chat.completion.OpenAIChatCompletionResponse;

import java.util.List;

/**
 * A comprehensive example showcasing how to use the "response_format" feature (Structured Outputs)
 * to ensure that the model's output adheres to a specific JSON schema.
 * <p>
 * In this example, we:
 *  1) Define a simple JSON Schema (using {@link OpenAIJsonSchema}) that expects an "event" object with:
 *     - name         (string, required)
 *     - date         (string, required)
 *     - participants (array of strings, required)
 *     This is typical if you want the model to produce structured data for a domain entity.
 *  2) Create a {@link OpenAIResponseFormat} with type = "json_schema" and strict = true,
 *     plus a schema name required by OpenAI.
 *  3) Build a {@link OpenAIChatCompletionRequest} that includes a user prompt, plus the "response_format" with the schema.
 *  4) Send the request using {@link OpenAIChatCompletionCallHandler} to handle the conversation. (No tools in this example.)
 *  5) Inspect the final {@link OpenAIChatCompletionResponse} to see if it has a "parsed" JSON object that we can use.
 *  6) If it has a "refusal" or other edge cases, handle accordingly.
 *
 * This demonstration does NOT define or use any "functions" (tools). It's purely focusing on
 * the "Structured Outputs" aspect (a.k.a. "response_format").
 *
 * Note: Make sure the selected model supports "json_schema" for response_format
 *       (e.g. "gpt-4o-mini-2024-07-18", "gpt-4o-2024-08-06", etc.).
 */
public class OpenAIChatCompletionWithStructuredOutputExample {

    public record Event(String date, String name, List<String> participants) {}

    public static void main(String[] args) throws Exception {
        // 1) Construct our JSON Schema for an "event" object.
        //    We want name, date, participants[] (all required, no additional props).

        // Removed explicit Builder usage and the final ".build()" call
        OpenAIJsonSchema eventSchema = OpenAIJsonSchema.objectSchema()
                .property("name", OpenAIJsonSchema.stringSchema("The name of the event."), true)
                .property("date", OpenAIJsonSchema.stringSchema("The date of the event, e.g. '2024-12-28'"), true)
                .property("participants", OpenAIJsonSchema.arraySchema(
                        OpenAIJsonSchema.stringSchema("A participant's name.")
                ), true)
                .additionalProperties(false);

        // 2) Create a strict "json_schema" response format with the above schema + a required schema name.
        OpenAIResponseFormat responseFormat = OpenAIResponseFormat.forJsonSchema(
                "event_schema", // the name of the schema
                eventSchema,
                true
        );

        // 3) Generate OpenAIClient
        OpenAIClient client = new OpenAIClient();

        // 4) Build the OpenAIChatCompletionRequest and execute it:
        OpenAIChatCompletionResponse finalResponse = client.chat().completion()
                .model("gpt-4o-mini")  // or "gpt-4o-2024-08-06", etc.
                .responseFormat(responseFormat)
                .addSystemMessage("You are an assistant that outputs structured JSON for an event object.")
                .addUserMessage("Alice and Bob are attending a new-year party on next Monday. Please output the event data.")
                .execute();

        // 5) Check for refusal
        if (finalResponse.hasRefusal()) {
            System.out.println("The model refused to comply: " + finalResponse.refusal());
            return;
        }

        // 6) Lets print the result
        Event event = finalResponse.convertTo(Event.class);
        System.out.println(event);
    }
}
