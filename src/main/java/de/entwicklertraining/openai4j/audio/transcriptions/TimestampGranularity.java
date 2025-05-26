package de.entwicklertraining.openai4j.audio.transcriptions;

/**
 * Optional values for "timestamp_granularities" to populate if
 * response_format is "verbose_json".
 * Each value will be sent as "timestamp_granularities[]=word" or "segment".
 */
public enum TimestampGranularity {
    WORD("word"),
    SEGMENT("segment");

    private final String value;

    TimestampGranularity(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
