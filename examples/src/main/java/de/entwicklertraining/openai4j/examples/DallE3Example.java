package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.openai4j.images.generations.DallE3Request;
import de.entwicklertraining.openai4j.images.generations.DallE3Request.ImageQuality;
import de.entwicklertraining.openai4j.images.generations.DallE3Request.ImageSize;
import de.entwicklertraining.openai4j.images.generations.DallE3Request.ImageStyle;
import de.entwicklertraining.openai4j.images.generations.DallE3Request.ResponseFormat;
import de.entwicklertraining.openai4j.images.generations.DallE3Response;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.List;

/**
 * Demonstrates how to use DALL·E 3 with the builder approach.
 * <p>
 * In this example:
 *   - We generate 1 image (DALL·E 3 typically supports n=1).
 *   - Use "quality=hd" and "style=vivid" to see advanced features.
 *   - We demonstrate how "noMoreDetail" can be set (true or false).
 *   - We'll request a base64 response and open the resulting image.
 */
public class DallE3Example {

    public static void main(String[] args) throws Exception {
        // Build a DALL·E 3 request with prompt + size + responseFormat + n=1 + optional flags
        DallE3Response response = DallE3Request.builder()
                .prompt("A futuristic city floating in the sky, with neon lights")
                .size(ImageSize.SIZE_1024x1024)
                .responseFormat(ResponseFormat.B64_JSON)
                .n(1)
                .quality(ImageQuality.HD)
                .style(ImageStyle.VIVID)
                .noMoreDetail(false)  // set to true if you want the "I NEED to test how..." prefix
                .execute();

        // We'll get a single (or possibly more, but typically 1) image back in B64 format
        List<String> images = response.images();
        if (images.isEmpty()) {
            System.err.println("No images received from DALL·E 3!");
            return;
        }

        // Decode and open the single result
        String base64Data = images.get(0);
        byte[] decoded = Base64.getDecoder().decode(base64Data);

        File outFile = new File("dalle3_output.png");
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
