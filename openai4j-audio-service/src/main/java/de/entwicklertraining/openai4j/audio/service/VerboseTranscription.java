package de.entwicklertraining.openai4j.audio.service;

import java.util.List;

public class VerboseTranscription {
    private String language;
    private double duration;
    private String text;
    private List<Word> words;
    private List<Segment> segments;

    // Getters and setters (you can use Lombok if desired)
    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    public double getDuration() {
        return duration;
    }
    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public List<Word> getWords() {
        return words;
    }
    public void setWords(List<Word> words) {
        this.words = words;
    }

    public List<Segment> getSegments() {
        return segments;
    }
    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }
}
