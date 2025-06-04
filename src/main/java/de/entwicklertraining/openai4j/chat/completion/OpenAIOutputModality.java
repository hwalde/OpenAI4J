package de.entwicklertraining.openai4j.chat.completion;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents the possible output modalities for a chat completion request.
 *
 * @see <a href="https://platform.openai.com/docs/api-reference/chat/create#chat-create-modalities">OpenAI API Reference</a>
 */
public enum OpenAIOutputModality {
    /**
     * Text output modality. This is the default.
     */
    TEXT("text"),

    /**
     * Audio output modality. Requires the 'audio' parameter to be set.
     */
    AUDIO("audio");

    private final String value;

    OpenAIOutputModality(String value) {
        this.value = value;
    }

    @JsonValue
    /**
     * Returns the literal value to include in the request JSON.
     */
    public String getValue() {
        return value;
    }
}