package de.entwicklertraining.openai4j;

import de.entwicklertraining.api.base.ApiRequest;
import de.entwicklertraining.api.base.ApiRequestBuilderBase;

/**
 * A generic request abstraction for OpenAI calls (chat completions, DALL·E, etc.).
 *
 * Subclasses must provide:
 *  - the target URI (e.g., https://api.openai.com/v1/chat/completions)
 *  - the HTTP method (POST, GET, ...)
 *  - the request body (JSON payload or null)
 *  - a factory method to create the corresponding GptResponse object
 * @param <T> The corresponding GptResponse type
 */
public abstract class GptRequest<T extends GptResponse<?>> extends ApiRequest<T> {

    protected <Y extends ApiRequestBuilderBase<?, ?>> GptRequest(Y builder) {
        super(builder);
    }

    @Override
    public boolean isBinaryResponse() {
        return false;
    }

    @Override
    public byte[] getBodyBytes() {
        throw new UnsupportedOperationException("This request does not have a body with bytes.");
    }

    @Override
    public String getContentType() {
        return "application/json";
    }
}
