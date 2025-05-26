package de.entwicklertraining.openai4j.audio.speech;

/**
 * Supported audio formats for TTS output:
 * mp3, opus, aac, flac, wav, pcm
 */
public enum SpeechResponseFormat {
    MP3("mp3"),
    OPUS("opus"),
    AAC("aac"),
    FLAC("flac"),
    WAV("wav"),
    PCM("pcm");

    private final String value;

    SpeechResponseFormat(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
