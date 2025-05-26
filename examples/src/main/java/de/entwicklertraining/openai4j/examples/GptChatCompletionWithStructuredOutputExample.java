package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.openai4j.*;
import de.entwicklertraining.openai4j.chat.completion.GptChatCompletionCallHandler;
import de.entwicklertraining.openai4j.chat.completion.GptChatCompletionRequest;
import de.entwicklertraining.openai4j.chat.completion.GptChatCompletionResponse;

import java.util.List;

/**
 * A comprehensive example showcasing how to use the "response_format" feature (Structured Outputs)
 * to ensure that the model's output adheres to a specific JSON schema.
 * <p>
 * In this example, we:
 *  1) Define a simple JSON Schema (using {@link GptJsonSchema}) that expects an "event" object with:
 *     - name         (string, required)
 *     - date         (string, required)
 *     - participants (array of strings, required)
 *     This is typical if you want the model to produce structured data for a domain entity.
 *  2) Create a {@link GptResponseFormat} with type = "json_schema" and strict = true,
 *     plus a schema name required by OpenAI.
 *  3) Build a {@link GptChatCompletionRequest} that includes a user prompt, plus the "response_format" with the schema.
 *  4) Send the request using {@link GptChatCompletionCallHandler} to handle the conversation. (No tools in this example.)
 *  5) Inspect the final {@link GptChatCompletionResponse} to see if it has a "parsed" JSON object that we can use.
 *  6) If it has a "refusal" or other edge cases, handle accordingly.
 *
 * This demonstration does NOT define or use any "functions" (tools). It's purely focusing on
 * the "Structured Outputs" aspect (a.k.a. "response_format").
 *
 * Note: Make sure the selected model supports "json_schema" for response_format
 *       (e.g. "gpt-4o-mini-2024-07-18", "gpt-4o-2024-08-06", etc.).
 */
public class GptChatCompletionWithStructuredOutputExample {

    public record Event(String date, String name, List<String> participants) {}

    public static void main(String[] args) throws Exception {
        // 1) Construct our JSON Schema for an "event" object.
        //    We want name, date, participants[] (all required, no additional props).

        // Removed explicit Builder usage and the final ".build()" call
        GptJsonSchema eventSchema = GptJsonSchema.objectSchema()
                .property("name", GptJsonSchema.stringSchema("The name of the event."), true)
                .property("date", GptJsonSchema.stringSchema("The date of the event, e.g. '2024-12-28'"), true)
                .property("participants", GptJsonSchema.arraySchema(
                        GptJsonSchema.stringSchema("A participant's name.")
                ), true)
                .additionalProperties(false);

        // 2) Create a strict "json_schema" response format with the above schema + a required schema name.
        GptResponseFormat responseFormat = GptResponseFormat.forJsonSchema(
                "event_schema", // the name of the schema
                eventSchema,
                true
        );

        // 3) Build the GptChatCompletionRequest and execute it:
        GptChatCompletionResponse finalResponse = GptChatCompletionRequest.builder()
                .model("gpt-4o-mini")  // or "gpt-4o-2024-08-06", etc.
                .responseFormat(responseFormat)
                .addSystemMessage("You are an assistant that outputs structured JSON for an event object.")
                .addUserMessage("Alice and Bob are attending a new-year party on next Monday. Please output the event data.")
                .execute();

        // 4) Check for refusal
        if (finalResponse.hasRefusal()) {
            System.out.println("The model refused to comply: " + finalResponse.refusal());
            return;
        }

        // 5) Lets print the result
        Event event = finalResponse.convertTo(Event.class);
        System.out.println(event);
    }
}
