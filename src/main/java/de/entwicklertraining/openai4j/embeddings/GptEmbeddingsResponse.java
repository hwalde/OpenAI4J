package de.entwicklertraining.openai4j.embeddings;

import de.entwicklertraining.openai4j.GptResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parsed-Antwort auf /v1/embeddings.
 *
 * Bei encoding_format = "float" enthält {@link #embeddingsFloat()} eine Liste von double-Arrays.
 * Bei "base64" enthält {@link #embeddingsBase64()} die gleichen Vektoren in Base-64-Kodierung.
 */
public final class GptEmbeddingsResponse extends GptResponse<GptEmbeddingsRequest> {

    private final List<double[]> embeddingsFloat = new ArrayList<>();
    private final List<String>   embeddingsB64   = new ArrayList<>();
    private final int            promptTokens;
    private final int            totalTokens;
    private final String         model;

    public GptEmbeddingsResponse(String rawBody, GptEmbeddingsRequest request) {
        super(new JSONObject(rawBody), request);

        this.model = json.optString("model", null);

        // usage
        JSONObject usage = json.optJSONObject("usage");
        this.promptTokens = usage != null ? usage.optInt("prompt_tokens", 0) : 0;
        this.totalTokens  = usage != null ? usage.optInt("total_tokens", 0)  : 0;

        // parse embeddings
        JSONArray data = json.getJSONArray("data");
        boolean floats = request.encodingFormat() == null ||
                         request.encodingFormat() == EmbeddingEncodingFormat.FLOAT;

        for (int i = 0; i < data.length(); i++) {
            JSONObject entry = data.getJSONObject(i);
            if (floats) {
                JSONArray arr = entry.getJSONArray("embedding");
                double[] vec = new double[arr.length()];
                for (int j = 0; j < arr.length(); j++) {
                    vec[j] = arr.getDouble(j);
                }
                embeddingsFloat.add(vec);
            } else {
                embeddingsB64.add(entry.getString("embedding"));
            }
        }
    }

    /* -------- Zugriffsmethoden -------- */

    public List<double[]> embeddingsFloat() {
        return Collections.unmodifiableList(embeddingsFloat);
    }
    public List<String> embeddingsBase64() {
        return Collections.unmodifiableList(embeddingsB64);
    }

    public int promptTokens() { return promptTokens; }
    public int totalTokens()  { return totalTokens;  }
    public String model()     { return model;        }

    /* Convenience: nur erstes Ergebnis zurückgeben */
    public double[] firstEmbedding() {
        if (embeddingsFloat.isEmpty()) throw new IllegalStateException("No float embeddings in response.");
        return embeddingsFloat.getFirst();
    }
}
