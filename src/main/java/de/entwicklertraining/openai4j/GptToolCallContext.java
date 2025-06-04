package de.entwicklertraining.openai4j;

import org.json.JSONObject;

/**
 * Context object passed to a {@link GptToolsCallback} when a tool is invoked by
 * the Chat Completions API.  It exposes the JSON arguments supplied by the
 * model so that the callback can process them.
 */
public record GptToolCallContext(
        /**
         * Raw JSON arguments of the tool call as provided by the model.
         * The structure corresponds to the schema defined in
         * {@link GptToolDefinition.Builder#parameter(String, GptJsonSchema, boolean)}.
         */
        JSONObject arguments) { }
