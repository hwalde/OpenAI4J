package de.entwicklertraining.openai4j.chat.completion;

import de.entwicklertraining.openai4j.*;
import de.entwicklertraining.api.base.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GptChatCompletionCallHandler extends the old GptToolCallHandler to additionally handle:
 *  - Parallel tool calls
 *  - Structured outputs (response_format)
 *  - Model "finish_reason" edge cases like "stop", "tool_calls", "length", "content_filter"
 *  - Refusals & parsed JSON (from Structured Outputs)
 *
 * Usage:
 *   1) Erstelle eine Instanz von GptChatCompletionCallHandler mit einem GptClient.
 *   2) Rufe handleRequest(...) mit einem GptChatCompletionRequest auf.
 *   3) Erhalte ein GptChatCompletionResponse zurück, welches entweder:
 *      - Keine Tool Calls mehr enthält => finale Antwort
 *      - Eine refusal oder partial response => handle entsprechend
 *      - Evtl. structured Output => entnehme über GptChatCompletionResponse#parsed() etc.
 */
public final class GptChatCompletionCallHandler {

    // We stop after 4 total loops to avoid infinite calling scenarios
    private static final int MAX_TURNS = 4;
    private final GptClient client;

    public GptChatCompletionCallHandler(GptClient client) {
        this.client = client;
    }

    /**
     * Hauptmethode: Schickt die Unterhaltung an GPT,
     * verarbeitet ggf. parallele Tool-Calls, structured outputs und kehrt
     * erst zurück, wenn ein finales Ergebnis (stop) oder ein Fehler bzw. refusal vorliegt.
     */
    public GptChatCompletionResponse handleRequest(GptChatCompletionRequest initialRequest, boolean useExponentialBackoff) {
        // Kopiere initiale Messages und Tools, da wir die Nachrichten sukzessive erweitern
        List<JSONObject> messages = new ArrayList<>(initialRequest.messages());
        Map<String, GptToolDefinition> toolMap = new HashMap<>();
        for (var t : initialRequest.tools()) {
            toolMap.put(t.name(), t);
        }

        GptChatCompletionRequest currentRequest = initialRequest;
        int turnCount = 0;

        // Hauptschleife: so lange, bis wir ein finales Ergebnis bekommen, max. 4 Durchläufe
        while (true) {
            turnCount++;
            if (turnCount > MAX_TURNS) {
                // max runs reached => throw an exception
                throw new ApiClient.ApiClientException("Exceeded maximum of " + MAX_TURNS + " GPT call iterations without final stop.");
            }

            // Anfrage an GPT
            GptChatCompletionResponse response;
            if (useExponentialBackoff) {
                response = client.sendRequestWithExponentialBackoff(currentRequest);
            } else {
                response = client.sendRequest(currentRequest);
            }

            // Prüfen auf API-Fehler in JSON
            if (response.getJson().has("error")) {
                throw new ApiClient.HTTP_400_RequestRejectedException(
                        "OpenAI API returned an error: " + response.getJson().toString()
                );
            }

            String finishReason = response.finishReason();

            // Prüfen, ob Refusal (Structured Outputs) => Dann brechen wir ab und geben das Response zurück
            if (response.hasRefusal()) {
                // Wir haben eine Verweigerung => finale Antwort, da GPT nicht weiterreden will
                return response;
            }

            // Versuchen, tool_calls zu extrahieren
            JSONArray toolCallsArray = null;
            try {
                toolCallsArray = response.getJson()
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getJSONArray("tool_calls");
            } catch (Exception ex) {
                // differentiate between no tool calls (meaning it's probably final) or a changed response structure
                // If there's no "tool_calls" key at all, it's probably final => we just do nothing here
                // but let's check if we are missing it in an unexpected way
                // We'll check if "finish_reason" is "tool_calls" yet no array => that might be unknown
                if ("tool_calls".equals(finishReason)) {
                    throw new ApiClient.ApiResponseUnusableException(
                            "GPT indicated it wants to call tools (finish_reason=tool_calls) but no 'tool_calls' array was found in the response JSON. Response: "
                                    + response.getJson().toString()
                    );
                }
                // else it's simply no tools => we'll handle that logic below
            }

            // Wir fügen die Assistant-Antwort (inkl. tool_calls) in den Nachrichtenverlauf an
            messages.add(response.getJson()
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message"));

            // Prüfen, ob es tool_calls gibt
            if (toolCallsArray == null || toolCallsArray.isEmpty()) {
                // Keine (weiteren) Tools => dieses Response ist final
                return response;
            }

            // => Falls wir Tool Calls haben, verarbeiten wir sie
            for (int i = 0; i < toolCallsArray.length(); i++) {
                JSONObject toolCallObj = toolCallsArray.getJSONObject(i);

                String toolName;
                JSONObject functionObj;
                try {
                    functionObj = toolCallObj.getJSONObject("function");
                    toolName = functionObj.optString("name", null);
                } catch (Exception e) {
                    throw new ApiClient.ApiResponseUnusableException(
                            "Missing or invalid 'function' object in tool call. " + toolCallObj.toString()
                    );
                }
                if (toolName == null || !toolMap.containsKey(toolName)) {
                    throw new ApiClient.ApiResponseUnusableException("Unknown or missing tool call name: " + toolName);
                }

                // Argumente parsen
                JSONObject args;
                try {
                    String argsString = functionObj.getString("arguments");
                    args = new JSONObject(argsString);
                } catch (Exception e) {
                    throw new ApiClient.ApiResponseUnusableException(
                            "Failed to parse arguments for tool call '" + toolName
                                    + "'. Error: " + e.getMessage() + "  ToolCallObj=" + toolCallObj.toString()
                    );
                }

                // Tool aufrufen
                GptToolDefinition toolDef = toolMap.get(toolName);
                GptToolResult toolResult = toolDef.callback().handle(new GptToolCallContext(args));

                // Tool-Response dem Messages-Verlauf anhängen => role=tool
                messages.add(new JSONObject()
                        .put("role", "tool")
                        .put("tool_call_id", toolCallObj.getString("id"))
                        .put("content", toolResult.content()));
            }

            // Nach Bearbeitung der Tools => bau einen neuen Request
            currentRequest = buildNextRequest(initialRequest, messages);
        }
    }

    /**
     * Hilfsmethode, um aus den gegebenen Messages und der initialen Request-Konfiguration
     * einen neuen GptChatCompletionRequest zu bauen, der fortlaufend gesendet werden kann.
     */
    private GptChatCompletionRequest buildNextRequest(GptChatCompletionRequest initialReq, List<org.json.JSONObject> messages) {
        var builder = GptChatCompletionRequest.builder(client)
                .model(initialReq.model())
                .maxExecutionTimeInSeconds(initialReq.getMaxExecutionTimeInSeconds())
                .setCancelSupplier(initialReq.getIsCanceledSupplier())
                .addAllMessages(messages)
                .addAllTools(initialReq.tools())
                .responseFormat(initialReq.responseFormat())
                .parallelToolCalls(initialReq.parallelToolCalls())
                .seed(initialReq.seed())
                .audioParams(initialReq.audioParams())
                .frequencyPenalty(initialReq.frequencyPenalty())
                .logitBias(initialReq.logitBias())
                .logprobs(initialReq.logprobs())
                .maxCompletionTokens(initialReq.maxCompletionTokens())
                .metadata(initialReq.metadata())
                .modalities(initialReq.modalities())
                .n(initialReq.n())
                .predictionParams(initialReq.predictionParams())
                .presencePenalty(initialReq.presencePenalty())
                .reasoningEffort(initialReq.reasoningEffort())
                .serviceTier(initialReq.serviceTier())
                .stopSequences(initialReq.stopSequences())
                .streamOptions(initialReq.streamOptions())
                .stream(initialReq.stream())
                .temperature(initialReq.temperature())
                .topLogprobs(initialReq.topLogprobs())
                .topP(initialReq.topP())
                .user(initialReq.user())
                .webSearchOptions(initialReq.webSearchOptions());

        // Übernehmen der captureOnSuccess / captureOnError
        if (initialReq.hasCaptureOnSuccess()) {
            builder.captureOnSuccess(initialReq.getCaptureOnSuccess());
        }
        if (initialReq.hasCaptureOnError()) {
            builder.captureOnError(initialReq.getCaptureOnError());
        }

        // toolChoice
        if (initialReq.forcedTool() != null) {
            builder.toolChoice(initialReq.forcedTool());
        } else if (initialReq.toolChoiceEnum() != null) {
            builder.toolChoice(initialReq.toolChoiceEnum());
        }

        return builder.build();
    }

}
