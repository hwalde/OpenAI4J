package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.api.base.ApiClientSettings;
import de.entwicklertraining.openai4j.GptClient;
import de.entwicklertraining.openai4j.images.generations.DallE2Request.ImageSize;
import de.entwicklertraining.openai4j.images.generations.DallE2Request.ResponseFormat;
import de.entwicklertraining.openai4j.images.generations.DallE2Response;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.List;

/**
 * Demonstrates how to use DALL·E 2 with the builder approach.
 * <p>
 * In this example:
 *   - We generate 2 images, in Base64 format
 *   - Then we decode them and open each as a local .png file
 *   - "n=2" means we generate 2 images
 */
public class DallE2Example {

    public static void main(String[] args) throws Exception {
        // Generate GptClient
        GptClient client = new GptClient();

        // Build a DALL·E 2 request with prompt + size + responseFormat + n
        // Then execute -> returns a DallE2Response
        DallE2Response response = client.images().generations().dalle2()
                .prompt("A surreal painting of a robotic flamingo on a unicycle.")
                .size(ImageSize.SIZE_512x512)
                .responseFormat(ResponseFormat.B64_JSON)
                .n(2)
                .execute();

        // The DallE2Response contains up to n images in either URL or base64 form
        List<String> images = response.images();

        // Let's decode them from base64, write them to disk, and open them
        // (If you used ResponseFormat.URL, you'd download each URL instead.)
        for (int i = 0; i < images.size(); i++) {
            String base64Data = images.get(i);
            byte[] decoded = Base64.getDecoder().decode(base64Data);

            File outFile = new File("dalle2_output_" + (i + 1) + ".png");
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                fos.write(decoded);
            }

            System.out.println("Saved " + outFile.getAbsolutePath());

            // Attempt to open the created file with the system default application
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(outFile);
            }
        }
    }
}
