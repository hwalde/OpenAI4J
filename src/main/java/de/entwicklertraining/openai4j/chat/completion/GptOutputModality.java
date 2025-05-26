package de.entwicklertraining.openai4j.chat.completion;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents the possible output modalities for a chat completion request.
 *
 * @see <a href="https://platform.openai.com/docs/api-reference/chat/create#chat-create-modalities">OpenAI API Reference</a>
 */
public enum GptOutputModality {
    /**
     * Text output modality. This is the default.
     */
    TEXT("text"),

    /**
     * Audio output modality. Requires the 'audio' parameter to be set.
     */
    AUDIO("audio");

    private final String value;

    GptOutputModality(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}