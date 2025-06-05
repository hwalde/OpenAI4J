package de.entwicklertraining.openai4j.audio.transcriptions;

import de.entwicklertraining.openai4j.OpenAIClient;
import de.entwicklertraining.openai4j.OpenAIRequest;
import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.api.base.ApiResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Represents a request to create a transcription via POST https://api.openai.com/v1/audio/transcriptions
 * <p>
 * The API expects multipart/form-data, including:
 *  - file              (required, the binary audio content)
 *  - model             (required, only "whisper-1" supported)
 *  - prompt            (optional)
 *  - language          (optional, ISO-639-1)
 *  - response_format   (optional, default = "json")
 *  - temperature       (optional, default = 0)
 *  - timestamp_granularities[] (optional, only valid if response_format=verbose_json)
 *
 * Usage example:
 * <pre>
 *   OpenAICreateTranscriptionResponse resp = OpenAICreateTranscriptionRequest.builder()
 *       .file(Path.of("my_audio.mp3"))
 *       .model(SpeechToTextModel.WHISPER_1)
 *       .language("en")
 *       .responseFormat(TranscriptionResponseFormat.VERBOSE_JSON)
 *       .timestampGranularities(Arrays.asList(TimestampGranularity.WORD, TimestampGranularity.SEGMENT))
 *       .build()
 *       .execute();
 *   String text = resp.text();
 * </pre>
 */
public final class OpenAICreateTranscriptionRequest extends OpenAIRequest<OpenAICreateTranscriptionResponse> {
    private final Path file;
    private final SpeechToTextModel model;
    private final String language;
    private final String prompt;
    private final TranscriptionResponseFormat responseFormat;
    private final Double temperature;
    private final List<TimestampGranularity> timestampGranularities;

    /**
     * We store the boundary for multipart.
     * We'll generate a raw binary multipart body instead of base64,
     * because the server rejects base64 for the file field.
     */
    private final String boundary = "----GPTTranscriptionBoundary" + UUID.randomUUID();

    OpenAICreateTranscriptionRequest(
            Builder builder,
            Path file,
            SpeechToTextModel model,
            String language,
            String prompt,
            TranscriptionResponseFormat responseFormat,
            Double temperature,
            List<TimestampGranularity> timestampGranularities
    ) {
        super(builder);
        if (file == null) {
            throw new IllegalArgumentException("Audio file path must not be null.");
        }
        if (model == null) {
            throw new IllegalArgumentException("SpeechToTextModel must not be null.");
        }
        this.file = file;
        this.model = model;
        this.language = language;
        this.prompt = prompt;
        this.responseFormat = (responseFormat != null) ? responseFormat : TranscriptionResponseFormat.JSON;
        this.temperature = (temperature != null) ? temperature : 0.0;
        this.timestampGranularities = (timestampGranularities != null)
                ? Collections.unmodifiableList(new ArrayList<>(timestampGranularities))
                : Collections.emptyList();
        if(!this.timestampGranularities.isEmpty() && this.responseFormat != TranscriptionResponseFormat.VERBOSE_JSON) {
            throw new IllegalArgumentException("Timestamp granularities are only supported with response_format=verbose_json");
        }
    }

    @Override
    public String getRelativeUrl() {
        return "/audio/transcriptions";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * We no longer use the old string-based method for multipart data.
     * It's kept here only because OpenAIRequest requires it.
     * Returning null ensures the caller won't actually use this string approach.
     */
    @Override
    public String getBody() {
        return null;
    }

    /**
     * Builds the multipart/form-data payload as a raw byte array.
     * We put the file's raw bytes in the "file" part so that
     * the server can parse it correctly.
     *
     * @return byte array containing the entire multipart data
     */
    public byte[] getBodyBytes() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // Read the file bytes
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

            // -- language part (if set)
            if (language != null && !language.isBlank()) {
                out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
                out.write("Content-Disposition: form-data; name=\"language\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                out.write(language.getBytes(StandardCharsets.UTF_8));
                out.write("\r\n".getBytes(StandardCharsets.UTF_8));
            }

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

            // -- timestamp granularities
            for (TimestampGranularity tg : timestampGranularities) {
                out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
                out.write("Content-Disposition: form-data; name=\"timestamp_granularities[]\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                out.write(tg.value().getBytes(StandardCharsets.UTF_8));
                out.write("\r\n".getBytes(StandardCharsets.UTF_8));
            }

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
    public OpenAICreateTranscriptionResponse createResponse(String responseBody) {
        return new OpenAICreateTranscriptionResponse(responseBody, this);
    }

    public static Builder builder(OpenAIClient client) {
        return new Builder(client);
    }

    public static final class Builder  extends ApiRequestBuilderBase<Builder, OpenAICreateTranscriptionRequest>  {
        private final OpenAIClient client;
        private Path file;
        private SpeechToTextModel model = SpeechToTextModel.WHISPER_1; // default
        private String language;
        private String prompt;
        private TranscriptionResponseFormat responseFormat;
        private Double temperature;
        private List<TimestampGranularity> timestampGranularities = new ArrayList<>();

        public Builder(OpenAIClient client) {
            this.client = client;
        }

        public Builder file(Path filePath) {
            this.file = filePath;
            return this;
        }

        public Builder model(SpeechToTextModel model) {
            this.model = model;
            return this;
        }

        /**
         * The language of the input audio (ISO-639-1).
         */
        public Builder language(String language) {
            this.language = language;
            return this;
        }

        /**
         * An optional text to guide the model's style or continue a previous audio segment.
         */
        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        /**
         * The format of the output, in one of: json, text, srt, verbose_json, or vtt.
         */
        public Builder responseFormat(TranscriptionResponseFormat format) {
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

        /**
         * The timestamp granularity (or granularity/ies) for verbose_json format.
         * Acceptable values: word, segment
         */
        public Builder timestampGranularities(List<TimestampGranularity> granularities) {
            this.timestampGranularities = granularities;
            return this;
        }

        public OpenAICreateTranscriptionRequest build() {
            return new OpenAICreateTranscriptionRequest(
                    this,
                    file,
                    model,
                    language,
                    prompt,
                    responseFormat,
                    temperature,
                    timestampGranularities
            );
        }

        @Override
        public OpenAICreateTranscriptionResponse executeWithExponentialBackoff() {
            return client.sendRequest(build());
        }

        @Override
        public OpenAICreateTranscriptionResponse execute() {
            return client.sendRequest(build());
        }
    }
}
