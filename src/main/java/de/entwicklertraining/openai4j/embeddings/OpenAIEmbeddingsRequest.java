package de.entwicklertraining.openai4j.embeddings;

import de.entwicklertraining.openai4j.OpenAIClient;
import de.entwicklertraining.openai4j.OpenAIRequest;
import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a call to {@code POST /embeddings}.
 */
public final class OpenAIEmbeddingsRequest extends OpenAIRequest<OpenAIEmbeddingsResponse> {
    /* Pflichtfelder */
    private final EmbeddingModel model;
    private final List<Object> input; // String ODER Integer-Token-Arrays

    /* Optionale Felder */
    private final Integer dimensions;
    private final EmbeddingEncodingFormat encodingFormat;
    private final String user;

    private OpenAIEmbeddingsRequest(
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
    public OpenAIEmbeddingsResponse createResponse(String responseBody) {
        return new OpenAIEmbeddingsResponse(responseBody, this);
    }

    /* ---------- Builder ---------- */

    /**
     * Creates a new builder for an embeddings request.
     *
     * @param client client used to execute the request
     * @return builder instance
     */
    public static Builder builder(OpenAIClient client) {
        return new Builder(client);
    }

    public static final class Builder extends ApiRequestBuilderBase<Builder,OpenAIEmbeddingsRequest> {

        private final OpenAIClient client;
        private EmbeddingModel               model        = EmbeddingModel.TEXT_EMBEDDING_3_SMALL;
        private final List<Object>           input        = new ArrayList<>();
        private Integer                      dimensions;
        private EmbeddingEncodingFormat      encodingFormat;
        private String                       user;

        /**
         * Constructs a builder bound to the given client.
         */
        private Builder(OpenAIClient client) {
            this.client = client;
        }

        /**
         * Adds a single text input to embed.
         *
         * @param text input text
         * @return this builder
         */
        public Builder addInput(String text) {
            this.input.add(text);
            return this;
        }
        /**
         * Replaces the current input list with the given texts.
         *
         * @param texts list of texts to embed
         * @return this builder
         */
        public Builder input(List<String> texts) {
            this.input.clear();
            this.input.addAll(texts);
            return this;
        }
        /**
         * Adds a pre-tokenised input represented by raw token IDs.
         *
         * @param tokens list of token IDs
         * @return this builder
         */
        public Builder addInputTokens(List<Integer> tokens) {
            this.input.add(tokens);
            return this;
        }

        /**
         * Sets the embedding model to use.
         */
        public Builder model(EmbeddingModel m)              { this.model = m; return this; }
        /**
         * Specifies the number of dimensions for the returned vector.
         */
        public Builder dimensions(Integer dims)             { this.dimensions = dims; return this; }
        /**
         * Controls how the vector is encoded in the response.
         */
        public Builder encodingFormat(EmbeddingEncodingFormat f){ this.encodingFormat = f; return this; }
        /**
         * Arbitrary user identifier for tracing abuse.
         */
        public Builder user(String userId)                  { this.user = userId; return this; }

        @Override
        /**
         * Builds the immutable request instance.
         */
        public OpenAIEmbeddingsRequest build() {
            return new OpenAIEmbeddingsRequest(
                    this, model, input, dimensions, encodingFormat, user
            );
        }

        @Override
        /**
         * Executes the request applying exponential backoff on retryable errors.
         */
        public OpenAIEmbeddingsResponse executeWithExponentialBackoff() {
            return client.sendRequest(build());
        }

        @Override
        /**
         * Executes the request without retries.
         */
        public OpenAIEmbeddingsResponse execute() {
            return client.sendRequest(build());
        }
    }
}
