package de.entwicklertraining.openai4j;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Definition of a function-style tool that can be registered with the chat
 * completion endpoint.  Each tool exposes a JSON schema describing its
 * parameters and provides a callback that implements the actual functionality.
 */
public final class OpenAIToolDefinition {
    private final String name;
    private final String description;
    private final JSONObject parameters;
    private final OpenAIToolsCallback callback;

    /**
     * Constructs a tool definition from the given parts. The constructor is
     * package-private so callers have to go through {@link Builder} which
     * validates the schema and required fields.
     *
     * @param name        unique tool name as presented to the API
     * @param description short human readable description
     * @param parameters  JSON schema describing the accepted parameters
     * @param callback    implementation that will be invoked when the tool is used
     */
    private OpenAIToolDefinition(String name, String description, JSONObject parameters, OpenAIToolsCallback callback) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.callback = callback;
    }

    /**
     * Name of the tool as exposed to the OpenAI API.
     */
    public String name() {
        return name;
    }

    /**
     * Short human readable description of what the tool does.
     */
    public String description() {
        return description;
    }

    /**
     * JSON schema describing the parameters the tool accepts.
     */
    public JSONObject parameters() {
        return parameters;
    }

    /**
     * Callback that will be invoked when the tool is called by the model.
     */
    public OpenAIToolsCallback callback() {
        return callback;
    }

    /**
     * Serialises this tool definition to the JSON structure expected by the
     * OpenAI API.
     */
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", "function");
        JSONObject function = new JSONObject();
        function.put("name", name);
        function.put("description", description);
        function.put("parameters", parameters);
        function.put("strict", true);
        tool.put("function", function);
        return tool;
    }

    /**
     * Creates a builder for a new tool definition.
     *
     * @param name the unique tool name as referenced by OpenAI
     * @return a new builder instance
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Fluent builder for {@link OpenAIToolDefinition} instances.
     */
    public static final class Builder {
        private final String name;
        private String description;
        private final JSONObject schema = new JSONObject();
        private final JSONObject properties = new JSONObject();
        private final JSONArray required = new JSONArray();
        private OpenAIToolsCallback callback;
        private boolean areAdditionalPropertiesAllowed = false;

        /**
         * Creates a new builder bound to the supplied tool name.
         * Users normally obtain instances via {@link #builder(String)}.
         *
         * @param name unique name of the tool
         */
        private Builder(String name) {
            this.name = name;
            schema.put("type", "object");
        }

        /**
         * Sets the human readable description of the tool.
         */
        public Builder description(String desc) {
            this.description = desc;
            return this;
        }

        /**
         * Adds a parameter to the tool schema.
         *
         * @param paramName       the parameter name as used in JSON
         * @param paramSchema     JSON schema describing the value
         * @param requiredField   whether the parameter is mandatory
         * @return this builder for chaining
         */
        public Builder parameter(String paramName, OpenAIJsonSchema paramSchema, boolean requiredField) {
            properties.put(paramName, paramSchema.toJson());
            if (requiredField) {
                required.put(paramName);
            }
            return this;
        }

        /**
         * Registers the callback that is executed when the tool is invoked.
         */
        public Builder callback(OpenAIToolsCallback cb) {
            this.callback = cb;
            return this;
        }

        /**
         * Builds the immutable tool definition.
         */
        public OpenAIToolDefinition build() {
            if (!properties.isEmpty()) {
                schema.put("properties", properties);
            }
            if (!required.isEmpty()) {
                schema.put("required", required);
            }
            schema.put("additionalProperties", areAdditionalPropertiesAllowed);
            return new OpenAIToolDefinition(name, description, schema, callback);
        }

        /**
         * Allows additional properties beyond those defined in the schema to be
         * passed to the tool.
         */
        public void allowAdditionalProperties() {
            this.areAdditionalPropertiesAllowed = true;
        }
    }
}
