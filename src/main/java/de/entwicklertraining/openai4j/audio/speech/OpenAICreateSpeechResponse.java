package de.entwicklertraining.openai4j.audio.speech;

import de.entwicklertraining.openai4j.OpenAIResponse;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

/**
 * Represents the response from POST https://api.openai.com/v1/audio/speech
 * <p>
 * Typically this is raw audio content, but if there's an error, the server
 * may respond with JSON. We store the raw content internally. You can use
 * {@link #audioData()} to retrieve the audio as bytes if the response is presumably binary.
 * <p>
 * If it is an error JSON, you can check {@link #isErrorJson()}.
 */
public final class OpenAICreateSpeechResponse extends OpenAIResponse<OpenAICreateSpeechRequest> {

    /**
     * If success, we store the raw audio bytes.
     * If error, we store the raw error bytes (which can parse as JSON).
     */
    private final byte[] rawBytes;

    /**
     * For backward compatibility, we also store the rawBody string if the request
     * was created by createResponse(String). This might happen if something fails
     * early in the pipeline or the server returns JSON in non-binary mode.
     */
    private final String rawBody;

    /**
     * Constructor for the case where we have a string-based response.
     * Usually indicates an error JSON or fallback text.
     */
    public OpenAICreateSpeechResponse(String rawResponseBody, OpenAICreateSpeechRequest request) {
        super(null, request);
        this.rawBody = rawResponseBody;
        this.rawBytes = null;
    }

    /**
     * Constructor for the case where we have raw bytes (likely audio).
     * Could still be error JSON if the server responded with an error in bytes form.
     */
    public OpenAICreateSpeechResponse(byte[] rawBytes, OpenAICreateSpeechRequest request) {
        super(null, request);
        this.rawBytes = rawBytes;
        this.rawBody = null;
    }

    /**
     * Returns the raw body as a string (if we originally got it as a string).
     * If we received bytes, this may be null.
     */
    public String rawBody() {
        return rawBody;
    }

    /**
     * If the server returned JSON with an error, this method returns true.
     * We'll interpret rawBytes or rawBody to see if it's JSON containing "error".
     */
    public boolean isErrorJson() {
        return (maybeErrorJson() != null);
    }

    /**
     * If this is an error JSON (i.e. parseable to a JSON object with an "error" key),
     * returns that JSON. Otherwise returns null.
     */
    public JSONObject maybeErrorJson() {
        byte[] contentToCheck;
        if (rawBytes != null) {
            contentToCheck = rawBytes;
        } else if (rawBody != null) {
            // If we only have a string, convert it to bytes for uniform parsing
            contentToCheck = rawBody.getBytes(StandardCharsets.UTF_8);
        } else {
            return null;
        }

        String parsed = new String(contentToCheck, StandardCharsets.UTF_8).trim();
        if (!parsed.startsWith("{")) {
            return null; // not JSON
        }
        try {
            JSONObject obj = new JSONObject(parsed);
            if (obj.has("error")) {
                return obj;
            }
        } catch (Exception ignore) {
            // Not valid JSON
        }
        return null;
    }

    /**
     * If it's not an error, the content is presumably binary data for the audio file.
     * @return the raw audio bytes. If it's an error, returns an empty byte array.
     */
    public byte[] audioData() {
        if (isErrorJson()) {
            // No actual audio if it's an error
            return new byte[0];
        }
        if (rawBytes != null) {
            return rawBytes;
        }
        // Fallback: if there's no rawBytes but we do have rawBody, it's likely corrupted or error text
        return new byte[0];
    }
}
