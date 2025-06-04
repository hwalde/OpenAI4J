package de.entwicklertraining.openai4j.audio.translations;

import de.entwicklertraining.openai4j.OpenAIClient;
import de.entwicklertraining.openai4j.OpenAIRequest;
import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.api.base.ApiResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a request to create a translation via POST https://api.openai.com/v1/audio/translations
 * <p>
 * The API expects multipart/form-data, including:
 *  - file              (required, the binary audio content)
 *  - model             (required, only "whisper-1" supported)
 *  - prompt            (optional, must be in English if used)
 *  - response_format   (optional, default = "json")
 *  - temperature       (optional, default = 0)
 *
 * The translated text (English) will be returned.
 */
public final class OpenAICreateTranslationRequest extends OpenAIRequest<OpenAICreateTranslationResponse> {
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "flac", "mp3", "mp4", "mpeg", "mpga", "m4a", "ogg", "wav", "webm"
    );

    private final Path file;
    private final TranslationModel model;
    private final String prompt;
    private final TranslationResponseFormat responseFormat;
    private final Double temperature;

    /**
     * We store the boundary for multipart.
     * We'll generate a raw binary multipart body instead of base64,
     * because the server rejects base64 for the file field.
     */
    private final String boundary = "----GPTTranslationBoundary" + UUID.randomUUID();

    OpenAICreateTranslationRequest(
            Builder builder,
            Path file,
            TranslationModel model,
            String prompt,
            TranslationResponseFormat responseFormat,
            Double temperature
    ) {
        super(builder);
        if (file == null) {
            throw new IllegalArgumentException("Audio file path must not be null.");
        }
        validateFileExtension(file);
        if (model == null) {
            throw new IllegalArgumentException("TranslationModel must not be null.");
        }

        this.file = file;
        this.model = model;
        this.prompt = prompt;
        this.responseFormat = (responseFormat != null) ? responseFormat : TranslationResponseFormat.JSON;
        this.temperature = (temperature != null) ? temperature : 0.0;
    }

    @Override
    public String getRelativeUrl() {
        return "/audio/translations";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * We do not use the old string-based method for multipart data.
     * It's kept here only because OpenAIRequest requires it.
     * Returning null ensures the caller won't actually use this string approach.
     */
    @Override
    public String getBody() {
        return null;
    }

    /**
     * Builds the multipart/form-data payload as a raw byte array,
     * referencing the raw bytes of the file for the "file" part.
     */
    @Override
    public byte[] getBodyBytes() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            byte[] fileBytes = Files.readAllBytes(file);

            // -- file part
            out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Disposition: form-data; name=\"file\"; filename=\""
                    + file.getFileName().toString() + "\"\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(fileBytes);
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));

            // -- model part
            out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            out.write("Content-Disposition: form-data; name=\"model\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            out.write(model.value().getBytes(StandardCharsets.UTF_8));
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));

            // -- prompt part (if set)
            if (prompt != null && !prompt.isBlank()) {
                out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
                out.write("Content-Disposition: form-data; name=\"prompt\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                out.write(prompt.getBytes(StandardCharsets.UTF_8));
                out.write("\r\n".getBytes(StandardCharsets.UTF_8));
            }

            // -- response_format part
            out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            out.write("Content-Disposition: form-data; name=\"response_format\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            out.write(responseFormat.value().getBytes(StandardCharsets.UTF_8));
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));

            // -- temperature part
            out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            out.write("Content-Disposition: form-data; name=\"temperature\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            out.write(temperature.toString().getBytes(StandardCharsets.UTF_8));
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));

            // -- closing boundary
            out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to build multipart body", e);
        }
    }

    /**
     * Instead of "multipart/form-data", we must include the boundary:
     */
    @Override
    public String getContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    @Override
    public OpenAICreateTranslationResponse createResponse(String responseBody) {
        return new OpenAICreateTranslationResponse(responseBody, this);
    }

    public static Builder builder(OpenAIClient client) {
        return new Builder(client);
    }

    private static void validateFileExtension(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase(Locale.ROOT);
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            throw new IllegalArgumentException(
                    "No file extension detected for: " + filePath);
        }
        String ext = fileName.substring(dotIndex + 1);
        if (!SUPPORTED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException(
                    "Unsupported audio file extension: " + ext
                            + ". Must be one of: " + SUPPORTED_EXTENSIONS
            );
        }
    }

    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenAICreateTranslationRequest>  {
        private final OpenAIClient client;
        private Path file;
        private TranslationModel model = TranslationModel.WHISPER_1; // default
        private String prompt;
        private TranslationResponseFormat responseFormat;
        private Double temperature;

        public Builder(OpenAIClient client) {
            this.client = client;
        }

        public Builder file(Path filePath) {
            this.file = filePath;
            return this;
        }

        public Builder model(TranslationModel model) {
            this.model = model;
            return this;
        }

        /**
         * Optional text to guide the style or continue a previous audio segment.
         * Must be in English.
         */
        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        /**
         * The format of the output, in one of: json, text, srt, verbose_json, or vtt.
         */
        public Builder responseFormat(TranslationResponseFormat format) {
            this.responseFormat = format;
            return this;
        }

        /**
         * The sampling temperature, between 0 and 1. Default is 0.
         */
        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public OpenAICreateTranslationRequest build() {
            return new OpenAICreateTranslationRequest(
                    this,
                    file,
                    model,
                    prompt,
                    responseFormat,
                    temperature
            );
        }

        @Override
        public OpenAICreateTranslationResponse executeWithExponentialBackoff() {
            return client.sendRequest(build());
        }

        @Override
        public OpenAICreateTranslationResponse execute() {
            return client.sendRequest(build());
        }
    }
}
