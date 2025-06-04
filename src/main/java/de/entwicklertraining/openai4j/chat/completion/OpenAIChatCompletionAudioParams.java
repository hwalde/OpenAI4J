package de.entwicklertraining.openai4j.chat.completion;

import de.entwicklertraining.openai4j.audio.speech.SpeechResponseFormat; // Reusing existing enum for format
import de.entwicklertraining.openai4j.audio.speech.SpeechVoice; // Reusing existing enum for voice

/**
 * Represents the parameters for audio output in a Chat Completion request.
 * Required when audio output is requested with `modalities: ["audio"]`.
 *
 * @see <a href="https://platform.openai.com/docs/guides/audio">OpenAI Audio Guide</a>
 */
public class OpenAIChatCompletionAudioParams {

    private final SpeechVoice voice;

    private final SpeechResponseFormat format; // Default is mp3 according to docs, but let's require explicit setting for clarity

    /**
     * Creates audio parameters for chat completion.
     *
     * @param voice The voice to use for the generated audio. Cannot be null.
     * @param format The format for the generated audio. Cannot be null.
     */
    public OpenAIChatCompletionAudioParams(SpeechVoice voice, SpeechResponseFormat format) {
        if (voice == null) {
            throw new IllegalArgumentException("Voice cannot be null.");
        }
        if (format == null) {
            throw new IllegalArgumentException("Format cannot be null.");
        }
        this.voice = voice;
        this.format = format;
    }

    /**
     * Returns the voice used for speech synthesis.
     *
     * @return the selected voice
     */
    public SpeechVoice getVoice() {
        return voice;
    }

    /**
     * Returns the audio format for the generated speech.
     *
     * @return the response format
     */
    public SpeechResponseFormat getFormat() {
        return format;
    }
}