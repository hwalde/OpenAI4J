package de.entwicklertraining.openai4j.audio.speech;

/**
 * Supported voices: "alloy", "ash", "coral", "echo", "fable",
 * "onyx", "nova", "sage", "shimmer".
 */
public enum SpeechVoice {
    ALLOY("alloy"),
    ASH("ash"),
    CORAL("coral"),
    ECHO("echo"),
    FABLE("fable"),
    ONYX("onyx"),
    NOVA("nova"),
    SAGE("sage"),
    SHIMMER("shimmer");

    private final String value;

    SpeechVoice(String value) {
        this.value = value;
    }

    /**
     * Returns the literal string expected by the API for this voice.
     */
    public String value() {
        return value;
    }
}
