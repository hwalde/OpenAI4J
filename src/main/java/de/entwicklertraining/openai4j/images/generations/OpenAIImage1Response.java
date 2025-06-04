package de.entwicklertraining.openai4j.images.generations;

import de.entwicklertraining.openai4j.OpenAIResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the results (base64-encoded images) returned by GPT-Image-1
 */
public final class OpenAIImage1Response extends OpenAIResponse<OpenAIImage1Request> {

    private final List<String> images = new ArrayList<>();

    public OpenAIImage1Response(String rawBody, OpenAIImage1Request request) {
        super(new JSONObject(rawBody), request);
        parseImages();
    }

    private void parseImages() {
        JSONArray dataArr = json.optJSONArray("data");
        if (dataArr == null || dataArr.isEmpty()) {
            return;
        }
        for (int i = 0; i < dataArr.length(); i++) {
            JSONObject entry = dataArr.getJSONObject(i);
            images.add(entry.getString("b64_json"));
        }
    }

    public List<String> images() {
        return Collections.unmodifiableList(images);
    }
}