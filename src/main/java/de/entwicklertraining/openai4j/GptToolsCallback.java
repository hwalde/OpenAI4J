package de.entwicklertraining.openai4j;

@FunctionalInterface
public interface GptToolsCallback {
    GptToolResult handle(GptToolCallContext context);
}
