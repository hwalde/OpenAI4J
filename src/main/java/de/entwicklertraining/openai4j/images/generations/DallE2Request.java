package de.entwicklertraining.openai4j.images.generations;

import de.entwicklertraining.openai4j.GptClient;
import de.entwicklertraining.openai4j.GptRequest;
import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Encapsulates all parameters required for a DALL·E 2 image generation request.
 *
 * DALL·E 2 specifics:
 *  - Supports sizes: 256x256, 512x512, 1024x1024
 *  - Can generate up to 10 images in one request (n up to 10).
 *  - Doesn't support quality or style parameters.
 *
 * Now extends GptRequest<DallE2Response> directly, incorporating
 * the functionality that was previously in GptDallE2Request.
 */
public final class DallE2Request extends GptRequest<DallE2Response> {

    private final String prompt;
    private final ImageSize size;
    private final ResponseFormat responseFormat;
    private final int n; // up to 10 images

    private DallE2Request(
            Builder builder,
            String prompt,
            ImageSize size,
            ResponseFormat responseFormat,
            int n
    ) {
        super(builder);
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt must not be null or blank.");
        }
        if (n < 1 || n > 10) {
            throw new IllegalArgumentException("DALL·E 2 can generate 1 to 10 images in one request.");
        }
        this.prompt = prompt;
        this.size = Objects.requireNonNull(size, "ImageSize must not be null.");
        this.responseFormat = Objects.requireNonNull(responseFormat, "ResponseFormat must not be null.");
        this.n = n;
    }

    @Override
    public String getRelativeUrl() {
        return "/images/generations";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    @Override
    public String getBody() {
        // Build JSON body (moved here from the old GptDallE2Request)
        JSONObject body = new JSONObject();
        body.put("model", "dall-e-2");
        body.put("prompt", prompt);
        body.put("size", size.value());
        body.put("response_format", responseFormat.value());
        body.put("n", n);
        return body.toString();
    }

    @Override
    public DallE2Response createResponse(String responseBody) {
        // Now we create a DallE2Response (which extends GptResponse<DallE2Request>)
        return new DallE2Response(responseBody, this);
    }

    public String prompt() {
        return prompt;
    }

    public ImageSize size() {
        return size;
    }

    public ResponseFormat responseFormat() {
        return responseFormat;
    }

    public int n() {
        return n;
    }

    public static Builder builder(GptClient client) {
        return new Builder(client);
    }

    /**
     * Builder class to configure and create a {@link DallE2Request}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, DallE2Request> {
        private final GptClient client;
        private String prompt;
        private ImageSize size = ImageSize.SIZE_512x512;
        private ResponseFormat responseFormat = ResponseFormat.URL;
        private int n = 1;

        private Builder(GptClient client) {
            this.client = client;
        }

        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public Builder size(ImageSize size) {
            this.size = size;
            return this;
        }

        public Builder responseFormat(ResponseFormat rf) {
            this.responseFormat = rf;
            return this;
        }

        public Builder n(int n) {
            this.n = n;
            return this;
        }

        public DallE2Request build() {
            return new DallE2Request(this, prompt, size, responseFormat, n);
        }

        @Override
        public DallE2Response executeWithExponentialBackoff() {
            return client.sendRequest(build());
        }

        @Override
        public DallE2Response execute() {
            return client.sendRequest(build());
        }
    }

    /**
     * DALL·E 2 valid sizes.
     */
    public enum ImageSize {
        SIZE_256x256("256x256"),
        SIZE_512x512("512x512"),
        SIZE_1024x1024("1024x1024");

        private final String value;
        ImageSize(String value) {
            this.value = value;
        }
        public String value() {
            return value;
        }
    }

    public enum ResponseFormat {
        URL("url"),
        B64_JSON("b64_json");

        private final String value;
        ResponseFormat(String value) {
            this.value = value;
        }
        public String value() {
            return value;
        }
    }
}
