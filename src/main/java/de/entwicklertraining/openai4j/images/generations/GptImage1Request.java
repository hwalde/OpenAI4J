package de.entwicklertraining.openai4j.images.generations;

import de.entwicklertraining.openai4j.GptClient;
import de.entwicklertraining.openai4j.GptRequest;
import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Encapsulates all parameters required for a GPT-Image-1 image generation request.
 * Follows the OpenAI API spec for gpt-image-1.
 */
public final class GptImage1Request extends GptRequest<GptImage1Response> {

    private final String prompt;
    private final int n;
    private final String user;
    private final ImageSize size;
    private final ImageQuality quality;
    private final OutputFormat outputFormat;
    private final Integer outputCompression;
    private final Background background;
    private final Moderation moderation;

    private GptImage1Request(
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
    public GptImage1Response createResponse(String responseBody) {
        return new GptImage1Response(responseBody, this);
    }

    public static Builder builder(GptClient client) {
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
     * Builder for GptImage1Request.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, GptImage1Request> {
        private final GptClient client;
        private String prompt;
        private int n = 1;
        private String user;
        private ImageSize size = ImageSize.AUTO;
        private ImageQuality quality = ImageQuality.AUTO;
        private OutputFormat outputFormat = OutputFormat.PNG;
        private Integer outputCompression = null;
        private Background background = Background.AUTO;
        private Moderation moderation = Moderation.AUTO;

        private Builder(GptClient client) {
            this.client = client;
        }

        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public Builder n(int n) {
            this.n = n;
            return this;
        }

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder size(ImageSize size) {
            this.size = size;
            return this;
        }

        public Builder quality(ImageQuality quality) {
            this.quality = quality;
            return this;
        }

        public Builder outputFormat(OutputFormat outputFormat) {
            this.outputFormat = outputFormat;
            return this;
        }

        public Builder outputCompression(Integer outputCompression) {
            this.outputCompression = outputCompression;
            return this;
        }

        public Builder background(Background background) {
            this.background = background;
            return this;
        }

        public Builder moderation(Moderation moderation) {
            this.moderation = moderation;
            return this;
        }

        public GptImage1Request build() {
            return new GptImage1Request(
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
        public GptImage1Response executeWithExponentialBackoff() {
            return client.sendRequest(build());
        }

        @Override
        public GptImage1Response execute() {
            return client.sendRequest(build());
        }
    }

    // Enums for gpt-image-1

    public enum ImageSize {
        SIZE_1024x1024("1024x1024"),
        SIZE_1536x1024("1536x1024"),
        SIZE_1024x1536("1024x1536"),
        AUTO("auto");

        private final String value;
        ImageSize(String value) {
            this.value = value;
        }
        public String value() {
            return value;
        }
    }

    public enum ImageQuality {
        HIGH("high"),
        MEDIUM("medium"),
        LOW("low"),
        AUTO("auto");

        private final String value;
        ImageQuality(String value) {
            this.value = value;
        }
        public String value() {
            return value;
        }
    }

    public enum OutputFormat {
        PNG("png"),
        JPEG("jpeg"),
        WEBP("webp");

        private final String value;
        OutputFormat(String value) {
            this.value = value;
        }
        public String value() {
            return value;
        }
    }

    public enum Background {
        TRANSPARENT("transparent"),
        OPAQUE("opaque"),
        AUTO("auto");

        private final String value;
        Background(String value) {
            this.value = value;
        }
        public String value() {
            return value;
        }
    }

    public enum Moderation {
        AUTO("auto"),
        LOW("low");

        private final String value;
        Moderation(String value) {
            this.value = value;
        }
        public String value() {
            return value;
        }
    }
}