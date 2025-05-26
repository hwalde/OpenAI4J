package de.entwicklertraining.openai4j;

import org.json.JSONObject;

/**
 * Represents the "response_format" parameter for OpenAI Chat Completions.
 * <p>
 * <b>Supports both:</b>
 * <ul>
 *   <li><b>Structured Outputs (preferred):</b> <code>{ "type": "json_schema", "json_schema": { ... } }</code></li>
 *   <li><b>JSON Mode:</b> <code>{ "type": "json_object" }</code></li>
 * </ul>
 * <p>
 * <b>json_schema</b> is preferred for models that support it, as it ensures the model output matches your supplied JSON schema.
 * <p>
 * Example usage for structured outputs:
 * <pre>
 *   GptResponseFormat.forJsonSchema("Event", mySchema, true)
 * </pre>
 * <p>
 * If you specify "json_schema", you must provide:
 *   <ul>
 *     <li>a name for the schema (required by OpenAI)</li>
 *     <li>a {@link GptJsonSchema} for the schema property</li>
 *     <li>strict = true or false</li>
 *   </ul>
 * The "additionalProperties" of the schema must be false to meet Structured Outputs constraints,
 * and all properties must be required. Enums can be used for restricting string values.
 */
public final class GptResponseFormat {

    /**
     * "json_schema" or "json_object"
     * According to the OpenAI docs:
     * - "json_schema": for advanced, strict schema adherence (Structured Outputs).
     * - "json_object": to ensure valid JSON (JSON mode).
     */
    private final String type;

    /**
     * If using "json_schema", you must provide a required "name" for the schema,
     * as well as the actual schema definition.
     */
    private final String schemaName;  // required by OpenAI
    private final GptJsonSchema jsonSchema;

    /**
     * If true and "type" == "json_schema", the system will enforce the schema strictly,
     * disallowing missing or extra keys (the schema must have "additionalProperties": false).
     */
    private final boolean strictSchema;

    /**
     * Create a GptResponseFormat in "json_schema" mode with a given schema name, schema object, and strictness.
     */
    public static GptResponseFormat forJsonSchema(String schemaName, GptJsonSchema schema, boolean strictSchema) {
        return new GptResponseFormat("json_schema", schemaName, schema, strictSchema);
    }

    /**
     * Create a GptResponseFormat in JSON mode. The model will always return valid JSON,
     * but it won't necessarily conform to a specific schema. (Older approach, see docs.)
     */
    public static GptResponseFormat forJsonObject() {
        return new GptResponseFormat("json_object", null, null, false);
    }

    private GptResponseFormat(String type, String schemaName, GptJsonSchema jsonSchema, boolean strictSchema) {
        this.type = type;
        this.schemaName = schemaName;
        this.jsonSchema = jsonSchema;
        this.strictSchema = strictSchema;
    }

    /**
     * Returns the JSON object that can be attached to the request body under "response_format".
     */
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("type", type);

        if ("json_schema".equals(type)) {
            JSONObject schemaObj = new JSONObject();
            // According to OpenAI docs, we must supply "name" and "schema" keys, plus "strict".
            schemaObj.put("name", schemaName);
            schemaObj.put("strict", strictSchema);

            // embed the actual JSON schema from GptJsonSchema
            schemaObj.put("schema", jsonSchema.toJson());
            obj.put("json_schema", schemaObj);
        }

        return obj;
    }

    public String type() {
        return type;
    }

    public String schemaName() {
        return schemaName;
    }

    public GptJsonSchema schema() {
        return jsonSchema;
    }

    public boolean strictSchema() {
        return strictSchema;
    }
}
