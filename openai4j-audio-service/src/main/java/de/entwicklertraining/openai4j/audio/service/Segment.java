package de.entwicklertraining.openai4j.audio.service;

import java.util.List;

public class Segment {
    private int id;
    private int seek;
    private double start;
    private double end;
    private String text;
    private List<Integer> tokens;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getSeek() {
        return seek;
    }
    public void setSeek(int seek) {
        this.seek = seek;
    }

    public double getStart() {
        return start;
    }
    public void setStart(double start) {
        this.start = start;
    }

    public double getEnd() {
        return end;
    }
    public void setEnd(double end) {
        this.end = end;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public List<Integer> getTokens() {
        return tokens;
    }
    public void setTokens(List<Integer> tokens) {
        this.tokens = tokens;
    }
}
