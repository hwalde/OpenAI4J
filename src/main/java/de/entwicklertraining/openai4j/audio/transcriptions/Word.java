package de.entwicklertraining.openai4j.audio.transcriptions;

public class Word {
    private String word;
    private double start;
    private double end;

    public String getWord() {
        return word;
    }
    public void setWord(String word) {
        this.word = word;
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
}
