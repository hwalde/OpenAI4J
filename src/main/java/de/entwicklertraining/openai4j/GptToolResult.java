package de.entwicklertraining.openai4j;

public record GptToolResult(String content) {
    public static GptToolResult of(String content) {
        return new GptToolResult(content);
    }
}
