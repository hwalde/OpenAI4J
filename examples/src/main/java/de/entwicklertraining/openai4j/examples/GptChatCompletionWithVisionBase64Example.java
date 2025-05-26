package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.openai4j.chat.completion.GptChatCompletionRequest;
import de.entwicklertraining.openai4j.chat.completion.GptChatCompletionResponse;

import java.nio.file.Path;

/**
 * Demonstrates how to use Vision with a local image (base64 encoded).
 * The image is located at "src/main/resources/images/AboutMe.jpg".
 * We pass it to the model with "detail=auto" and ask what is in the image.
 */
public class GptChatCompletionWithVisionBase64Example {

    public static void main(String[] args) {
        // Build a Chat Completion request with an image in base64 format
        GptChatCompletionResponse response = GptChatCompletionRequest.builder()
                .model("gpt-4o-mini") // or any model that supports vision
                .addSystemMessage("You are a helpful assistant that can analyze images.")
                .addUserMessage("Please describe the following photo:")
                // Using the newly created method addImageByBase64
                .addImageByBase64(Path.of("src", "main", "resources", "images", "AboutMe.jpg"),
                        GptChatCompletionRequest.ImageDetail.AUTO)
                .execute();

        // Print model's response
        System.out.println("Model's answer:");
        System.out.println(response.assistantMessage());
    }
}
