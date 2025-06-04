package de.entwicklertraining.openai4j.images.generations;

import de.entwicklertraining.openai4j.GptResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the results (base64-encoded images) returned by GPT-Image-1
 */
public final class GptImage1Response extends GptResponse<GptImage1Request> {

    private final List<String> images = new ArrayList<>();

    public GptImage1Response(String rawBody, GptImage1Request request) {
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