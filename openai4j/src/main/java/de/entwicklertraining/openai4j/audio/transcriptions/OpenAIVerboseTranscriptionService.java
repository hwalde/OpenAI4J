package de.entwicklertraining.openai4j.audio.transcriptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton service for handling Verbose Transcription objects.
 */
public class OpenAIVerboseTranscriptionService {

    // -- Singleton setup --
    private static OpenAIVerboseTranscriptionService instance;

    private OpenAIVerboseTranscriptionService() {
        // Private constructor to prevent instantiation
    }

    public static OpenAIVerboseTranscriptionService getInstance() {
        if (instance == null) {
            synchronized (OpenAIVerboseTranscriptionService.class) {
                if (instance == null) {
                    instance = new OpenAIVerboseTranscriptionService();
                }
            }
        }
        return instance;
    }

    /**
     * Converts a JSON object representing verbose transcription into a VerboseTranscription model object.
     *
     * @param verboseTranscriptionJson The JSON object containing verbose transcription data
     * @return A VerboseTranscription object populated with data from the provided JSON
     */
    public VerboseTranscription convertJsonToObject(JSONObject verboseTranscriptionJson) {
        VerboseTranscription transcription = new VerboseTranscription();

        // Basic Fields
        // Some JSON responses store language/duration as strings; adapt as needed:
        transcription.setLanguage(verboseTranscriptionJson.optString("language"));
        transcription.setDuration(verboseTranscriptionJson.optDouble("duration", 0.0));
        transcription.setText(verboseTranscriptionJson.optString("text", ""));

        // Optional: if the JSON has a "task" field, you could store it somewhere
        // e.g., transcription.setTask(verboseTranscriptionJson.optString("task", ""));

        // Segments
        List<Segment> segments = new ArrayList<>();
        if (verboseTranscriptionJson.has("segments")) {
            JSONArray segmentsJsonArray = verboseTranscriptionJson.getJSONArray("segments");
            for (int i = 0; i < segmentsJsonArray.length(); i++) {
                JSONObject segmentJson = segmentsJsonArray.getJSONObject(i);
                Segment segment = new Segment();
                segment.setId(segmentJson.optInt("id", 0));
                segment.setSeek(segmentJson.optInt("seek", 0));
                segment.setStart(segmentJson.optDouble("start", 0.0));
                segment.setEnd(segmentJson.optDouble("end", 0.0));
                segment.setText(segmentJson.optString("text", ""));

                // You could parse tokens or other fields if they exist
                // For example:
                if (segmentJson.has("tokens")) {
                    JSONArray tokensJsonArray = segmentJson.getJSONArray("tokens");
                    List<Integer> tokens = new ArrayList<>();
                    for (int t = 0; t < tokensJsonArray.length(); t++) {
                        tokens.add(tokensJsonArray.getInt(t));
                    }
                    segment.setTokens(tokens);
                }

                segments.add(segment);
            }
        }
        transcription.setSegments(segments);

        // Words (if your verbose JSON has a "words" field with timestamps)
        List<Word> words = new ArrayList<>();
        if (verboseTranscriptionJson.has("words")) {
            JSONArray wordsJsonArray = verboseTranscriptionJson.getJSONArray("words");
            for (int i = 0; i < wordsJsonArray.length(); i++) {
                JSONObject wordJson = wordsJsonArray.getJSONObject(i);
                Word word = new Word();
                word.setWord(wordJson.optString("word", ""));
                word.setStart(wordJson.optDouble("start", 0.0));
                word.setEnd(wordJson.optDouble("end", 0.0));
                words.add(word);
            }
        }
        transcription.setWords(words);

        return transcription;
    }

    /**
     * Combines multiple VerboseTranscription objects into a single VerboseTranscription.
     * 
     * Basic approach:
     * - Use the first transcription's language as the combined language (or adapt if you want a different behavior).
     * - Sum durations (or keep the max if that suits your data).
     * - Append texts with a space or newline separator.
     * - Combine segments, adjusting start/end times so they are sequential (if desired).
     * - Combine words similarly, if you track individual word timestamps.
     *
     * @param verboseTranscriptions One or more VerboseTranscription objects to be combined
     * @return A new VerboseTranscription representing the merged content
     */
    public VerboseTranscription combine(VerboseTranscription... verboseTranscriptions) {
        VerboseTranscription combined = new VerboseTranscription();

        if (verboseTranscriptions == null || verboseTranscriptions.length == 0) {
            return combined; // return empty if nothing to combine
        }

        // Use the first transcription's language
        combined.setLanguage(verboseTranscriptions[0].getLanguage());

        // Combine text with space separation (you can customize)
        StringBuilder combinedText = new StringBuilder();
        // We'll keep track of the current offset for segments and words
        double currentOffset = 0.0;

        List<Segment> allSegments = new ArrayList<>();
        List<Word> allWords = new ArrayList<>();

        double totalDuration = 0.0;

        for (int i = 0; i < verboseTranscriptions.length; i++) {
            VerboseTranscription t = verboseTranscriptions[i];
            if (t == null) continue;

            // Sum durations
            totalDuration += t.getDuration();

            // Append text
            if (combinedText.length() > 0) {
                combinedText.append(" ");
            }
            combinedText.append(t.getText());

            // Combine segments
            for (Segment seg : t.getSegments()) {
                Segment newSeg = new Segment();
                newSeg.setId(seg.getId());
                newSeg.setSeek(seg.getSeek());
                // offset start/end times by currentOffset
                newSeg.setStart(seg.getStart() + currentOffset);
                newSeg.setEnd(seg.getEnd() + currentOffset);
                newSeg.setText(seg.getText());
                newSeg.setTokens(seg.getTokens());
                allSegments.add(newSeg);
            }

            // Combine words (if you track word timestamps)
            for (Word w : t.getWords()) {
                Word newWord = new Word();
                newWord.setWord(w.getWord());
                newWord.setStart(w.getStart() + currentOffset);
                newWord.setEnd(w.getEnd() + currentOffset);
                allWords.add(newWord);
            }

            // Increase offset by this transcription's duration
            currentOffset += t.getDuration();
        }

        combined.setDuration(totalDuration);
        combined.setText(combinedText.toString());
        combined.setSegments(allSegments);
        combined.setWords(allWords);

        return combined;
    }
}
