package de.entwicklertraining.openai4j.chat.completion;

import java.util.Objects;
import org.json.JSONObject;

/**
 * Represents the parameters for the "prediction" object in an OpenAI Chat Completions request.
 * Used to provide predicted output to potentially speed up responses when parts of the
 * response are known beforehand.
 *
 * Note: The exact structure of the prediction object might need verification against
 * the latest OpenAI documentation for "Predicted Outputs". This implementation assumes
 * a simple structure with a "content" field based on the general description.
 *
 * See: https://platform.openai.com/docs/guides/predicted-outputs
 */
public final class GptChatCompletionPredictionParams {

    private final String predictedContent;

    /**
     * Creates prediction parameters.
     *
     * @param predictedContent The predicted content string. Must not be null.
     */
    public GptChatCompletionPredictionParams(String predictedContent) {
        this.predictedContent = Objects.requireNonNull(predictedContent, "predictedContent cannot be null");
        // Add validation if there are constraints on the content (e.g., length)
    }

    /**
     * Gets the predicted content string.
     *
     * @return The predicted content.
     */
    public String getPredictedContent() {
        return predictedContent;
    }

    /**
     * Converts this object to its JSON representation for the API request.
     * The current implementation follows the examples in the official
     * documentation and sends a single {@code content} field.  This may need
     * revisiting if the API evolves.
     *
     * @return A JSONObject representing the prediction parameters.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("content", predictedContent);
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GptChatCompletionPredictionParams that = (GptChatCompletionPredictionParams) o;
        return Objects.equals(predictedContent, that.predictedContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(predictedContent);
    }

    @Override
    public String toString() {
        // Avoid potentially logging large predicted content directly
        return "GptChatCompletionPredictionParams{predictedContent present}";
    }
}