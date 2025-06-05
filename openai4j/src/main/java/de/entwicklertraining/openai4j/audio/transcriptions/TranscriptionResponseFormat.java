package de.entwicklertraining.openai4j.audio.transcriptions;

/**
 * The format of the transcription output.
 * Default is "json".
 */
public enum TranscriptionResponseFormat {
    JSON("json"),
    TEXT("text"),
    SRT("srt"),
    VERBOSE_JSON("verbose_json"),
    VTT("vtt");

    private final String value;

    TranscriptionResponseFormat(String value) {
        this.value = value;
    }

    /**
     * Returns the string literal used in requests.
     */
    public String value() {
        return value;
    }
}
