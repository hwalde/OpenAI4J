package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.openai4j.GptClient;
import de.entwicklertraining.openai4j.audio.translations.*;

import java.nio.file.Path;

/**
 * A simple example showing how to translate audio into English
 * using the GptCreateTranslationRequest.
 */
public class GptTranslationExample {

    public static void main(String[] args) {
        // Adjust this path to your local audio file if needed
        Path audioFile = Path.of("src/main/resources/audio/deutsch-kurz.wav");

        // Generate GptClient
        GptClient client = new GptClient();

        // Build and execute the request
        GptCreateTranslationResponse response = client.audio().translation()
                .file(audioFile)
                .model(TranslationModel.WHISPER_1)
                .prompt("This is a test prompt in English.")
                .responseFormat(TranslationResponseFormat.JSON)
                .temperature(0.0)
                .execute();

        // Retrieve the translated text
        String translatedText = response.text();

        System.out.println("=== TRANSLATION RESULT ===");
        System.out.println(translatedText);
    }
}
