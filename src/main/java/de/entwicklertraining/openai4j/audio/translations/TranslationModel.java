package de.entwicklertraining.openai4j.audio.translations;

/**
 * The currently available translation models.
 * Per API docs, only "whisper-1" is supported.
 */
public enum TranslationModel {
    WHISPER_1("whisper-1");

    private final String value;

    TranslationModel(String value) {
        this.value = value;
    }

    /**
     * Returns the model identifier used in API requests.
     */
    public String value() {
        return value;
    }
}
