package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.openai4j.GptClient;
import de.entwicklertraining.openai4j.chat.completion.GptChatCompletionRequest;
import de.entwicklertraining.openai4j.chat.completion.GptChatCompletionResponse;

/**
 * Demonstrates how to use Vision with an external URL.
 * We pass an image URL with "detail=low" for a faster, low-res analysis.
 */
public class GptChatCompletionWithVisionUrlExample {

    public static void main(String[] args) {
        // Example: an external image URL
        String url = "https://software-quality-services.de/wp-content/uploads/2024/09/Walde_0141.jpg";

        // Generate GptClient
        GptClient client = new GptClient();

        GptChatCompletionResponse response = client.chat().completion()
                .model("gpt-4o-mini") // or a vision-enabled model
                .addSystemMessage("You are a helpful assistant that can see images.")
                .addUserMessage("What do you see in this image?")
                // Using the newly created method addImageByUrl
                .addImageByUrl(url, GptChatCompletionRequest.ImageDetail.LOW)
                .execute();

        System.out.println("Model's answer:");
        System.out.println(response.assistantMessage());
    }
}
