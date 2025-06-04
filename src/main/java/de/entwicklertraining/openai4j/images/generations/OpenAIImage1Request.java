package de.entwicklertraining.openai4j.images.generations;

import de.entwicklertraining.openai4j.OpenAIClient;
import de.entwicklertraining.openai4j.OpenAIRequest;
import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Encapsulates all parameters required for a GPT-Image-1 image generation request.
 * Follows the OpenAI API spec for gpt-image-1.
 */
public final class OpenAIImage1Request extends OpenAIRequest<OpenAIImage1Response> {

    private final String prompt;
    private final int n;
    private final String user;
    private final ImageSize size;
    private final ImageQuality quality;
    private final OutputFormat outputFormat;
    private final Integer outputCompression;
    private final Background background;
    private final Moderation moderation;

    /**
     * Constructs a request for the gpt-image-1 generation endpoint.
     *
     * @param builder           originating builder instance
     * @param prompt            textual prompt for the image
     * @param n                 number of images to generate
     * @param user              optional user identifier forwarded to the API
     * @param size              requested image size
     * @param quality           desired quality level
     * @param outputFormat      image file format
     * @param outputCompression optional compression level for PNG/WebP
     * @param background        background transparency setting
     * @param moderation        moderation behaviour
     */
    private OpenAIImage1Request(
            Builder builder,
            String prompt,
            int n,
            String user,
            ImageSize size,
            ImageQuality quality,
            OutputFormat outputFormat,
            Integer outputCompression,
            Background background,
            Moderation moderation
    ) {
        super(builder);
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt must not be null or blank.");
        }
        this.prompt = prompt;
        this.n = n;
        this.user = user;
        this.size = Objects.requireNonNullElse(size, ImageSize.AUTO);
        this.quality = Objects.requireNonNullElse(quality, ImageQuality.AUTO);
        this.outputFormat = Objects.requireNonNullElse(outputFormat, OutputFormat.PNG);
        this.outputCompression = outputCompression;
        this.background = Objects.requireNonNullElse(background, Background.AUTO);
        this.moderation = Objects.requireNonNullElse(moderation, Moderation.AUTO);
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
        body.put("model", "gpt-image-1");
        body.put("prompt", prompt);
        body.put("n", n);
        body.put("size", size.value());
        body.put("quality", quality.value());
        body.put("output_format", outputFormat.value());
        if (outputCompression != null) {
            body.put("output_compression", outputCompression);
        }
        body.put("background", background.value());
        body.put("moderation", moderation.value());
        if (user != null && !user.isBlank()) {
            body.put("user", user);
        }
        return body.toString();
    }

    @Override
    public OpenAIImage1Response createResponse(String responseBody) {
        return new OpenAIImage1Response(responseBody, this);
    }

    /**
     * Returns a new builder for a GPT-Image‑1 request.
     *
     * @param client client used to execute the request
     * @return builder instance
     */
    public static Builder builder(OpenAIClient client) {
        return new Builder(client);
    }

    public String prompt() {
        return prompt;
    }

    public int n() {
        return n;
    }

    public String user() {
        return user;
    }

    public ImageSize size() {
        return size;
    }

    public ImageQuality quality() {
        return quality;
    }

    public OutputFormat outputFormat() {
        return outputFormat;
    }

    public Integer outputCompression() {
        return outputCompression;
    }

    public Background background() {
        return background;
    }

    public Moderation moderation() {
        return moderation;
    }

    /**
     * Builder for OpenAIImage1Request.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenAIImage1Request> {
        private final OpenAIClient client;
        private String prompt;
        private int n = 1;
        private String user;
        private ImageSize size = ImageSize.AUTO;
        private ImageQuality quality = ImageQuality.AUTO;
        private OutputFormat outputFormat = OutputFormat.PNG;
        private Integer outputCompression = null;
        private Background background = Background.AUTO;
        private Moderation moderation = Moderation.AUTO;

        /**
         * Creates a builder using the provided client. Builders are normally
         * created via {@link OpenAIClient#gptImage1Request()}.
         *
         * @param client the API client
         */
        /**
         * Creates a builder bound to the provided client.
         */
        private Builder(OpenAIClient client) {
            this.client = client;
        }

        /**
         * Sets the textual prompt for the image generation.
         */
        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        /**
         * Number of images to generate.
         */
        public Builder n(int n) {
            this.n = n;
            return this;
        }

        /**
         * Optional user identifier forwarded to the API.
         */
        public Builder user(String user) {
            this.user = user;
            return this;
        }

        /**
         * Sets the desired image size.
         */
        public Builder size(ImageSize size) {
            this.size = size;
            return this;
        }

        /**
         * Sets the requested quality level.
         */
        public Builder quality(ImageQuality quality) {
            this.quality = quality;
            return this;
        }

        /**
         * Sets the image format of the response.
         */
        public Builder outputFormat(OutputFormat outputFormat) {
            this.outputFormat = outputFormat;
            return this;
        }

        /**
         * Compression level for PNG/WebP outputs, if applicable.
         */
        public Builder outputCompression(Integer outputCompression) {
            this.outputCompression = outputCompression;
            return this;
        }

        /**
         * Selects the background transparency behaviour.
         */
        public Builder background(Background background) {
            this.background = background;
            return this;
        }

        /**
         * Sets how the request should handle content moderation.
         */
        public Builder moderation(Moderation moderation) {
            this.moderation = moderation;
            return this;
        }

        /**
         * Builds the request with the configured parameters.
         */
        public OpenAIImage1Request build() {
            return new OpenAIImage1Request(
                    this,
                    prompt,
                    n,
                    user,
                    size,
                    quality,
                    outputFormat,
                    outputCompression,
                    background,
                    moderation
            );
        }

        @Override
        /**
         * Executes the request with exponential backoff.
         */
        public OpenAIImage1Response executeWithExponentialBackoff() {
            return client.sendRequest(build());
        }

        @Override
        /**
         * Executes the request without retries.
         */
        public OpenAIImage1Response execute() {
            return client.sendRequest(build());
        }
    }

    // Enums for gpt-image-1

    /**
     * Available image dimensions for gpt-image-1.
     */
    public enum ImageSize {
        SIZE_1024x1024("1024x1024"),
        SIZE_1536x1024("1536x1024"),
        SIZE_1024x1536("1024x1536"),
        AUTO("auto");

        private final String value;
        ImageSize(String value) {
            this.value = value;
        }
        /**
         * String literal describing the requested size.
         */
        public String value() {
            return value;
        }
    }

    /**
     * Quality levels for the generated image.
     */
    public enum ImageQuality {
        HIGH("high"),
        MEDIUM("medium"),
        LOW("low"),
        AUTO("auto");

        private final String value;
        ImageQuality(String value) {
            this.value = value;
        }
        /**
         * Returns the literal used for image quality.
         */
        public String value() {
            return value;
        }
    }

    /**
     * File formats that can be returned by the API.
     */
    public enum OutputFormat {
        PNG("png"),
        JPEG("jpeg"),
        WEBP("webp");

        private final String value;
        OutputFormat(String value) {
            this.value = value;
        }
        /**
         * Returns the format identifier for generated images.
         */
        public String value() {
            return value;
        }
    }

    /**
     * Background options for generated images.
     */
    public enum Background {
        TRANSPARENT("transparent"),
        OPAQUE("opaque"),
        AUTO("auto");

        private final String value;
        Background(String value) {
            this.value = value;
        }
        /**
         * Literal controlling the image background type.
         */
        public String value() {
            return value;
        }
    }

    /**
     * Moderation settings applied to the request.
     */
    public enum Moderation {
        AUTO("auto"),
        LOW("low");

        private final String value;
        Moderation(String value) {
            this.value = value;
        }
        /**
         * Returns the moderation setting string.
         */
        public String value() {
            return value;
        }
    }
}