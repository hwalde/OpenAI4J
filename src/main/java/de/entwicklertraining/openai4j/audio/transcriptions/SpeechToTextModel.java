package de.entwicklertraining.openai4j.audio.transcriptions;

/**
 * The currently available speech-to-text models.
 * OpenAI docs: only "whisper-1" is supported at this time.
 */
public enum SpeechToTextModel {
    WHISPER_1("whisper-1"),
    GPT_4o_MINI_TRANSCRIBE("gpt-4o-mini-transcribe"),
    GPT_4o_TRANSCRIBE("gpt-4o-transcribe");

    private final String value;

    SpeechToTextModel(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
