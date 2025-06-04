package de.entwicklertraining.openai4j;

/**
 * Result returned by a {@link GptToolsCallback}.  The content is typically
 * fed back to the model as the tool's response.
 *
 * @param content textual result that will be inserted into the assistant
 *                message returned to the chat completion API
 */
public record GptToolResult(String content) {

    /**
     * Convenience factory method mirroring the canonical constructor.
     *
     * @param content textual result of the tool execution
     * @return a new {@code GptToolResult}
     */
    public static GptToolResult of(String content) {
        return new GptToolResult(content);
    }
}
