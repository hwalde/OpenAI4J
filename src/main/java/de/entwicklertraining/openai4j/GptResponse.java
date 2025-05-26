package de.entwicklertraining.openai4j;

import de.entwicklertraining.api.base.ApiResponse;
import org.json.JSONObject;

/**
 * A generic response abstraction from OpenAI calls (chat completions, DALL·E, etc.).
 *
 * Typically, implementations will parse the JSON into more specific fields and
 * offer helper methods to extract data.
 */
public abstract class GptResponse<T extends GptRequest<?>> extends ApiResponse<T> {

    protected final JSONObject json;

    protected GptResponse(JSONObject json, T request) {
        super(request);
        this.json = json;
    }

    /**
     * Returns the raw JSON object of the response.
     */
    public JSONObject getJson() {
        return json;
    }
}
