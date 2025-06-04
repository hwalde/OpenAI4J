package de.entwicklertraining.openai4j.audio.transcriptions;

import java.util.List;

/**
 * Model object representing the response from the OpenAI transcription
 * endpoint when {@code response_format} is set to {@code verbose_json}.
 */
public class VerboseTranscription {
    private String language;
    private double duration;
    private String text;
    private List<Word> words;
    private List<Segment> segments;

    // Getters and setters

    /**
     * @return ISO language code detected for the audio
     */
    public String getLanguage() {
        return language;
    }
    /**
     * @param language ISO language code detected for the audio
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return total duration of the audio in seconds
     */
    public double getDuration() {
        return duration;
    }
    /**
     * @param duration total duration of the audio in seconds
     */
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * @return full transcription text
     */
    public String getText() {
        return text;
    }
    /**
     * @param text full transcription text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return list of recognised words including timestamps
     */
    public List<Word> getWords() {
        return words;
    }
    /**
     * @param words list of recognised words including timestamps
     */
    public void setWords(List<Word> words) {
        this.words = words;
    }

    /**
     * @return list of segments covering the entire transcription
     */
    public List<Segment> getSegments() {
        return segments;
    }
    /**
     * @param segments list of segments covering the entire transcription
     */
    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }
}
