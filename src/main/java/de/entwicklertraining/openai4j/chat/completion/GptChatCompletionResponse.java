package de.entwicklertraining.openai4j.chat.completion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.entwicklertraining.openai4j.GptResponse;
import de.entwicklertraining.api.base.ApiClient;
import org.json.JSONObject;

import java.util.Objects;
import java.util.List; // Added
import java.util.ArrayList; // Added
import java.util.Collections; // Added
import org.json.JSONArray; // Added

/**
 * Represents the raw JSON response from the OpenAI Chat Completions API
 * and helper methods to extract content, tool calls, and now structured outputs / refusals.
 */
public final class GptChatCompletionResponse extends GptResponse<GptChatCompletionRequest> {

    public GptChatCompletionResponse(JSONObject json, GptChatCompletionRequest request) {
        super(json, request);
    }

    /**
     * Returns the assistant message as a JSON object (for structured output).
     */
    public JSONObject parsed() {
        return new JSONObject(Objects.requireNonNull(assistantMessage()));
    }

    /**
     * Converts the assistant JSON to a given POJO class using Jackson.
     */
    public <T> T convertTo(Class<T> targetType) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(Objects.requireNonNull(assistantMessage()), targetType);
        } catch (JsonProcessingException e) {
            throw new ApiClient.ApiResponseUnusableException(
                    "Could not parse the assistant message to " + targetType.getSimpleName() + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Returns the first choice's assistant "content" if present, or null if absent.
     * Convenience method, prefer using choices().get(0).getMessage().getContent() for more control.
     */
    public String assistantMessage() {
        try {
            // Ensure choices exist and are not empty before accessing
            List<Choice> choicesList = getChoices();
            if (choicesList.isEmpty()) {
                return null;
            }
            Message firstMessage = choicesList.getFirst().getMessage();
            return (firstMessage != null) ? firstMessage.getContent() : null;
        } catch (Exception e) { // Catch broader exceptions during initial access
            // Log or handle the exception appropriately
            System.err.println("Error accessing assistant message: " + e.getMessage());
            return null;
        }
    }


    /**
     * Check if the top message indicates a refusal.
     * "refusal" is part of the "Structured Outputs" approach.
     * Convenience method, prefer using choices().get(0).getMessage().getRefusal() != null.
     */
    public boolean hasRefusal() {
         try {
            List<Choice> choicesList = getChoices();
            if (choicesList.isEmpty()) {
                return false;
            }
            Message firstMessage = choicesList.get(0).getMessage();
            return firstMessage != null && firstMessage.getRefusal() != null;
        } catch (Exception e) {
            System.err.println("Error checking for refusal: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns the "refusal" string if any, otherwise null.
     * Convenience method, prefer using choices().get(0).getMessage().getRefusal().
     */
    public String refusal() {
        try {
            List<Choice> choicesList = getChoices();
            if (choicesList.isEmpty()) {
                return null;
            }
            Message firstMessage = choicesList.getFirst().getMessage();
            return (firstMessage != null) ? firstMessage.getRefusal() : null;
        } catch (Exception e) {
            System.err.println("Error accessing refusal message: " + e.getMessage());
            return null;
        }
    }

    /**
     * If the model invoked tools (function calling style),
     * check if there is a "tool_calls" array in the message.
     * Convenience method, prefer checking choices().get(0).getMessage().getToolCalls().isEmpty().
     */
    public boolean hasToolCall() {
        try {
            List<Choice> choicesList = getChoices();
            if (choicesList.isEmpty()) {
                return false;
            }
            Message firstMessage = choicesList.getFirst().getMessage();
            // Check if message exists and tool_calls is not empty
            return firstMessage != null && !firstMessage.getToolCalls().isEmpty();
        } catch (Exception e) {
             System.err.println("Error checking for tool calls: " + e.getMessage());
            return false;
        }
    }

    /**
     * Return the "finish_reason" from the first choice.
     * It could be "stop", "tool_calls", "length", "content_filter", etc.
     * Convenience method, prefer using choices().get(0).getFinishReason().
     */
    public String finishReason() {
        try {
            List<Choice> choicesList = getChoices();
            if (choicesList.isEmpty()) {
                return null;
            }
            return choicesList.getFirst().getFinishReason();
        } catch (Exception e) {
            System.err.println("Error accessing finish reason: " + e.getMessage());
            return null;
        }
    }

    // --- Added Choices Method and Classes ---

    /**
     * Gets the list of chat completion choices.
     * @return A list of Choice objects, or an empty list if not present or invalid.
     */
    public List<Choice> getChoices() {
        JSONArray choicesArray = getJson().optJSONArray("choices");
        if (choicesArray == null) {
            return Collections.emptyList();
        }
        List<Choice> choices = new ArrayList<>();
        for (int i = 0; i < choicesArray.length(); i++) {
            JSONObject choiceJson = choicesArray.optJSONObject(i);
            if (choiceJson != null) {
                choices.add(new Choice(choiceJson));
            }
        }
        return choices;
    }

    /**
     * Represents a single choice in the chat completion response.
     */
    public static class Choice {
        private final JSONObject json;

        Choice(JSONObject json) {
            this.json = (json != null) ? json : new JSONObject();
        }

        public int getIndex() {
            return json.optInt("index", 0);
        }

        /**
         * Returns the reason the model stopped generating tokens.
         * This will be `stop` if the model hit a natural stop point or a provided stop sequence,
         * `length` if the maximum number of tokens specified in the request was reached,
         * `content_filter` if content was omitted due to a flag from our content filters,
         * `tool_calls` if the model called a tool, or `function_call` (deprecated) if the model called a function.
         * @return The finish reason string, or null if not present.
         */
        public String getFinishReason() {
            return json.optString("finish_reason", null);
        }

        /**
         * Gets the message generated by the model for this choice.
         * @return A Message object, or null if not present.
         */
        public Message getMessage() {
            JSONObject messageJson = json.optJSONObject("message");
            return (messageJson != null) ? new Message(messageJson) : null;
        }

        /**
         * Gets the log probability information for the choice, if requested.
         * @return A JSONObject representing logprobs, or null if not present or not requested.
         *         Note: The structure of logprobs can be complex. Returning raw JSON for flexibility.
         */
        public JSONObject getLogprobs() {
            // optJSONObject returns null if the key doesn't exist or the value is not a JSONObject (including JSON null).
            return json.optJSONObject("logprobs");
        }
    }

    /**
     * Represents a message within a choice, including content and potential tool calls.
     */
    public static class Message {
        private final JSONObject json;

        Message(JSONObject json) {
            this.json = (json != null) ? json : new JSONObject();
        }

        /**
         * Gets the role of the author of this message (e.g., "assistant").
         * @return The role string, or null if not present.
         */
        public String getRole() {
            return json.optString("role", null);
        }

        /**
         * Gets the content of the message.
         * @return The content string, or null if not present (e.g., if tool calls are made).
         */
        public String getContent() {
            // Use optString which returns empty string for null, check explicitly for JSON null.
            if (json.isNull("content")) {
                return null;
            }
            return json.optString("content", null); // Default to null if key missing or not string
        }

        /**
         * Gets the refusal message if the model chose not to generate content (structured output feature).
         * @return The refusal string, or null if not present or not applicable.
         */
        public String getRefusal() {
             if (json.isNull("refusal")) {
                return null;
            }
            return json.optString("refusal", null);
        }

        /**
         * Gets the list of tool calls generated by the model, if any.
         * @return A list of ToolCall objects, or an empty list if none.
         */
        public List<ToolCall> getToolCalls() {
            JSONArray toolCallsArray = json.optJSONArray("tool_calls");
            if (toolCallsArray == null) {
                return Collections.emptyList();
            }
            List<ToolCall> toolCalls = new ArrayList<>();
            for (int i = 0; i < toolCallsArray.length(); i++) {
                JSONObject toolCallJson = toolCallsArray.optJSONObject(i);
                if (toolCallJson != null) {
                    toolCalls.add(new ToolCall(toolCallJson));
                }
            }
            return toolCalls;
        }

        /**
         * Gets the deprecated function call generated by the model, if any.
         * Prefer using getToolCalls().
         * @return A FunctionCall object, or null if not present.
         */
        @Deprecated
        public FunctionCall getFunctionCall() {
            JSONObject functionCallJson = json.optJSONObject("function_call");
            return (functionCallJson != null) ? new FunctionCall(functionCallJson) : null;
        }

         /**
         * Gets the annotations associated with the message content, if any.
         * The structure of annotations is not strictly defined here, returning raw JSONArray.
         * @return A JSONArray of annotations, or null if not present.
         */
        public JSONArray getAnnotations() {
            return json.optJSONArray("annotations");
        }
    }

    /**
     * Represents a tool call requested by the model.
     */
    public static class ToolCall {
        private final JSONObject json;

        ToolCall(JSONObject json) {
            this.json = (json != null) ? json : new JSONObject();
        }

        /**
         * Gets the ID of the tool call.
         * @return The ID string, or null if not present.
         */
        public String getId() {
            return json.optString("id", null);
        }

        /**
         * Gets the type of the tool. Currently, only "function" is supported.
         * @return The type string (e.g., "function"), or null if not present.
         */
        public String getType() {
            return json.optString("type", null);
        }

        /**
         * Gets the details of the function called.
         * @return A FunctionCall object containing function details, or null if not present.
         */
        public FunctionCall getFunction() {
            JSONObject functionJson = json.optJSONObject("function");
            return (functionJson != null) ? new FunctionCall(functionJson) : null;
        }
    }

    /**
     * Represents the function details within a tool call or a deprecated function_call.
     */
    public static class FunctionCall {
        private final JSONObject json;

        FunctionCall(JSONObject json) {
            this.json = (json != null) ? json : new JSONObject();
        }

        /**
         * Gets the name of the function to call.
         * @return The function name string, or null if not present.
         */
        public String getName() {
            return json.optString("name", null);
        }

        /**
         * Gets the arguments to call the function with, as a JSON string.
         * Note: The model does not always generate valid JSON.
         * @return The arguments JSON string, or null if not present.
         */
        public String getArguments() {
            return json.optString("arguments", null);
        }

        /**
         * Convenience method to parse arguments into a JSONObject.
         * Handles potential parsing errors.
         * @return Arguments as JSONObject, or an empty JSONObject if parsing fails or args are null/empty.
         */
        public JSONObject getArgumentsAsJson() {
            String args = getArguments();
            if (args == null || args.isEmpty()) {
                return new JSONObject();
            }
            try {
                return new JSONObject(args);
            } catch (Exception e) {
                // Consider logging this error
                System.err.println("Warning: Could not parse function arguments as JSON: " + args + " (" + e.getMessage() + ")");
                return new JSONObject(); // Return empty object on failure
            }
        }
    }

    /**
     * Returns the usage object from the response, or null if not present.
     */
    private JSONObject getUsage() {
        return getJson().optJSONObject("usage");
    }

    /**
     * Returns the prompt_tokens_details object from usage, or null if not present.
     */
    private JSONObject getPromptTokensDetails() {
        JSONObject usage = getUsage();
        return usage != null ? usage.optJSONObject("prompt_tokens_details") : null;
    }

    /**
     * Returns the completion_tokens_details object from usage, or null if not present.
     */
    private JSONObject getCompletionTokensDetails() {
        JSONObject usage = getUsage();
        return usage != null ? usage.optJSONObject("completion_tokens_details") : null;
    }

    // Base usage fields
    public int getPromptTokens() {
        JSONObject usage = getUsage();
        return usage != null ? usage.optInt("prompt_tokens", 0) : 0;
    }

    public int getCompletionTokens() {
        JSONObject usage = getUsage();
        return usage != null ? usage.optInt("completion_tokens", 0) : 0;
    }

    public int getTotalTokens() {
        JSONObject usage = getUsage();
        return usage != null ? usage.optInt("total_tokens", 0) : 0;
    }

    // Prompt tokens details
    public int getCachedTokens() {
        JSONObject details = getPromptTokensDetails();
        return details != null ? details.optInt("cached_tokens", 0) : 0;
    }

    public int getPromptAudioTokens() {
        JSONObject details = getPromptTokensDetails();
        return details != null ? details.optInt("audio_tokens", 0) : 0;
    }

    // Completion tokens details
    public int getReasoningTokens() {
        JSONObject details = getCompletionTokensDetails();
        return details != null ? details.optInt("reasoning_tokens", 0) : 0;
    }

    public int getCompletionAudioTokens() {
        JSONObject details = getCompletionTokensDetails();
        return details != null ? details.optInt("audio_tokens", 0) : 0;
    }

    public int getAcceptedPredictionTokens() {
        JSONObject details = getCompletionTokensDetails();
        return details != null ? details.optInt("accepted_prediction_tokens", 0) : 0;
    }

    public int getRejectedPredictionTokens() {
        JSONObject details = getCompletionTokensDetails();
        return details != null ? details.optInt("rejected_prediction_tokens", 0) : 0;
    }

    /**
     * Throws exception if refusal present.
     */
    public void throwOnRefusal() {
        if (hasRefusal()) {
            throw new ApiClient.ApiResponseUnusableException("The model refused to comply: " + refusal());
        }
    }
}
