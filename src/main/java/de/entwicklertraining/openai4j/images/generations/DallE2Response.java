package de.entwicklertraining.openai4j.images.generations;

import de.entwicklertraining.openai4j.GptResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the results (image URLs or Base64) returned by DALL·E 2
 */
public final class DallE2Response extends GptResponse<DallE2Request> {

    private final List<String> images = new ArrayList<>();

    public DallE2Response(String rawBody, DallE2Request request) {
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
            if (getRequest().responseFormat() == DallE2Request.ResponseFormat.URL) {
                images.add(entry.getString("url"));
            } else {
                images.add(entry.getString("b64_json"));
            }
        }
    }

    /**
     * Returns the list of generated images.
     * If the request used ResponseFormat.URL, these are URLs.
     * If the request used ResponseFormat.B64_JSON, these are base64-encoded strings.
     */
    public List<String> images() {
        return Collections.unmodifiableList(images);
    }
}
