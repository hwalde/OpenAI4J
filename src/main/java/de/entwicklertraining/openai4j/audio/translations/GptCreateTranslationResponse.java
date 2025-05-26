package de.entwicklertraining.openai4j.audio.translations;

import de.entwicklertraining.openai4j.GptResponse;
import org.json.JSONObject;

/**
 * Represents the response from POST https://api.openai.com/v1/audio/translations.
 * Depending on the request's "response_format", this may be:
 *  - JSON
 *  - text, srt, verbose_json, vtt.
 *
 * By default (if response_format=json), we typically get {"text":"..."} in the JSON.
 * If "text", "srt", or "vtt" is used, the entire response is plain text, so we
 * wrap it in {"text":"..."} for internal storage.
 */
public final class GptCreateTranslationResponse extends GptResponse<GptCreateTranslationRequest> {

    public GptCreateTranslationResponse(String rawResponseBody, GptCreateTranslationRequest request) {
        super(parseToJsonSafely(rawResponseBody), request);
    }

    private static JSONObject parseToJsonSafely(String raw) {
        // If the response format was text, srt, or vtt => plain text
        // We do a best-effort parse to JSON. If not valid JSON, store in "text" key.
        try {
            return new JSONObject(raw);
        } catch (Exception e) {
            // fallback: store it as { "text": "...raw..." }
            JSONObject fallback = new JSONObject();
            fallback.put("text", raw);
            return fallback;
        }
    }

    /**
     * If JSON has a "text" field, return it; otherwise null.
     */
    public String text() {
        try {
            return getJson().getString("text");
        } catch (Exception e) {
            return null;
        }
    }
}
