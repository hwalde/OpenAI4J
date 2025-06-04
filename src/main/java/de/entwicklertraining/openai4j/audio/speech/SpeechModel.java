package de.entwicklertraining.openai4j.audio.speech;

/**
 * The available TTS models: "tts-1" or "tts-1-hd".
 */
public enum SpeechModel {
    TTS_1("tts-1"),
    TTS_1_HD("tts-1-hd");

    private final String value;

    SpeechModel(String value) {
        this.value = value;
    }

    /**
     * String literal to send to the API (e.g. "tts-1").
     */
    public String value() {
        return value;
    }
}
