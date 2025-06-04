package de.entwicklertraining.openai4j;

/**
 * Callback used by {@link GptToolDefinition} to implement the actual logic of
 * a tool.  The callback receives the JSON arguments supplied by the model and
 * returns a textual result that is sent back to the assistant.
 */
@FunctionalInterface
public interface GptToolsCallback {

    /**
     * Handles a tool invocation.
     *
     * @param context contains the arguments passed by the model
     * @return the result of the tool execution
     */
    GptToolResult handle(GptToolCallContext context);
}
