package de.entwicklertraining.openai4j.images.generations;

import de.entwicklertraining.openai4j.OpenAIClient;
import de.entwicklertraining.openai4j.OpenAIRequest;
import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Encapsulates all parameters required for a DALL·E 3 image generation request
 *
 * DALL·E 3 specifics:
 *  - Supports sizes: 1024x1024, 1024x1792, 1792x1024
 *  - Usually limited to 1 image at a time (n=1).
 *  - Supports optional 'quality' and 'style'.
 *  - Optionally, can prepend a "noMoreDetail" note to the prompt.
 */
public final class DallE3Request extends OpenAIRequest<DallE3Response> {

    private final String prompt;
    private final ImageSize size;
    private final ResponseFormat responseFormat;
    private final int n; // typically 1 for DALL·E 3
    private final ImageQuality quality; // "standard" or "hd"
    private final ImageStyle style;     // "vivid" or "natural"
    private final boolean noMoreDetail; // if true -> special text is prepended to the prompt

    /**
     * Constructs a request for the DALL·E 3 image generation API.
     *
     * @param builder       originating builder
     * @param prompt        prompt text describing the desired image
     * @param size          size of the generated image
     * @param responseFormat result format (URL or base64)
     * @param n             number of images (typically 1)
     * @param quality       optional quality parameter
     * @param style         optional style parameter
     * @param noMoreDetail  if true, the prompt is prepended with a special string
     */
    private DallE3Request(
            Builder builder,
            String prompt,
            ImageSize size,
            ResponseFormat responseFormat,
            int n,
            ImageQuality quality,
            ImageStyle style,
            boolean noMoreDetail
    ) {
        super(builder);
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt must not be null or blank.");
        }
        if (n != 1) {
            // The documentation suggests that Dall-E 3 handles only 1 image per request for official usage.
            throw new IllegalArgumentException("DALL·E 3 can only generate 1 image per request. n=" + n);
        }
        this.prompt = prompt;
        this.size = Objects.requireNonNull(size, "ImageSize must not be null for Dall-E 3.");
        this.responseFormat = Objects.requireNonNull(responseFormat, "ResponseFormat must not be null.");
        this.n = n;
        this.quality = quality; // optional
        this.style = style;     // optional
        this.noMoreDetail = noMoreDetail;
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
        JSONObject body = new JSONObject();
        body.put("model", "dall-e-3");
        body.put("prompt", effectivePrompt());
        body.put("size", size.value());
        body.put("response_format", responseFormat.value());
        body.put("n", n);

        if (quality != null) {
            body.put("quality", quality.value());
        }
        if (style != null) {
            body.put("style", style.value());
        }
        return body.toString();
    }

    @Override
    public DallE3Response createResponse(String responseBody) {
        return new DallE3Response(responseBody, this);
    }

    /**
     * Final prompt that will be sent to the API.
     * If {@code noMoreDetail} is {@code true}, a note requesting no additional
     * detail is prepended.
     *
     * @return the prompt actually used for the request
     */
    public String effectivePrompt() {
        if (noMoreDetail) {
            return "I NEED to test how the tool works with extremely simple prompts. "
                    + "DO NOT add any detail, just use it AS-IS: " + prompt;
        }
        return prompt;
    }

    /**
     * Returns a new builder for a DALL·E 3 request.
     *
     * @param client API client used to execute the request
     * @return builder instance
     */
    public static Builder builder(OpenAIClient client) {
        return new Builder(client);
    }

    /**
     * Original prompt text supplied by the user.
     *
     * @return the prompt
     */
    public String prompt() {
        return prompt;
    }

    /**
     * Requested image size.
     *
     * @return the image size
     */
    public ImageSize size() {
        return size;
    }

    /**
     * Response format specifying whether URLs or base64 data are returned.
     *
     * @return the response format
     */
    public ResponseFormat responseFormat() {
        return responseFormat;
    }

    /**
     * Number of images requested from the API.
     *
     * @return the number of images
     */
    public int n() {
        return n;
    }

    /**
     * Optional quality setting for the generated image.
     *
     * @return the image quality or {@code null} if default
     */
    public ImageQuality quality() {
        return quality;
    }

    /**
     * Optional style hint used for image generation.
     *
     * @return the image style or {@code null} if not set
     */
    public ImageStyle style() {
        return style;
    }

    /**
     * Indicates whether a "no more detail" note is prepended to the prompt.
     *
     * @return {@code true} if no additional detail should be added
     */
    public boolean noMoreDetail() {
        return noMoreDetail;
    }

    /**
     * Builder class to configure and create a {@link DallE3Request}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, DallE3Request>  {
        private final OpenAIClient client;
        private String prompt;
        private ImageSize size = ImageSize.SIZE_1024x1024; // default
        private ResponseFormat responseFormat = ResponseFormat.URL; // default
        private int n = 1; // Dall-E 3 typically only supports 1
        private ImageQuality quality = null; // optional
        private ImageStyle style = null; // optional
        private boolean noMoreDetail = false;

        /**
         * Creates a builder bound to the given client. Usually obtained via
         * {@link OpenAIClient#dallE3Request()}.
         *
         * @param client API client to execute the request
         */
        /**
         * Creates a builder bound to the given client.
         */
        private Builder(OpenAIClient client) {
            this.client = client;
        }

        /**
         * Sets the prompt describing the desired image.
         *
         * @param prompt the image description
         * @return this builder instance
         */
        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        /**
         * Sets the image size to request.
         *
         * @param size the desired image size
         * @return this builder instance
         */
        public Builder size(ImageSize size) {
            this.size = size;
            return this;
        }

        /**
         * Sets the desired response format.
         *
         * @param rf the response format
         * @return this builder instance
         */
        public Builder responseFormat(ResponseFormat rf) {
            this.responseFormat = rf;
            return this;
        }

        /**
         * Number of images to generate. The API typically only allows {@code 1}.
         *
         * @param n number of images
         * @return this builder instance
         */
        public Builder n(int n) {
            this.n = n;
            return this;
        }

        /**
         * Optional image quality setting.
         *
         * @param quality desired image quality
         * @return this builder instance
         */
        public Builder quality(ImageQuality quality) {
            this.quality = quality;
            return this;
        }

        /**
         * Optional style hint for the image.
         *
         * @param style desired image style
         * @return this builder instance
         */
        public Builder style(ImageStyle style) {
            this.style = style;
            return this;
        }

        /**
         * If true, prepends a note to the prompt requesting no additional detail.
         *
         * @param val whether to add the no-more-detail flag
         * @return this builder instance
         */
        public Builder noMoreDetail(boolean val) {
            this.noMoreDetail = val;
            return this;
        }

        /**
         * Builds the request using the configured parameters.
         *
         * @return immutable request instance
         */
        public DallE3Request build() {
            return new DallE3Request(
                    this,
                    prompt,
                    size,
                    responseFormat,
                    n,
                    quality,
                    style,
                    noMoreDetail
            );
        }

        /**
         * Executes the request with exponential backoff on retryable errors.
         */
        @Override
        public DallE3Response executeWithExponentialBackoff() {
            return client.sendRequest(build());
        }

        /**
         * Executes the request without retries.
         */
        @Override
        public DallE3Response execute() {
            return client.sendRequest(build());
        }
    }

    // Enums for DALL·E 3

    /**
     * Supported image sizes for DALL·E 3.
     */
    public enum ImageSize {
        /** 1024x1024 pixels */
        SIZE_1024x1024("1024x1024"),
        /** 1024x1792 pixels */
        SIZE_1024x1792("1024x1792"),
        /** 1792x1024 pixels */
        SIZE_1792x1024("1792x1024");

        private final String value;
        ImageSize(String value) {
            this.value = value;
        }
        /**
         * Returns the literal size value used by the API.
         *
         * @return API string for this size
         */
        public String value() {
            return value;
        }
    }

    /**
     * Quality options for generated images.
     */
    public enum ImageQuality {
        /** Standard quality */
        STANDARD("standard"),
        /** High definition quality */
        HD("hd");

        private final String value;
        ImageQuality(String value) {
            this.value = value;
        }
        /**
         * API literal for image quality.
         *
         * @return string used by the API
         */
        public String value() {
            return value;
        }
    }

    /**
     * Style options controlling the visual appearance of the result.
     */
    public enum ImageStyle {
        /** Vivid, saturated colors */
        VIVID("vivid"),
        /** Natural, subdued colors */
        NATURAL("natural");

        private final String value;
        ImageStyle(String value) {
            this.value = value;
        }
        /**
         * API literal for image style.
         *
         * @return string used by the API
         */
        public String value() {
            return value;
        }
    }

    /**
     * Format of the API response payload.
     */
    public enum ResponseFormat {
        /** URLs pointing to the images */
        URL("url"),
        /** Base64-encoded image data */
        B64_JSON("b64_json");

        private final String value;
        ResponseFormat(String value) {
            this.value = value;
        }
        /**
         * String used to request the corresponding response format.
         *
         * @return API literal for this format
         */
        public String value() {
            return value;
        }
    }
}
