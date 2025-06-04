package de.entwicklertraining.openai4j.audio.transcriptions;

/**
 * A single recognised word including timing information.
 */
public class Word {
    private String word;
    private double start;
    private double end;

    /**
     * @return the recognised word text
     */
    public String getWord() {
        return word;
    }
    /**
     * @param word the recognised word text
     */
    public void setWord(String word) {
        this.word = word;
    }

    /**
     * @return start timestamp of the word in seconds
     */
    public double getStart() {
        return start;
    }
    /**
     * @param start start timestamp of the word in seconds
     */
    public void setStart(double start) {
        this.start = start;
    }

    /**
     * @return end timestamp of the word in seconds
     */
    public double getEnd() {
        return end;
    }
    /**
     * @param end end timestamp of the word in seconds
     */
    public void setEnd(double end) {
        this.end = end;
    }
}
