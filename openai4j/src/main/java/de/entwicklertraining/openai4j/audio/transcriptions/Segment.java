package de.entwicklertraining.openai4j.audio.transcriptions;

import java.util.List;

/**
 * Represents a single segment in a verbose transcription response.  Segments
 * contain the recognised text as well as timing information and token indices.
 */
public class Segment {
    private int id;
    private int seek;
    private double start;
    private double end;
    private String text;
    private List<Integer> tokens;

    /**
     * @return sequential identifier of this segment
     */
    public int getId() {
        return id;
    }
    /**
     * @param id sequential identifier of this segment
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return number of samples to seek before this segment starts
     */
    public int getSeek() {
        return seek;
    }
    /**
     * @param seek number of samples to seek before this segment starts
     */
    public void setSeek(int seek) {
        this.seek = seek;
    }

    /**
     * @return start time of the segment in seconds
     */
    public double getStart() {
        return start;
    }
    /**
     * @param start start time of the segment in seconds
     */
    public void setStart(double start) {
        this.start = start;
    }

    /**
     * @return end time of the segment in seconds
     */
    public double getEnd() {
        return end;
    }
    /**
     * @param end end time of the segment in seconds
     */
    public void setEnd(double end) {
        this.end = end;
    }

    /**
     * @return recognised text of this segment
     */
    public String getText() {
        return text;
    }
    /**
     * @param text recognised text of this segment
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return token indices belonging to this segment, if available
     */
    public List<Integer> getTokens() {
        return tokens;
    }
    /**
     * @param tokens token indices belonging to this segment
     */
    public void setTokens(List<Integer> tokens) {
        this.tokens = tokens;
    }
}
