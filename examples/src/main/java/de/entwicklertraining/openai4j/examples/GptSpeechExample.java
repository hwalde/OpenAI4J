package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.openai4j.audio.speech.*;
import de.herbertwalde.util.AudioService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Demonstrates usage of the TTS endpoint with GptCreateSpeechRequest.
 */
public class GptSpeechExample {

    public static void main(String[] args) {

        // 1) Build and execute a TTS request
        GptCreateSpeechResponse response = GptCreateSpeechRequest.builder()
                .model(SpeechModel.TTS_1_HD)    // or TTS_1
                .input("Willkommen zur KVB Schulung - Tag 3")
                .voice(SpeechVoice.ALLOY)
                .responseFormat(SpeechResponseFormat.MP3) // default is MP3 anyway
                .speed(1.0)  // a bit faster than normal
                .execute();

        // 2) Check for errors
        if (response.isErrorJson()) {
            System.err.println("Server returned an error: " + response.maybeErrorJson().toString());
            return;
        }

        // 3) Retrieve audio data (MP3 in this example)
        byte[] audioBytes = response.audioData();
        System.out.println("Received " + audioBytes.length + " bytes of audio data.");

        // Here you could write the data to a file:
        Path outputFilePath = Paths.get("output.mp3");
        try (FileOutputStream fos = new FileOutputStream(outputFilePath.toFile())) {
             fos.write(audioBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Wrote TTS audio to " + outputFilePath.toAbsolutePath());

        AudioService audioService = new AudioService();
        audioService.play(outputFilePath);
    }
}
