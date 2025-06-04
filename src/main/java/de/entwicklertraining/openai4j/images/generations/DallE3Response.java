package de.entwicklertraining.openai4j.images.generations;

import de.entwicklertraining.openai4j.OpenAIResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the results (image URLs or Base64) returned by DALL·E 3
 */
public final class DallE3Response extends OpenAIResponse<DallE3Request> {

    private final List<String> images = new ArrayList<>();

    /**
     * Parses the raw response returned by the API.
     *
     * @param rawBody JSON payload from the API
     * @param request originating request instance
     */
    public DallE3Response(String rawBody, DallE3Request request) {
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
            if (getRequest().responseFormat() == DallE3Request.ResponseFormat.URL) {
                images.add(entry.getString("url"));
            } else {
                images.add(entry.getString("b64_json"));
            }
        }
    }

    /**
     * Returns the generated images as URLs or base64 strings depending on the
     * request's {@link DallE3Request.ResponseFormat}.
     *
     * @return immutable list of image results
     */
    public List<String> images() {
        return Collections.unmodifiableList(images);
    }
}
