package de.entwicklertraining.openai4j.audio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SrtService is a singleton class that can parse and merge multiple SRT contents (as strings).
 */
public final class SrtService {

    private static final Logger logger = LoggerFactory.getLogger(SrtService.class);

    // Singleton instance
    private static final SrtService INSTANCE = new SrtService();

    // Patterns and constants
    private static final Pattern PATTERN_TIME = Pattern.compile("([\\d]{2}:[\\d]{2}:[\\d]{2},[\\d]{3}).*([\\d]{2}:[\\d]{2}:[\\d]{2},[\\d]{3})");
    private static final Pattern PATTERN_NUMBERS = Pattern.compile("(\\d+)");
    private static final Pattern PATTERN_TIME_SINGLE = Pattern.compile("([\\d]{2}):([\\d]{2}):([\\d]{2}),([\\d]{3})");

    private static final String REGEX_REMOVE_TAGS = "<[^>]*>";
    private static final String TIME_SEPARATOR = " --> ";

    // Time constants
    private static final long MILLIS_IN_SECOND = 1000;
    private static final long MILLIS_IN_MINUTE = MILLIS_IN_SECOND * 60; // 60000
    private static final long MILLIS_IN_HOUR   = MILLIS_IN_MINUTE * 60; // 3600000

    private SrtService() {
        // private constructor for singleton
    }

    /**
     * Accessor for the singleton instance.
     * @return SrtService instance
     */
    public static SrtService getInstance() {
        return INSTANCE;
    }

    /**
     * Combine multiple SRT contents (passed as strings) into a single SRT string.
     * 
     * @param srtParts one or more SRT contents
     * @return a merged SRT string
     */
    public String combine(String... srtParts) {
        List<Subtitle> allSubtitles = new ArrayList<>();
        long lastEndTime = 0;

        for (String srtPart : srtParts) {
            List<Subtitle> current = parseSubtitles(srtPart);
            // Shift times so that each subsequent part starts after the last one ends
            mergeSubtitles(allSubtitles, current, lastEndTime);

            // Update lastEndTime
            if (!current.isEmpty()) {
                lastEndTime = current.get(current.size() - 1).timeOut;
            }
        }

        // Format as a single SRT content
        return formatSubtitles(allSubtitles);
    }

    /**
     * Parse an SRT string into a list of Subtitle objects.
     */
    private List<Subtitle> parseSubtitles(String srtContent) {
        List<Subtitle> subtitles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(srtContent))) {
            String line;
            Subtitle subtitle = new Subtitle();
            StringBuilder textBuffer = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    // End of a block
                    if (textBuffer.length() > 0) {
                        String text = textBuffer.toString().trim();
                        // remove HTML tags
                        text = text.replaceAll(REGEX_REMOVE_TAGS, "");
                        subtitle.text = text;
                        subtitles.add(subtitle);
                        textBuffer.setLength(0);
                    }
                    // Prepare next
                    subtitle = new Subtitle();
                    continue;
                }

                // Check if line is numeric ID
                Matcher matcherNumber = PATTERN_NUMBERS.matcher(line);
                if (matcherNumber.matches()) {
                    // found an ID line
                    subtitle.id = Integer.parseInt(matcherNumber.group(1));
                    continue;
                }

                // Check if it's time line
                Matcher matcherTime = PATTERN_TIME.matcher(line);
                if (matcherTime.find()) {
                    String startTime = matcherTime.group(1);
                    String endTime = matcherTime.group(2);

                    subtitle.startTime = startTime;
                    subtitle.endTime = endTime;

                    // Convert to long
                    subtitle.timeIn = textTimeToMillis(startTime);
                    subtitle.timeOut = textTimeToMillis(endTime);

                    continue;
                }

                // Otherwise, it's subtitle text
                textBuffer.append(line).append("\n");
            }

            // In case the file doesn't end with an empty line
            if (textBuffer.length() > 0) {
                String text = textBuffer.toString().trim();
                text = text.replaceAll(REGEX_REMOVE_TAGS, "");
                subtitle.text = text;
                subtitles.add(subtitle);
            }
        } catch (Exception e) {
            logger.error("Error parsing SRT content", e);
        }
        return subtitles;
    }

    /**
     * Merge newly-parsed subtitles into the accumulated list, shifting their times by lastEndTime.
     */
    private void mergeSubtitles(List<Subtitle> allSubtitles, List<Subtitle> newSubtitles, long lastEndTime) {
        for (Subtitle sub : newSubtitles) {
            // shift times
            sub.timeIn += lastEndTime;
            sub.timeOut += lastEndTime;
            allSubtitles.add(sub);
        }
    }

    /**
     * Format a list of subtitles into a valid SRT string.
     */
    private String formatSubtitles(List<Subtitle> subtitles) {
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (Subtitle sub : subtitles) {
            sb.append(index++).append("\n");
            sb.append(millisToText(sub.timeIn))
              .append(TIME_SEPARATOR)
              .append(millisToText(sub.timeOut))
              .append("\n");
            sb.append(sub.text).append("\n\n");
        }
        return sb.toString();
    }

    /**
     * Convert a time string (HH:mm:ss,SSS) into milliseconds.
     */
    private long textTimeToMillis(String time) throws Exception {
        if (time == null) {
            throw new NullPointerException("Time must not be null");
        }

        Matcher matcher = PATTERN_TIME_SINGLE.matcher(time);
        if (!matcher.find()) {
            throw new Exception("Incorrect time format: " + time);
        }

        int hours = Integer.parseInt(matcher.group(1));
        int minutes = Integer.parseInt(matcher.group(2));
        int seconds = Integer.parseInt(matcher.group(3));
        int millis = Integer.parseInt(matcher.group(4));

        long msTime = 0;
        msTime += hours * MILLIS_IN_HOUR;
        msTime += minutes * MILLIS_IN_MINUTE;
        msTime += seconds * MILLIS_IN_SECOND;
        msTime += millis;

        return msTime;
    }

    /**
     * Convert milliseconds to time string (HH:mm:ss,SSS).
     */
    private String millisToText(long millisToText) {
        long hours = millisToText / 3600000;
        long remainder = millisToText % 3600000;
        long minutes = remainder / 60000;
        remainder = remainder % 60000;
        long seconds = remainder / 1000;
        long millis = remainder % 1000;

        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, millis);
    }

    /**
     * Private Subtitle data structure
     */
    private static class Subtitle {
        public int id;
        public String startTime;
        public String endTime;
        public String text;
        public long timeIn;
        public long timeOut;

        // nextSubtitle not strictly necessary here,
        // but can be added if you wish to replicate SRTParser logic exactly.
    }
}
