package de.entwicklertraining.openai4j.chat.completion;

import de.entwicklertraining.openai4j.*;
import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Represents a request to the OpenAI Chat Completions API.
 * Now supports "response_format" for Structured Outputs or JSON mode,
 * in addition to the function-calling "tools" mechanism.
 * <p>
 * Also supports:
 *  - parallelToolCalls: allow the model to call multiple tools in a single response
 *  - toolChoice to control how the model calls tools:
 *     * "auto"
 *     * "required"
 *     * "none"
 *     * or a specific tool to force.
 *  - audio: parameters for audio output (Added)
 * </p>
 */
public final class GptChatCompletionRequest extends GptRequest<GptChatCompletionResponse> {
    
    /**
     * Represents the service tier options for request processing.
     */
    public enum GptServiceTier {
        AUTO("auto"),
        DEFAULT("default"),
        FLEX("flex");

        private final String value;

        GptServiceTier(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * This enum represents the high-level tool choice strategies:
     *  - AUTO: the model automatically decides whether to call a tool
     *  - REQUIRED: the model is forced to call at least one tool
     *  - NONE: the model is forced NOT to call any tools
     */
    public enum ToolChoice {
        AUTO("auto"),
        REQUIRED("required"),
        NONE("none");

        private final String literal;

        ToolChoice(String literal) {
            this.literal = literal;
        }

        public String literal() {
            return literal;
        }
    }

    public enum ImageDetail {
        LOW("low"),
        HIGH("high"),
        AUTO("auto");

        private final String value;

        ImageDetail(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum StreamOption {
        INCLUDE_USAGE("include_usage");

        private final String value;

        StreamOption(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private final String model;
    private final List<JSONObject> messages;
    private final List<GptToolDefinition> tools;
    private final GptResponseFormat responseFormat;

    private final ToolChoice toolChoiceEnum;          // For "auto", "required", "none"
    private final GptToolDefinition forcedToolChoice; // For forcing a specific function

    private final Boolean parallelToolCalls;
    private final GptChatCompletionAudioParams audioParams; // Added field
    private final Double frequencyPenalty; // Added field for frequency_penalty
    private final Map<Integer, Integer> logitBias; // Added field for logit_bias
    private final Boolean logprobs; // Added field for logprobs
    private final Integer maxCompletionTokens; // Added field for max_completion_tokens
    private final Map<String, String> metadata; // Added field for metadata
    private final Set<GptOutputModality> modalities; // Added field for modalities
    private final Integer n; // Added field for n
    private final GptChatCompletionPredictionParams predictionParams; // Added field for prediction
    private final Double presencePenalty; // Added field for presence_penalty
    private final GptReasoningEffort reasoningEffort; // Added field for reasoning_effort
    private final Integer seed; // Added field for seed
    private final Set<StreamOption> streamOptions;
    private final Double topP;
    private final String user;
    private final boolean stream;
    private final Double temperature;
    private final GptServiceTier serviceTier;
    private final List<String> stopSequences; // Added field
    private final Integer topLogprobs; // Added field
    private final GptWebSearchOptions webSearchOptions; // Added field

    GptChatCompletionRequest(
            Builder builder,
            String model,
            List<JSONObject> messages,
            List<GptToolDefinition> tools,
            GptResponseFormat responseFormat,
            ToolChoice toolChoiceEnum,
            GptToolDefinition forcedToolChoice,
            Boolean parallelToolCalls,
            GptChatCompletionAudioParams audioParams, // Added parameter
            Double frequencyPenalty, // Added parameter
            Map<Integer, Integer> logitBias, // Added parameter
            Boolean logprobs, // Added parameter
            Integer maxCompletionTokens, // Added parameter
            Map<String, String> metadata, // Added parameter
            Set<GptOutputModality> modalities, // Added parameter
            Integer n, // Added parameter
            GptChatCompletionPredictionParams predictionParams, // Added parameter
            Double presencePenalty, // Added parameter
            GptReasoningEffort reasoningEffort, // Added parameter
            Integer seed, // Added parameter
            Set<StreamOption> streamOptions,
            Double topP,
            String user,
            boolean stream,
            Double temperature,
            GptServiceTier serviceTier,  // Add parameter
            List<String> stopSequences,  // Add parameter
            Integer topLogprobs,  // Add parameter
            GptWebSearchOptions webSearchOptions // Added parameter
    ) {
        super(builder); // default no specific max execution time
        this.model = model;
        this.messages = messages;
        this.tools = tools;
        this.responseFormat = responseFormat;
        this.toolChoiceEnum = toolChoiceEnum;
        this.forcedToolChoice = forcedToolChoice;
        this.parallelToolCalls = parallelToolCalls;
        this.audioParams = audioParams; // Added assignment
        this.frequencyPenalty = frequencyPenalty; // Added assignment
        this.logitBias = (logitBias != null) ? Map.copyOf(logitBias) : null; // Added assignment with defensive copy
        this.logprobs = logprobs; // Added assignment
        this.maxCompletionTokens = maxCompletionTokens; // Added assignment
        this.metadata = (metadata != null) ? Map.copyOf(metadata) : null; // Added assignment with defensive copy
        // Initialize modalities, defaulting to TEXT if null or empty
        if (modalities == null || modalities.isEmpty()) {
            this.modalities = Set.of(GptOutputModality.TEXT);
        } else {
            this.modalities = Set.copyOf(modalities); // Defensive copy
        }
        this.n = n; // Added assignment
        this.predictionParams = predictionParams; // Added assignment
        this.presencePenalty = presencePenalty; // Added assignment
        this.reasoningEffort = reasoningEffort; // Added assignment
        this.seed = seed; // Added assignment
        this.streamOptions = (streamOptions != null) ? Set.copyOf(streamOptions) : null;
        this.topP = topP;
        this.user = user;
        this.stream = stream;
        this.temperature = temperature;
        this.serviceTier = serviceTier;  // Add assignment
        this.stopSequences = stopSequences != null ? List.copyOf(stopSequences) : null;  // Add assignment
        this.topLogprobs = topLogprobs;  // Add assignment
        this.webSearchOptions = webSearchOptions; // Added assignment
    }

    public String model() {
        return model;
    }

    public List<JSONObject> messages() {
        return messages;
    }

    public List<GptToolDefinition> tools() {
        return tools;
    }

    public GptResponseFormat responseFormat() {
        return responseFormat;
    }

    /**
     * If non-null, this indicates we are forcing an approach:
     *  - AUTO => model decides
     *  - REQUIRED => must call a tool
     *  - NONE => no tool calls
     */
    public ToolChoice toolChoiceEnum() {
        return toolChoiceEnum;
    }

    /**
     * If non-null, indicates we are forcing the model to call exactly this specific tool.
     */
    public GptToolDefinition forcedTool() {
        return forcedToolChoice;
    }

    public Boolean parallelToolCalls() {
        return parallelToolCalls;
    }

    /**
     * Returns the parameters for audio output, if specified.
     */
    public GptChatCompletionAudioParams audioParams() { // Added getter
        return audioParams;
    }

    /**
     * Returns the frequency penalty value, if specified.
     */
    public Double frequencyPenalty() { // Added getter
        return frequencyPenalty;
    }

    /**
     * Returns the logit bias map, if specified.
     * Keys are token IDs, values are bias adjustments (-100 to 100).
     */
    public Map<Integer, Integer> logitBias() { // Added getter
        // Return an unmodifiable view to prevent external modification
        return (logitBias != null) ? Collections.unmodifiableMap(logitBias) : null;
    }

    /**
     * Returns whether log probabilities should be returned, if specified.
     */
    public Boolean logprobs() { // Added getter
        return logprobs;
    }

    public Double topP() {
        return topP;
    }

    /**
     * Returns the upper bound for the number of tokens that can be generated for a completion,
     * including both visible output tokens and internal reasoning tokens, if specified.
     * This parameter replaces the deprecated {@code max_tokens} parameter.
     * See <a href="https://platform.openai.com/docs/guides/reasoning">OpenAI Reasoning Tokens Documentation</a> for details.
     */
    public Integer maxCompletionTokens() { // Added getter
        return maxCompletionTokens;
    }

    /**
     * Returns the metadata map, if specified.
     * Keys have max length 64, values max length 512. Max 16 pairs.
     */
    public Map<String, String> metadata() { // Added getter
        // Return an unmodifiable view
        return (metadata != null) ? Collections.unmodifiableMap(metadata) : null;
    }

    /**
     * Returns the set of requested output modalities.
     * Defaults to a set containing only {@link GptOutputModality#TEXT}.
     */
    public Set<GptOutputModality> modalities() { // Added getter
        // Return an unmodifiable view
        return Collections.unmodifiableSet(modalities);
    }

    /**
     * Returns the number of chat completion choices to generate, if specified.
     */
    public Integer n() { // Added getter
        return n;
    }

    /**
     * Returns the prediction parameters, if specified.
     */
    public GptChatCompletionPredictionParams predictionParams() { // Added getter
        return predictionParams;
    }

    /**
     * Returns the presence penalty value, if specified.
     * Number between -2.0 and 2.0. Positive values penalize new tokens based on
     * whether they appear in the text so far, increasing the model's likelihood
     * to talk about new topics.
     */
    public Double presencePenalty() { // Added getter
        return presencePenalty;
    }

    /**
     * Returns the reasoning effort setting, if specified.
     * Applicable only for o-series models.
     */
    public GptReasoningEffort reasoningEffort() { // Added getter
        return reasoningEffort;
    }

    /**
     * Returns the seed value for deterministic sampling, if specified.
     */
    public Integer seed() {
        return seed;
    }

    public String user() {
        return user;
    }

    public boolean stream() {
        return stream;
    }

    public Double temperature() {
        return temperature;
    }

    public Set<StreamOption> streamOptions() {
        return streamOptions != null ? Collections.unmodifiableSet(streamOptions) : null;
    }

    public GptServiceTier serviceTier() {
        return serviceTier;
    }

    public List<String> stopSequences() {
        return stopSequences;
    }

    public Integer topLogprobs() {
        return topLogprobs;
    }

    /**
     * Returns the web search options, if specified.
     */
    public GptWebSearchOptions webSearchOptions() { // Added getter
        return webSearchOptions;
    }

    /**
     * Builds the JSON body for this request.
     */
    public JSONObject toJson() {
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("messages", new JSONArray(messages));

        if (!tools.isEmpty()) {
            JSONArray toolArr = new JSONArray();
            for (var t : tools) {
                toolArr.put(t.toJson());
            }
            body.put("tools", toolArr);
        }

        if (responseFormat != null) {
            body.put("response_format", responseFormat.toJson());
        }

        // If forcing a specific tool => set "tool_choice" to a function descriptor
        if (forcedToolChoice != null) {
            JSONObject forced = new JSONObject();
            forced.put("type", "function");
            JSONObject fn = new JSONObject();
            fn.put("name", forcedToolChoice.name());
            forced.put("function", fn);
            body.put("tool_choice", forced);
        }
        // Else if we have an enum choice => "auto", "required", or "none"
        else if (toolChoiceEnum != null) {
            body.put("tool_choice", toolChoiceEnum.literal());
        }

        // parallelToolCalls => true/false. If null, rely on server default
        if (parallelToolCalls != null) {
            body.put("parallel_tool_calls", parallelToolCalls);
        }

        // Add audio parameters if present
        if (audioParams != null) { // Added block
            JSONObject audioObj = new JSONObject();
            // Assuming SpeechVoice and SpeechResponseFormat have a getValue() method returning the string literal
            audioObj.put("voice", audioParams.getVoice().value());
            audioObj.put("format", audioParams.getFormat().value());
            body.put("audio", audioObj);
        }

        // Add frequency_penalty if present and not null
        if (frequencyPenalty != null) { // Added block
            body.put("frequency_penalty", frequencyPenalty);
        }

        // Add logit_bias if present and not null/empty
        if (logitBias != null && !logitBias.isEmpty()) { // Added block
            JSONObject biasObj = new JSONObject();
            for (Map.Entry<Integer, Integer> entry : logitBias.entrySet()) {
                // Key (token ID) must be a string in the JSON object
                biasObj.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            body.put("logit_bias", biasObj);
        }

        // Add logprobs if present and not null
        if (logprobs != null) { // Added block
            body.put("logprobs", logprobs);
        }

        // Add max_completion_tokens if present and not null
        if (maxCompletionTokens != null) { // Added block
            body.put("max_completion_tokens", maxCompletionTokens);
        }

        // Add metadata if present and not null/empty
        if (metadata != null && !metadata.isEmpty()) { // Added block
            body.put("metadata", new JSONObject(metadata));
        }

        // Add modalities if not null/empty and not just the default "text"
        if (modalities != null && !modalities.isEmpty() && !(modalities.size() == 1 && modalities.contains(GptOutputModality.TEXT))) {
            JSONArray modalitiesArray = new JSONArray();
            for (GptOutputModality modality : modalities) {
                modalitiesArray.put(modality.getValue()); // Use the enum's string value
            }
            body.put("modalities", modalitiesArray);
        }

        // Add n if present and not null and not the default (1)
        if (n != null && n != 1) { // Added block
            body.put("n", n);
        }

        // Add prediction parameters if present
        if (predictionParams != null) { // Added block
            body.put("prediction", predictionParams.toJson());
        }

        // Add presence_penalty if present and not null
        if (presencePenalty != null) { // Added block
            body.put("presence_penalty", presencePenalty);
        }

        // Add reasoning_effort if present, not null, and model is o-series
        if (reasoningEffort != null && model != null && model.startsWith("o")) { // Added block
             body.put("reasoning_effort", reasoningEffort.getValue()); // Use the enum's string value
        }

        // Add seed if present
        if (seed != null) {
            body.put("seed", seed);
        }

        // Add user if set
        if (user != null && !user.isEmpty()) {
            body.put("user", user);
        }

        // Add stream if true (default is false, so we only need to add if true)
        if (stream) {
            body.put("stream", true);
        }

        // Add stream_options if present and stream is true
        if (stream && streamOptions != null && !streamOptions.isEmpty()) {
            JSONObject streamOptionsObj = new JSONObject();
            for (StreamOption option : streamOptions) {
                streamOptionsObj.put(option.getValue(), true);
            }
            body.put("stream_options", streamOptionsObj);
        }

        // Add top_p if present
        if (topP != null) {
            body.put("top_p", topP);
        }

        // Add temperature if set and not default (1.0)
        if (temperature != null && temperature != 1.0) {
            body.put("temperature", temperature);
        }

        // Add service_tier if present and not AUTO (default)
        if (serviceTier != null && serviceTier != GptServiceTier.AUTO) {
            body.put("service_tier", serviceTier.getValue());
        }

        // Add stop sequences if present
        if (stopSequences != null && !stopSequences.isEmpty()) {
            // Check if model is o3 or o4-mini (these don't support stop parameter)
            String modelLower = model.toLowerCase(Locale.ROOT);
            if (!modelLower.equals("o3") && !modelLower.equals("o4-mini")) {
                // If single sequence, add as string
                if (stopSequences.size() == 1) {
                    body.put("stop", stopSequences.get(0));
                } else {
                    // If multiple sequences, add as array
                    body.put("stop", new JSONArray(stopSequences));
                }
            }
        }

        // Add top_logprobs if present
        if (topLogprobs != null) {
            // Ensure logprobs is set to true when top_logprobs is used
            if (logprobs == null || !logprobs) {
                throw new IllegalStateException("logprobs must be set to true when using top_logprobs");
            }
            body.put("top_logprobs", topLogprobs);
        }

        // Add web_search_options if present
        if (webSearchOptions != null) { // Added block
            body.put("web_search_options", webSearchOptions.toJson());
        }

        return body;
    }

    @Override
    public String getRelativeUrl() {
        return "/chat/completions";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    @Override
    public String getBody() {
        return toJson().toString();
    }

    @Override
    public GptChatCompletionResponse createResponse(String responseBody) {
        return new GptChatCompletionResponse(new JSONObject(responseBody), this);
    }

    public static Builder builder(GptClient client) {
        return new Builder(client);
    }

    public static final class Builder extends ApiRequestBuilderBase<Builder, GptChatCompletionRequest> {
        private final GptClient client;
        private String model;
        private final List<JSONObject> messages = new ArrayList<>();
        private final List<JSONObject> imageMessages = new ArrayList<>();
        private final List<GptToolDefinition> tools = new ArrayList<>();
        private GptResponseFormat responseFormat;
        private ToolChoice toolChoiceEnum;
        private GptToolDefinition forcedTool;
        private Boolean parallelToolCalls;
        private GptChatCompletionAudioParams audioParams; // Added field
        private Double frequencyPenalty; // Added field
        private Map<Integer, Integer> logitBias; // Added field
        private Boolean logprobs; // Added field
        private Integer maxCompletionTokens; // Added field
        private Map<String, String> metadata; // Added field
        private Set<GptOutputModality> modalities; // Added field
        private Integer n; // Added field
        private GptChatCompletionPredictionParams predictionParams; // Added field
        private Double presencePenalty; // Added field
        private GptReasoningEffort reasoningEffort; // Added field
        private Integer seed; // Added field
        private String user; // Added field
        private boolean stream;
        private Double temperature;
        private Set<StreamOption> streamOptions;
        private Double topP;
        private GptServiceTier serviceTier;
        private List<String> stopSequences; // Added field
        private Integer topLogprobs; // Added field
        private GptWebSearchOptions webSearchOptions; // Added field

        // For extension checks
        private static final Set<String> ALLOWED_EXTENSIONS =
                Set.of("jpg", "jpeg", "png", "gif", "webp");

        public Builder(GptClient client) {
            this.client = client;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder addSystemMessage(String content) {
            messages.add(new JSONObject().put("role", getSystemRoleForModel()).put("content", content));
            return this;
        }

        /**
         * Determines the correct role for system-level messages based on the model name.
         * If the model name starts with "o", returns "developer" (reasoning model).
         * Otherwise, returns "system" (non-reasoning model).
         */
        private String getSystemRoleForModel() {
            if (model != null && model.toLowerCase(Locale.ROOT).startsWith("o")) {
                return "developer";
            }
            return "system";
        }

        public Builder addUserMessage(String content) {
            messages.add(new JSONObject().put("role", "user").put("content", content));
            return this;
        }

        public Builder addAssistantMessage(String content) {
            messages.add(new JSONObject().put("role", "assistant").put("content", content));
            return this;
        }

        public Builder addTool(GptToolDefinition tool) {
            this.tools.add(tool);
            return this;
        }

        public Builder addAllMessages(List<JSONObject> msgs) {
            this.messages.addAll(msgs);
            return this;
        }

        public Builder addAllTools(List<GptToolDefinition> tls) {
            this.tools.addAll(tls);
            return this;
        }

        /**
         * Set the response format for structured outputs or JSON mode.
         */
        public Builder responseFormat(GptResponseFormat format) {
            this.responseFormat = format;
            return this;
        }

        /**
         * Let the model decide if it calls a tool: (AUTO), or require a call (REQUIRED),
         * or forbid calls (NONE).
         */
        public Builder toolChoice(ToolChoice choice) {
            if(this.forcedTool != null) {
                throw new IllegalArgumentException("You cannot set this.forcedTool as well as  this.toolChoiceEnum at the same time!");
            }
            this.toolChoiceEnum = choice;
            return this;
        }

        /**
         * Force the model to call exactly this specific tool.
         */
        public Builder toolChoice(GptToolDefinition specificTool) {
            if (this.toolChoiceEnum != null) {
                throw new IllegalArgumentException("You cannot set forcedTool as well as toolChoiceEnum at the same time!");
            }
            this.forcedTool = specificTool;
            return this;
        }

        /**
         * Whether or not to allow multiple tool calls in a single response.
         * If null, server defaults (usually true) will apply.
         */
        public Builder parallelToolCalls(Boolean value) {
            this.parallelToolCalls = value;
            return this;
        }

        /**
         * Sets the parameters for audio output.
         * Required when audio output is requested with `modalities: ["audio"]`.
         */
        public Builder audioParams(GptChatCompletionAudioParams params) { // Added method
            this.audioParams = params;
            return this;
        }

        /**
         * Sets the frequency penalty. Number between -2.0 and 2.0.
         * Positive values penalize new tokens based on their existing frequency
         * in the text so far, decreasing the model's likelihood to repeat the
         * same line verbatim.
         *
         * @param penalty the frequency penalty value, or null to use the default (0).
         * @throws IllegalArgumentException if the penalty is outside the range [-2.0, 2.0].
         */
        public Builder frequencyPenalty(Double penalty) { // Added method
            if (penalty != null && (penalty < -2.0 || penalty > 2.0)) {
                throw new IllegalArgumentException("frequency_penalty must be between -2.0 and 2.0, but was: " + penalty);
            }
            this.frequencyPenalty = penalty;
            return this;
        }

        /**
         * Sets the logit bias map. Modifies the likelihood of specified tokens
         * appearing in the completion.
         *
         * @param bias A map where keys are token IDs and values are bias adjustments
         *             from -100 to 100. Null or empty map uses default behavior.
         * @throws IllegalArgumentException if any bias value is outside the range [-100, 100].
         */
        public Builder logitBias(Map<Integer, Integer> bias) { // Added method
            if (bias != null) {
                for (Map.Entry<Integer, Integer> entry : bias.entrySet()) {
                    Integer value = entry.getValue();
                    if (value == null || value < -100 || value > 100) {
                        throw new IllegalArgumentException(
                                "logit_bias value for token ID " + entry.getKey() +
                                " must be between -100 and 100, but was: " + value);
                    }
                }
                // Store a mutable copy internally in the builder
                this.logitBias = new HashMap<>(bias);
            } else {
                this.logitBias = null;
            }
            return this;
        }

        /**
         * Sets whether to return log probabilities of the output tokens.
         * If true, returns the log probabilities of each output token returned
         * in the `content` of `message`.
         *
         * @param enable true to enable logprobs, false or null to disable (default).
         */
        public Builder logprobs(Boolean enable) { // Added method
            this.logprobs = enable;
            return this;
        }

        /**
         * Sets an upper bound for the number of tokens that can be generated for a completion.
         * This limit includes both visible output tokens and internal reasoning tokens.
         * This parameter replaces the deprecated `max_tokens` parameter.
         *
         * @param maxTokens The maximum number of tokens (output + reasoning) to generate.
         *                  Must be non-negative. Null uses the model's default limit.
         * @return This builder instance.
         * @throws IllegalArgumentException if maxTokens is negative.
         */
        public Builder maxCompletionTokens(Integer maxTokens) { // Added method
            if (maxTokens != null && maxTokens < 0) {
                throw new IllegalArgumentException("max_completion_tokens cannot be negative, but was: " + maxTokens);
            }
            this.maxCompletionTokens = maxTokens;
            return this;
        }

        /**
         * Sets the metadata map for the request.
         * Allows attaching key-value pairs (up to 16) for tracking or querying.
         *
         * @param data A map containing metadata. Keys max 64 chars, values max 512 chars.
         *             Maximum 16 key-value pairs allowed. Null clears metadata.
         * @throws IllegalArgumentException if validation rules are violated (size, key/value length).
         */
        public Builder metadata(Map<String, String> data) { // Added method
            if (data != null) {
                if (data.size() > 16) {
                    throw new IllegalArgumentException("Metadata map cannot exceed 16 key-value pairs, but got: " + data.size());
                }
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (key == null || key.length() > 64) {
                        throw new IllegalArgumentException("Metadata key cannot be null and must be <= 64 characters, but got: '" + key + "'");
                    }
                    if (value == null || value.length() > 512) {
                        throw new IllegalArgumentException("Metadata value cannot be null and must be <= 512 characters for key '" + key + "', but got: '" + value + "'");
                    }
                }
                // Store a mutable copy internally
                this.metadata = new HashMap<>(data);
            } else {
                this.metadata = null;
            }
            return this;
        }
    
        /**
         * Sets the desired output modalities for the completion.
         * If null or empty, defaults to `["text"]`.
         * If `AUDIO` is included, the `audioParams` must also be set.
         *
         * @param modes A set of {@link GptOutputModality} values.
         * @return This builder instance.
         * @throws IllegalArgumentException if the set is empty after defaulting logic.
         */
        public Builder modalities(Set<GptOutputModality> modes) { // Added method
            if (modes == null || modes.isEmpty()) {
                this.modalities = Set.of(GptOutputModality.TEXT); // Default to text
            } else {
                 // Ensure no null elements if provided as a mutable set
                if (modes.stream().anyMatch(Objects::isNull)) {
                    throw new IllegalArgumentException("Modalities set cannot contain null elements.");
                }
                this.modalities = new HashSet<>(modes); // Store mutable copy
            }
            // Should not happen with the default logic, but double-check
            if (this.modalities.isEmpty()) {
                 throw new IllegalArgumentException("Modalities set cannot be empty after defaulting.");
            }
            return this;
        }

        /**
         * Sets how many chat completion choices to generate for each input message.
         * Defaults to 1. Note that you will be charged based on the number of
         * generated tokens across all choices.
         *
         * @param count The number of choices to generate. Must be positive if not null.
         * @return This builder instance.
         * @throws IllegalArgumentException if count is non-positive.
         */
        public Builder n(Integer count) { // Added method
            if (count != null && count <= 0) {
                throw new IllegalArgumentException("Parameter 'n' must be positive, but was: " + count);
            }
            this.n = count;
            return this;
        }

        /**
         * Sets the prediction parameters for the request.
         * Used to provide predicted output to potentially speed up responses.
         *
         * @param params The prediction parameters object. Null clears the setting.
         * @return This builder instance.
         */
        public Builder predictionParams(GptChatCompletionPredictionParams params) { // Added method
            this.predictionParams = params;
            return this;
        }

        /**
         * Sets the presence penalty. Number between -2.0 and 2.0.
         * Positive values penalize new tokens based on whether they appear in the
         * text so far, increasing the model's likelihood to talk about new topics.
         *
         * @param penalty the presence penalty value, or null to use the default (0).
         * @throws IllegalArgumentException if the penalty is outside the range [-2.0, 2.0].
         */
        public Builder presencePenalty(Double penalty) { // Added method
            if (penalty != null && (penalty < -2.0 || penalty > 2.0)) {
                throw new IllegalArgumentException("presence_penalty must be between -2.0 and 2.0, but was: " + penalty);
            }
            this.presencePenalty = penalty;
            return this;
        }

        /**
         * Sets the reasoning effort for o-series models.
         * Constrains effort on reasoning. Reducing effort can result in faster responses
         * and fewer tokens used on reasoning.
         *
         * @param effort The desired reasoning effort (`LOW`, `MEDIUM`, `HIGH`), or null to use the default (`MEDIUM`).
         * @return This builder instance.
         */
        public Builder reasoningEffort(GptReasoningEffort effort) { // Added method
            this.reasoningEffort = effort;
            return this;
        }

        /**
         * Sets the seed for deterministic sampling.
         * If specified, the system will make a best effort to sample deterministically.
         * 
         * @param seedValue The seed value for deterministic sampling, or null to use default behavior
         * @return This builder instance
         */
        public Builder seed(Integer seedValue) {
            this.seed = seedValue;
            return this;
        }

        /**
         * A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
         * Max length is 64 characters.
         *
         * @param user A string identifying the end-user
         * @return This builder instance
         * @throws IllegalArgumentException if user is longer than 64 characters
         */
        public Builder user(String user) {
            if (user != null && user.length() > 64) {
                throw new IllegalArgumentException("user identifier must not exceed 64 characters");
            }
            this.user = user;
            return this;
        }

        /**
         * Adds an image via external URL. Validates supported file extensions:
         *  - png, jpg, jpeg, webp, gif
         * The content is appended in a new "user" message with a "content" array containing
         * a single item with {type="image_url", image_url={url=..., detail=...}}.
         */
        public Builder addImageByUrl(String url, ImageDetail detail) {
            Objects.requireNonNull(url, "url must not be null");
            Objects.requireNonNull(detail, "detail must not be null");

            String fileExt = extractExtension(url).toLowerCase(Locale.ROOT);
            if (!ALLOWED_EXTENSIONS.contains(fileExt)) {
                throw new IllegalArgumentException(
                        "Unsupported file extension: " + fileExt + ". Allowed: " + ALLOWED_EXTENSIONS
                );
            }

            // Build the content array
            JSONArray contentArray = new JSONArray();
            JSONObject imageObj = new JSONObject();
            imageObj.put("type", "image_url");

            // place the "url" and "detail" in a subobject
            JSONObject imageUrlObj = new JSONObject();
            imageUrlObj.put("url", url);
            imageUrlObj.put("detail", detail.getValue());
            imageObj.put("image_url", imageUrlObj);

            contentArray.put(imageObj);

            // Add as a user message
            JSONObject msg = new JSONObject()
                    .put("role", "user")
                    .put("content", contentArray);
            imageMessages.add(msg);

            return this;
        }

        /**
         * Reads the local file from filePath, base64-encodes it, and appends as a "user" message
         * containing a single "image_url" item with data URI "data:image/...;base64,...".
         * We also attach the "detail" parameter if provided.
         */
        public Builder addImageByBase64(Path filePath, ImageDetail detail) {
            Objects.requireNonNull(filePath, "filePath must not be null");
            Objects.requireNonNull(detail, "detail must not be null");
    
            String fileName = filePath.getFileName().toString().toLowerCase(Locale.ROOT);
            String ext = extractExtension(fileName);  // e.g. "jpg"
            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                throw new IllegalArgumentException(
                        "Unsupported file extension: " + ext + ". Allowed: " + ALLOWED_EXTENSIONS
                );
            }
    
            String mimeType = extensionToMime(ext);
    
            byte[] fileBytes;
            try {
                fileBytes = Files.readAllBytes(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file: " + filePath + " => " + e.getMessage(), e);
            }
            String base64Data = Base64.getEncoder().encodeToString(fileBytes);
            String dataUrl = "data:" + mimeType + ";base64," + base64Data;
    
            JSONArray contentArray = new JSONArray();
            JSONObject imageObj = new JSONObject();
            imageObj.put("type", "image_url");
    
            JSONObject imageUrlObj = new JSONObject();
            imageUrlObj.put("url", dataUrl);
            imageUrlObj.put("detail", detail.getValue());
            imageObj.put("image_url", imageUrlObj);
    
            contentArray.put(imageObj);
    
            JSONObject msg = new JSONObject()
                    .put("role", "user")
                    .put("content", contentArray);
            imageMessages.add(msg);
    
            return this;
        }
    
        /**
         * Adds an audio input to the next user message as a content part.
         * The audio is provided as a base64-encoded string and format (e.g., "wav", "mp3").
         * This will append a user message with a content array containing an input_audio part.
         *
         * @param base64Data The base64-encoded audio data.
         * @param format     The audio format (e.g., "wav", "mp3").
         * @return This builder instance.
         */
        public Builder addAudioByBase64(String base64Data, String format) {
            Objects.requireNonNull(base64Data, "base64Data must not be null");
            Objects.requireNonNull(format, "format must not be null");
    
            JSONArray contentArray = new JSONArray();
    
            // Optionally, allow mixing with text by adding a text part first (user can do this manually)
            // Here, we only add the audio part.
            JSONObject audioObj = new JSONObject();
            audioObj.put("type", "input_audio");
    
            JSONObject inputAudioObj = new JSONObject();
            inputAudioObj.put("data", base64Data);
            inputAudioObj.put("format", format);
    
            audioObj.put("input_audio", inputAudioObj);
    
            contentArray.put(audioObj);
    
            JSONObject msg = new JSONObject()
                    .put("role", "user")
                    .put("content", contentArray);
    
            messages.add(msg);
    
            return this;
        }

        private static String extractExtension(String path) {
            int dotIdx = path.lastIndexOf('.');
            if (dotIdx < 0) {
                return "";
            }
            String raw = path.substring(dotIdx + 1).toLowerCase(Locale.ROOT);
            // strip query params if any
            int qMark = raw.indexOf('?');
            return (qMark >= 0) ? raw.substring(0, qMark) : raw;
        }

        private static String extensionToMime(String ext) {
            return switch (ext) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                case "gif" -> "image/gif";
                case "webp" -> "image/webp";
                default -> throw new IllegalArgumentException("Unsupported extension (mime lookup) " + ext);
            };
        }

        /**
         * If set, partial message deltas will be sent as server-sent events.
         * Tokens will be sent as data-only server-sent events as they become available,
         * with the stream terminated by a data: [DONE] message.
         *
         * @param enableStream true to enable streaming, false to disable
         * @return This builder instance
         */
        public Builder stream(boolean enableStream) {
            this.stream = enableStream;
            return this;
        }

        /**
         * What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random,
         * while lower values like 0.2 will make it more focused and deterministic.
         *
         * We generally recommend altering this or top_p but not both.
         *
         * @param temperature Value between 0 and 2, or null for model default
         * @return This builder instance
         * @throws IllegalArgumentException if temperature is outside the valid range
         */
        public Builder temperature(Double temperature) {
            if (temperature != null && (temperature < 0 || temperature > 2)) {
                throw new IllegalArgumentException("Temperature must be between 0 and 2");
            }
            this.temperature = temperature;
            return this;
        }

        /**
         * Sets the stream options. Only applicable when stream is true.
         */
        public Builder streamOptions(Set<StreamOption> options) {
            this.streamOptions = options;
            return this;
        }

        /**
         * Sets the nucleus sampling parameter.
         */
        public Builder topP(Double topP) {
            if (topP != null && (topP < 0 || topP > 1)) {
                throw new IllegalArgumentException("top_p must be between 0 and 1");
            }
            this.topP = topP;
            return this;
        }

        /**
         * Sets the service tier for processing the request.
         * This parameter is relevant for customers subscribed to the scale tier service.
         * 
         * @param tier The service tier to use (AUTO, DEFAULT, or FLEX)
         * @return This builder instance
         */
        public Builder serviceTier(GptServiceTier tier) {
            this.serviceTier = tier;
            return this;
        }

        /**
         * Sets the sequences where the API will stop generating further tokens.
         * The returned text will not contain the stop sequence.
         * Maximum of 4 sequences allowed.
         * 
         * @param sequences List of stop sequences (1-4 sequences)
         * @return This builder instance
         * @throws IllegalArgumentException if list is empty or contains more than 4 sequences
         */
        public Builder stopSequences(List<String> sequences) {
            if (sequences != null) {
                if (sequences.isEmpty()) {
                    throw new IllegalArgumentException("Stop sequences list cannot be empty");
                }
                if (sequences.size() > 4) {
                    throw new IllegalArgumentException("Maximum of 4 stop sequences allowed");
                }
            }
            this.stopSequences = sequences;
            return this;
        }

        /**
         * Adds a single stop sequence where the API will stop generating further tokens.
         * The returned text will not contain the stop sequence.
         * Maximum of 4 sequences allowed.
         * 
         * @param sequence Stop sequence to add
         * @return This builder instance
         * @throws IllegalArgumentException if adding this sequence would exceed 4 sequences
         */
        public Builder addStopSequence(String sequence) {
            if (sequence == null) {
                throw new IllegalArgumentException("Stop sequence cannot be null");
            }
            if (this.stopSequences == null) {
                this.stopSequences = new ArrayList<>();
            }
            if (this.stopSequences.size() >= 4) {
                throw new IllegalArgumentException("Maximum of 4 stop sequences allowed");
            }
            this.stopSequences.add(sequence);
            return this;
        }

        /**
         * Sets the number of most likely tokens to return at each token position,
         * each with an associated log probability.
         * Requires logprobs to be set to true.
         * 
         * @param count Number of top logprobs (0-20), or null to disable
         * @return This builder instance
         * @throws IllegalArgumentException if count is outside valid range
         */
        public Builder topLogprobs(Integer count) {
            if (count != null && (count < 0 || count > 20)) {
                throw new IllegalArgumentException("top_logprobs must be between 0 and 20");
            }
            this.topLogprobs = count;
            return this;
        }

        /**
         * Sets the web search options for the request.
         * Note: This parameter is currently only supported by models like `gpt-4o-search-preview`
         * and `gpt-4o-mini-search-preview`.
         *
         * @param options The web search options object, or null to disable web search.
         * @return This builder instance.
         */
        public Builder webSearchOptions(GptWebSearchOptions options) { // Added method
            this.webSearchOptions = options;
            return this;
        }

        public GptChatCompletionRequest build() {
            // Validate: If AUDIO modality is requested, audioParams must be set.
            if (modalities != null && modalities.contains(GptOutputModality.AUDIO) && audioParams == null) {
                throw new IllegalArgumentException("audioParams must be set when modalities includes AUDIO.");
            }

            // Validate logprobs is true if top_logprobs is set
            if (topLogprobs != null && (logprobs == null || !logprobs)) {
                throw new IllegalStateException("logprobs must be set to true when using top_logprobs");
            }

            List<JSONObject> messagesCopy = new ArrayList<>(List.copyOf(messages));
            messagesCopy.addAll(imageMessages);
            return new GptChatCompletionRequest(
                    this,
                    model,
                    messagesCopy,
                    List.copyOf(tools),
                    responseFormat,
                    toolChoiceEnum,
                    forcedTool,
                    parallelToolCalls,
                    audioParams, // Added parameter
                    frequencyPenalty, // Added parameter
                    logitBias, // Added parameter
                    logprobs, // Added parameter
                    maxCompletionTokens, // Added parameter
                    metadata, // Added parameter
                    modalities, // Added parameter
                    n, // Added parameter
                    predictionParams, // Added parameter
                    presencePenalty, // Added parameter
                    reasoningEffort, // Added parameter
                    seed, // Added parameter
                    streamOptions,
                    topP,
                    user,
                    stream, // Add parameter
                    temperature, // Add parameter
                    serviceTier,  // Add parameter
                    stopSequences,  // Add parameter
                    topLogprobs,  // Add parameter
                    webSearchOptions // Added parameter
            );
        }

        @Override
        public GptChatCompletionResponse executeWithExponentialBackoff() {
            return new GptChatCompletionCallHandler(client).handleRequest(build(), true);
        }

        @Override
        public GptChatCompletionResponse execute() {
            return new GptChatCompletionCallHandler(client).handleRequest(build(), false);
        }
    }
}
