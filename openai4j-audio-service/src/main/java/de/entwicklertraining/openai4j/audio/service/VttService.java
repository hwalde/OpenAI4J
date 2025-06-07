package de.entwicklertraining.openai4j.audio.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VttService {

    // Singleton instance
    private static final VttService INSTANCE = new VttService();

    // Regex pattern for matching timestamps (e.g., 00:01:23.456)
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}\\.\\d{3})");

    // Private constructor to prevent instantiation
    private VttService() { }

    /**
     * Get the singleton instance of the VttService.
     */
    public static VttService getInstance() {
        return INSTANCE;
    }

    /**
     * Combine multiple VTT parts (strings) into a single VTT-String with normalized timecodes.
     *
     * @param vttParts One or more strings representing partial VTT content. 
     *                 You can assume these are given in the correct chronological order.
     * @return A single WebVTT string with merged and normalized timecodes.
     */
    public String combine(String... vttParts) {
        if (vttParts == null || vttParts.length == 0) {
            throw new IllegalArgumentException("No VTT parts provided.");
        }

        // The result of merging lines from all VTT parts
        List<String> mergedLines = new ArrayList<>();
        // The official header for WebVTT:
        mergedLines.add("WEBVTT");
        mergedLines.add("");  // Blank line after the header

        long totalOffsetMs = 0;  // Running offset in milliseconds

        for (String part : vttParts) {
            // Split the incoming part into lines
            List<String> lines = Arrays.asList(part.split("\\r?\\n"));

            // Skip the header line(s) if present (commonly "WEBVTT" and possibly a blank line)
            int startIndex = 0;
            if (!lines.isEmpty() && lines.get(0).trim().equalsIgnoreCase("webvtt")) {
                startIndex = 1;
                // Some VTTs have a blank line after the header:
                if (lines.size() > 1 && lines.get(1).trim().isEmpty()) {
                    startIndex = 2;
                }
            }

            // For each line, adjust the timestamps by totalOffsetMs
            for (int i = startIndex; i < lines.size(); i++) {
                String line = lines.get(i);

                // Find all timestamps and shift them by totalOffsetMs
                Matcher matcher = TIMESTAMP_PATTERN.matcher(line);
                StringBuffer sb = new StringBuffer();
                while (matcher.find()) {
                    String timestamp = matcher.group(1);
                    long timeMs = parseTimestampToMs(timestamp);
                    timeMs += totalOffsetMs;  // add the offset
                    String newTimestamp = formatMsToTimestamp(timeMs);
                    matcher.appendReplacement(sb, newTimestamp);
                }
                matcher.appendTail(sb);

                mergedLines.add(sb.toString());
            }

            // Update totalOffsetMs by the duration of this VTT part
            totalOffsetMs += calculateDuration(lines, startIndex);
        }

        // Return a single string joined by newlines
        return String.join("\n", mergedLines);
    }

    /**
     * Parse a timestamp string in the format HH:MM:SS.mmm into milliseconds.
     */
    private static long parseTimestampToMs(String timestamp) {
        String[] parts = timestamp.split(":|\\.");
        long hours = Long.parseLong(parts[0]);
        long minutes = Long.parseLong(parts[1]);
        long seconds = Long.parseLong(parts[2]);
        long milliseconds = Long.parseLong(parts[3]);
        return (hours * 3600 + minutes * 60 + seconds) * 1000 + milliseconds;
    }

    /**
     * Format milliseconds back into a timestamp string HH:MM:SS.mmm.
     */
    private static String formatMsToTimestamp(long timeMs) {
        long hours = timeMs / 3600000;
        long minutes = (timeMs % 3600000) / 60000;
        long seconds = (timeMs % 60000) / 1000;
        long milliseconds = timeMs % 1000;
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
    }

    /**
     * Calculate the maximum timestamp (in ms) found in the given lines. 
     * This reflects the length of the VTT segment and can be used to 
     * determine how much to offset subsequent parts.
     *
     * @param lines      Lines of the current VTT part
     * @param startIndex First index of actual cues in the lines list 
     *                   (skipping possible "WEBVTT" header lines)
     * @return The duration in milliseconds (i.e., the maximum timestamp)
     */
    private static long calculateDuration(List<String> lines, int startIndex) {
        long duration = 0;
        for (int i = startIndex; i < lines.size(); i++) {
            Matcher matcher = TIMESTAMP_PATTERN.matcher(lines.get(i));
            while (matcher.find()) {
                long timeMs = parseTimestampToMs(matcher.group(1));
                duration = Math.max(duration, timeMs);
            }
        }
        return duration;
    }
}
