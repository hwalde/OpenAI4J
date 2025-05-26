package de.entwicklertraining.openai4j.audio.speech;

import de.entwicklertraining.openai4j.GptClient;
import de.entwicklertraining.openai4j.GptRequest;
import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import org.json.JSONObject;

/**
 * Represents a request to create speech via POST https://api.openai.com/v1/audio/speech
 * <p>
 * This endpoint returns raw audio data (binary) rather than JSON. Hence, the {@link GptCreateSpeechResponse}
 * will (normally) store the raw audio content as bytes. If there's an error, the response might be JSON.
 *
 * Usage example:
 * <pre>
 *   GptCreateSpeechResponse resp = GptCreateSpeechRequest.builder()
 *       .model(SpeechModel.TTS_1)
 *       .input("Hello world!")
 *       .voice(SpeechVoice.ALLOY)
 *       .responseFormat(SpeechResponseFormat.MP3)
 *       .speed(1.0)
 *       .build()
 *       .execute();
 *
 *   // Then resp.audioData() contains the raw audio bytes (or resp.maybeErrorJson() if an error occurred)
 * </pre>
 */
public final class GptCreateSpeechRequest extends GptRequest<GptCreateSpeechResponse> {

    private final SpeechModel model;
    private final String input;
    private final SpeechVoice voice;
    private final SpeechResponseFormat responseFormat;
    private final Double speed;

    GptCreateSpeechRequest(
            Builder builder,
            SpeechModel model,
            String input,
            SpeechVoice voice,
            SpeechResponseFormat responseFormat,
            Double speed
    ) {
        super(builder);
        if (model == null) {
            throw new IllegalArgumentException("model must not be null.");
        }
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("input (text) must not be null or empty.");
        }
        if (voice == null) {
            throw new IllegalArgumentException("voice must not be null.");
        }

        this.model = model;
        this.input = input;
        this.voice = voice;
        this.responseFormat = (responseFormat != null) ? responseFormat : SpeechResponseFormat.MP3; // default
        this.speed = (speed != null) ? speed : 1.0; // default
    }

    @Override
    public String getRelativeUrl() {
        return "/audio/speech";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * Builds a JSON string with the fields needed by /v1/audio/speech.
     */
    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        body.put("model", model.value());
        body.put("input", input);
        body.put("voice", voice.value());
        body.put("response_format", responseFormat.value());
        if (!speed.equals(1.0)) {
            body.put("speed", speed);
        }
        return body.toString();
    }

    @Override
    public GptCreateSpeechResponse createResponse(String responseBody) {
        // This method wouldn't normally be used for a successful TTS call
        // because we expect binary data. However, if there's an error, the
        // server might respond with JSON. We'll let GptCreateSpeechResponse
        // handle it.
        return new GptCreateSpeechResponse(responseBody, this);
    }

    @Override
    public boolean isBinaryResponse() {
        // Indicate that this endpoint returns raw audio data on success
        return true;
    }

    @Override
    public GptCreateSpeechResponse createResponse(byte[] responseBytes) {
        // TTS endpoint successful call will be raw audio bytes.
        // If the returned bytes happen to be JSON (i.e. an error),
        // GptCreateSpeechResponse will parse that.
        return new GptCreateSpeechResponse(responseBytes, this);
    }

    @Override
    public String getContentType() {
        // We send JSON in the request
        return "application/json";
    }

    public static Builder builder(GptClient client) {
        return new Builder(client);
    }

    public static final class Builder extends ApiRequestBuilderBase<Builder, GptCreateSpeechRequest> {
        private final GptClient client;
        private SpeechModel model = SpeechModel.TTS_1;    // default
        private String input;
        private SpeechVoice voice = SpeechVoice.ALLOY;    // default
        private SpeechResponseFormat responseFormat = SpeechResponseFormat.MP3;
        private Double speed = 1.0;

        public Builder(GptClient client) {
            this.client = client;
        }

        public Builder model(SpeechModel m) {
            this.model = m;
            return this;
        }

        /**
         * Required. The text to generate audio for. (max length ~4k chars)
         */
        public Builder input(String text) {
            this.input = text;
            return this;
        }

        /**
         * Required. The voice to use (alloy, ash, coral, echo, fable, onyx, nova, sage, shimmer).
         */
        public Builder voice(SpeechVoice voice) {
            this.voice = voice;
            return this;
        }

        /**
         * The format of the output: mp3, opus, aac, flac, wav, or pcm.
         */
        public Builder responseFormat(SpeechResponseFormat responseFormat) {
            this.responseFormat = responseFormat;
            return this;
        }

        /**
         * Speed factor from 0.25 to 4.0. Default=1.0
         */
        public Builder speed(Double speed) {
            this.speed = speed;
            return this;
        }

        public GptCreateSpeechRequest build() {
            return new GptCreateSpeechRequest(
                    this,
                    model,
                    input,
                    voice,
                    responseFormat,
                    speed
            );
        }

        @Override
        public GptCreateSpeechResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }

        @Override
        public GptCreateSpeechResponse execute() {
            return client.sendRequest(build());
        }
    }
}
