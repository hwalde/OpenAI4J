package de.entwicklertraining.openai4j;

import org.json.JSONArray;
import org.json.JSONObject;

public final class GptToolDefinition {
    private final String name;
    private final String description;
    private final JSONObject parameters;
    private final GptToolsCallback callback;

    private GptToolDefinition(String name, String description, JSONObject parameters, GptToolsCallback callback) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.callback = callback;
    }

    public String name() { 
        return name; 
    }

    public String description() { 
        return description; 
    }

    public JSONObject parameters() { 
        return parameters; 
    }

    public GptToolsCallback callback() { 
        return callback; 
    }

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

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private final String name;
        private String description;
        private final JSONObject schema = new JSONObject();
        private final JSONObject properties = new JSONObject();
        private final JSONArray required = new JSONArray();
        private GptToolsCallback callback;
        private boolean areAdditionalPropertiesAllowed = false;

        private Builder(String name) {
            this.name = name;
            schema.put("type", "object");
        }

        public Builder description(String desc) {
            this.description = desc;
            return this;
        }

        public Builder parameter(String paramName, GptJsonSchema paramSchema, boolean requiredField) {
            properties.put(paramName, paramSchema.toJson());
            if (requiredField) {
                required.put(paramName);
            }
            return this;
        }

        public Builder callback(GptToolsCallback cb) {
            this.callback = cb;
            return this;
        }

        public GptToolDefinition build() {
            if (!properties.isEmpty()) {
                schema.put("properties", properties);
            }
            if (!required.isEmpty()) {
                schema.put("required", required);
            }
            schema.put("additionalProperties", areAdditionalPropertiesAllowed);
            return new GptToolDefinition(name, description, schema, callback);
        }

        public void allowAdditionalProperties() {
            this.areAdditionalPropertiesAllowed = true;
        }
    }
}
