package de.entwicklertraining.openai4j.embeddings;

import de.entwicklertraining.openai4j.GptClient;
import de.entwicklertraining.openai4j.GptRequest;
import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Repräsentiert einen Aufruf von <pre>POST /embeddings</pre>.
 */
public final class GptEmbeddingsRequest extends GptRequest<GptEmbeddingsResponse> {
    /* Pflichtfelder */
    private final EmbeddingModel model;
    private final List<Object> input; // String ODER Integer-Token-Arrays

    /* Optionale Felder */
    private final Integer dimensions;
    private final EmbeddingEncodingFormat encodingFormat;
    private final String user;

    private GptEmbeddingsRequest(
            Builder b,
            EmbeddingModel model,
            List<Object> input,
            Integer dimensions,
            EmbeddingEncodingFormat encodingFormat,
            String user
    ) {
        super(b);
        this.model          = Objects.requireNonNull(model, "model must not be null");
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("input must not be null/empty");
        }
        this.input          = List.copyOf(input);
        this.dimensions     = dimensions;
        this.encodingFormat = encodingFormat != null ? encodingFormat : EmbeddingEncodingFormat.FLOAT;
        this.user           = user;
    }

    /* ---------- overrides from ApiRequest ---------- */

    @Override
    public String getRelativeUrl() {
        return "/embeddings";
    }

    @Override public String getHttpMethod() { return "POST";  }

    public EmbeddingEncodingFormat encodingFormat() {
        return encodingFormat;
    }

    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        body.put("model", model.value());

        // input darf String, Array<String> oder Array<Token-IDs> sein
        if (input.size() == 1) {
            body.put("input", input.getFirst());
        } else {
            body.put("input", new JSONArray(input));
        }

        if (dimensions != null)     body.put("dimensions", dimensions);
        if (encodingFormat != null) body.put("encoding_format", encodingFormat.value());
        if (user != null)           body.put("user", user);

        return body.toString();
    }

    @Override
    public GptEmbeddingsResponse createResponse(String responseBody) {
        return new GptEmbeddingsResponse(responseBody, this);
    }

    /* ---------- Builder ---------- */

    public static Builder builder(GptClient client) {
        return new Builder(client);
    }

    public static final class Builder extends ApiRequestBuilderBase<Builder,GptEmbeddingsRequest> {

        private final GptClient client;
        private EmbeddingModel               model        = EmbeddingModel.TEXT_EMBEDDING_3_SMALL;
        private final List<Object>           input        = new ArrayList<>();
        private Integer                      dimensions;
        private EmbeddingEncodingFormat      encodingFormat;
        private String                       user;

        public Builder(GptClient client) {
            this.client = client;
        }

        /** Einzelnen Text hinzufügen */
        public Builder addInput(String text) {
            this.input.add(text);
            return this;
        }
        /** Mehrere Texte */
        public Builder input(List<String> texts) {
            this.input.clear();
            this.input.addAll(texts);
            return this;
        }
        /** Roh-Token-IDs hinzufügen (für bereits tokenisierte Eingaben) */
        public Builder addInputTokens(List<Integer> tokens) {
            this.input.add(tokens);
            return this;
        }

        public Builder model(EmbeddingModel m)              { this.model = m; return this; }
        public Builder dimensions(Integer dims)             { this.dimensions = dims; return this; }
        public Builder encodingFormat(EmbeddingEncodingFormat f){ this.encodingFormat = f; return this; }
        public Builder user(String userId)                  { this.user = userId; return this; }

        @Override
        public GptEmbeddingsRequest build() {
            return new GptEmbeddingsRequest(
                    this, model, input, dimensions, encodingFormat, user
            );
        }

        @Override
        public GptEmbeddingsResponse executeWithExponentialBackoff() {
            return client.sendRequest(build());
        }

        @Override
        public GptEmbeddingsResponse execute() {
            return client.sendRequest(build());
        }
    }
}
