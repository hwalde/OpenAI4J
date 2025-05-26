package de.entwicklertraining.openai4j;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents the reasoning effort constraint for o-series models in OpenAI Chat Completions.
 *
 * @see <a href="https://platform.openai.com/docs/guides/reasoning">OpenAI Reasoning Models</a>
 */
public enum GptReasoningEffort {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    private final String value;

    GptReasoningEffort(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}