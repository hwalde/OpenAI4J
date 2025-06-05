package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.openai4j.OpenAIClient;
import de.entwicklertraining.openai4j.images.generations.OpenAIImage1Request;
import de.entwicklertraining.openai4j.images.generations.OpenAIImage1Request.*;
import de.entwicklertraining.openai4j.images.generations.OpenAIImage1Response;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.List;

/**
 * Demonstrates how to use GPT-Image-1 with the builder approach.
 * <p>
 * In this example:
 *   - We generate 1 image using the gpt-image-1 model.
 *   - We set prompt, size, quality, output format, and other options.
 *   - We request a PNG image in base64 format and open the resulting image.
 */
public class OpenAIImage1Example {

    public static void main(String[] args) {
        try {
            // Generate OpenAIClient
            OpenAIClient client = new OpenAIClient();

            // Build a GPT-Image-1 request with prompt + size + outputFormat + n=1 + optional flags
            OpenAIImage1Response response = client.images().generations().image1()
                    .prompt("A photorealistic portrait of a cyberpunk cat with neon fur")
                    .size(ImageSize.SIZE_1024x1024)
                    .quality(ImageQuality.HIGH)
                    .outputFormat(OutputFormat.PNG)
                    .background(Background.OPAQUE)
                    .moderation(Moderation.AUTO)
                    .n(1)
                    .execute();

            // We'll get a single (or possibly more, but typically 1) image back in B64 format
            List<String> images = response.images();
            if (images.isEmpty()) {
                System.err.println("No images received from GPT-Image-1!");
                return;
            }

            // Decode and open the single result
            String base64Data = images.get(0);
            byte[] decoded = Base64.getDecoder().decode(base64Data);

            File outFile = new File("gptimage1_output.png");
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                fos.write(decoded);
            }

            System.out.println("Saved " + outFile.getAbsolutePath());

            // Attempt to open the created file with the system default application
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(outFile);
            }
        } catch (Exception e) {
            System.err.println("Error during GPT-Image-1 image generation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}