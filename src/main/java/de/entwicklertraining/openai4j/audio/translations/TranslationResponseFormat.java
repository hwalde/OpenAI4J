package de.entwicklertraining.openai4j.audio.translations;

/**
 * The format of the translation output.
 * Default is "json".
 *
 * Options: json, text, srt, verbose_json, vtt
 */
public enum TranslationResponseFormat {
    JSON("json"),
    TEXT("text"),
    SRT("srt"),
    VERBOSE_JSON("verbose_json"),
    VTT("vtt");

    private final String value;

    TranslationResponseFormat(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
