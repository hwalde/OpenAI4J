package de.entwicklertraining.openai4j.audio.transcriptions;

import de.entwicklertraining.openai4j.GptResponse;
import org.json.JSONObject;

/**
 * Represents the response from POST https://api.openai.com/v1/audio/transcriptions.
 * Depending on the request's "response_format", this may be:
 *  - JSON or text, srt, verbose_json, vtt.
 * 
 * For "json", the response typically has {"text":"..."}.
 * For "text", you'll receive just the transcription text as a string.
 * For "verbose_json", there's a more complex JSON structure with "words" and "segments".
 * 
 * This class holds the raw JSON in getJson(). You can also call text() if 
 * the response includes a "text" field. If you used "text" response_format, 
 * that entire string is stored in getJson().toString().
 */
public final class GptCreateTranscriptionResponse extends GptResponse<GptCreateTranscriptionRequest> {

    /**
     * Creates a response object from the raw HTTP body returned by the API.
     * The body may be plain text or JSON depending on the requested format.
     *
     * @param rawResponseBody raw response body returned by the server
     * @param request         originating request instance
     */
    public GptCreateTranscriptionResponse(String rawResponseBody,
                                          GptCreateTranscriptionRequest request) {
        super(parseToJsonSafely(rawResponseBody), request);
    }

    private static JSONObject parseToJsonSafely(String raw) {
        // If response_format=text, the body is plain text, not JSON => wrap in a JSON
        // If response_format=srt/vtt => also plain text.
        // We'll do a best-effort parse. If it's valid JSON, parse; otherwise, treat it as text in "text" key.
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
     * If the JSON has a "text" field, return it; otherwise null.
     * If the response format was "text", "srt", or "vtt", 
     * the entire response is placed in "text".
     */
    public String text() {
        try {
            return getJson().getString("text");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * For "verbose_json" responses, you can manually inspect the entire JSON:
     *   {
     *     "task": "transcribe",
     *     "language": "...",
     *     "duration": 123.456,
     *     "text": "The transcribed text",
     *     "words": [...],
     *     "segments": [...]
     *   }
     * 
     * Use getJson() directly to read those fields.
     */
}
