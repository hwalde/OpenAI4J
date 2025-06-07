package de.entwicklertraining.openai4j.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.entwicklertraining.openai4j.audio.service.OpenAITranscribeAudioService;
import de.entwicklertraining.openai4j.audio.service.OpenAITranscribeAudioService.TranscriptionSettings;
import de.entwicklertraining.openai4j.audio.transcriptions.TimestampGranularity;
import de.entwicklertraining.openai4j.audio.service.VerboseTranscription;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Demonstrates the {@code OpenAITranscribeAudioService}.
 * To run this example you need to include the optional
 * {@code openai4j-audio-service} dependency in your project.
 */
public class OpenAITranscribeAudioServiceExample {

    public static void main(String[] args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Adjust the path to your local audio file.
        // This can be e.g. an .mp3 or .m4a, etc.
        Path sampleAudio = Path.of("openai4j-examples/src/main/resources/hello-world.wav");

        // Define output file path
        Path outputFile = Paths.get("transcription_output.txt");

        // Use BufferedWriter to write to the file
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            // 1) Transcribe to simple text:
            String plainText = OpenAITranscribeAudioService.getInstance().transcribeAudioToText(sampleAudio);
            writer.write("=== Plain Text Transcription ===\n");
            writer.write(plainText);
            writer.write("\n\n");

            // 2) Transcribe to SRT subtitles:
            String srtSubtitles = OpenAITranscribeAudioService.getInstance().transcribeAudioToSrt(sampleAudio);
            writer.write("=== SRT Subtitles ===\n");
            writer.write(srtSubtitles);
            writer.write("\n\n");

            // 3) Transcribe to VTT subtitles:
            String vttSubtitles = OpenAITranscribeAudioService.getInstance().transcribeAudioToVtt(sampleAudio);
            writer.write("=== VTT Subtitles ===\n");
            writer.write(vttSubtitles);
            writer.write("\n\n");

            // 4) Transcribe to a verbose JSON format with word- or segment-level timestamps:
            VerboseTranscription verbose = OpenAITranscribeAudioService.getInstance()
                    .transcribeAudioToVerboseTranscription(sampleAudio);
            writer.write("=== Verbose Transcription (JSON) ===\n");
            writer.write("Language: " + verbose.getLanguage() + "\n");
            writer.write("Duration: " + verbose.getDuration() + "\n");
            writer.write("Full text: " + verbose.getText() + "\n");
            writer.write("Number of segments: " + (verbose.getSegments() == null ? 0 : verbose.getSegments().size()) + "\n");
            writer.write("Number of words: " + (verbose.getWords() == null ? 0 : verbose.getWords().size()) + "\n");
            writer.write("Segments converted to json: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(verbose.getSegments()) + "\n");
            writer.write("Words converted to json: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(verbose.getWords()) + "\n\n");

            // 5) Demonstrate custom settings, e.g. specifying language + temperature + prompt + word timestamps:
            TranscriptionSettings settings = new TranscriptionSettings()
                    .setLanguage("en")                     // e.g. "en", "de", "es"...
                    .setTemperature(0.2)                   // for more variety in the transcription
                    .setPrompt("Context for the conversation, e.g. 'It's an interview.'")
                    .setTimestampGranularities(Arrays.asList(TimestampGranularity.WORD));

            String textWithSettings = OpenAITranscribeAudioService.getInstance().transcribeAudioToText(sampleAudio, settings);
            writer.write("=== Transcription With Custom Settings ===\n");
            writer.write(textWithSettings);
            writer.write("\n\n");

            // 6) Another example: verbose transcription with segment + word timestamps:
            settings.setTimestampGranularities(Arrays.asList(
                    TimestampGranularity.WORD,
                    TimestampGranularity.SEGMENT
            ));
            VerboseTranscription vtx = OpenAITranscribeAudioService.getInstance()
                    .transcribeAudioToVerboseTranscription(sampleAudio, settings);
            writer.write("=== Verbose Transcription With Both Granularities ===\n");
            writer.write("Text: " + vtx.getText() + "\n");
            writer.write("Segments: " + vtx.getSegments().size() + "\n");
            writer.write("Words: " + vtx.getWords().size() + "\n");

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
