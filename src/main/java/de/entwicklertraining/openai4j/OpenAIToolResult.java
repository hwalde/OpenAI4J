package de.entwicklertraining.openai4j;

/**
 * Result returned by a {@link OpenAIToolsCallback}.  The content is typically
 * fed back to the model as the tool's response.
 *
 * @param content textual result that will be inserted into the assistant
 *                message returned to the chat completion API
 */
public record OpenAIToolResult(String content) {

    /**
     * Convenience factory method mirroring the canonical constructor.
     *
     * @param content textual result of the tool execution
     * @return a new {@code OpenAIToolResult}
     */
    public static OpenAIToolResult of(String content) {
        return new OpenAIToolResult(content);
    }
}
