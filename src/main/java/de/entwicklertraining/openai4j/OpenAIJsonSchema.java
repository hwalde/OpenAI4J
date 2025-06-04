package de.entwicklertraining.openai4j;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A sealed interface representing an OpenAI-compatible JSON Schema definition,
 * supporting object/array/string/enum/anyOf types and the ability to mark fields as required.
 *
 * This updated version removes the old Builder pattern. Instead, each static factory
 * method returns a mutable schema instance. You can chain methods (property, additionalProperties, etc.)
 * without calling a final "build()" method.
 *
 * Usage example:
 * <pre>
 * // Create an object schema:
 * OpenAIJsonSchema eventSchema = OpenAIJsonSchema.objectSchema()
 *     .property("name", OpenAIJsonSchema.stringSchema("The name of the event."), true)
 *     .property("date", OpenAIJsonSchema.stringSchema("The date of the event"), true)
 *     .property("participants", OpenAIJsonSchema.arraySchema(
 *         OpenAIJsonSchema.stringSchema("A participant's name.")
 *     ), true)
 *     .additionalProperties(false);
 * </pre>
 */
public sealed interface OpenAIJsonSchema permits OpenAIJsonSchemaImpl {

    /**
     * Produce the final JSON object describing this schema.
     */
    JSONObject toJson();

    /**
     * Add or override the schema's description, if any.
     */
    OpenAIJsonSchema description(String desc);

    /**
     * For object schemas, add a named property referencing another schema.
     *
     * @param name the property name
     * @param schema the schema of that property
     * @param requiredField if true, add this property name to the "required" array
     * @return this schema, for chaining
     */
    OpenAIJsonSchema property(String name, OpenAIJsonSchema schema, boolean requiredField);

    /**
     * For array schemas, set the items schema (what each element in the array should look like).
     */
    OpenAIJsonSchema items(OpenAIJsonSchema itemSchema);

    /**
     * For enum schemas, specify the possible string values.
     */
    OpenAIJsonSchema enumValues(String... values);

    /**
     * Mark whether additional properties are allowed in an object schema.
     *
     * By default, we set additionalProperties = false for structured outputs.
     */
    OpenAIJsonSchema additionalProperties(boolean allowed);

    // --- Static factory methods ---

    /**
     * Returns a new object schema (type="object").
     */
    static OpenAIJsonSchema objectSchema() {
        return new OpenAIJsonSchemaImpl("object");
    }

    /**
     * Returns a new string schema (type="string"), with an optional description.
     */
    static OpenAIJsonSchema stringSchema(String description) {
        OpenAIJsonSchemaImpl schema = new OpenAIJsonSchemaImpl("string");
        schema.description(description);
        return schema;
    }

    /**
     * Returns a new number schema (type="number"), with an optional description.
     */
    static OpenAIJsonSchema numberSchema(String description) {
        OpenAIJsonSchemaImpl schema = new OpenAIJsonSchemaImpl("number");
        schema.description(description);
        return schema;
    }

    /**
     * Returns a new boolean schema (type="boolean"), with an optional description.
     */
    static OpenAIJsonSchema booleanSchema(String description) {
        OpenAIJsonSchemaImpl schema = new OpenAIJsonSchemaImpl("boolean");
        schema.description(description);
        return schema;
    }

    /**
     * Returns a new integer schema (type="integer"), with an optional description.
     */
    static OpenAIJsonSchema integerSchema(String description) {
        OpenAIJsonSchemaImpl schema = new OpenAIJsonSchemaImpl("integer");
        schema.description(description);
        return schema;
    }

    /**
     * Returns a new array schema (type="array") with the specified item schema.
     */
    static OpenAIJsonSchema arraySchema(OpenAIJsonSchema itemsSchema) {
        OpenAIJsonSchemaImpl schema = new OpenAIJsonSchemaImpl("array");
        schema.items(itemsSchema);
        return schema;
    }

    /**
     * Returns a new string schema with an enum constraint.
     * The schema type is "string" and only the listed values are allowed.
     */
    static OpenAIJsonSchema enumSchema(String description, String... enumValues) {
        OpenAIJsonSchemaImpl schema = new OpenAIJsonSchemaImpl("string");
        schema.description(description);
        schema.enumValues(enumValues);
        return schema;
    }

    /**
     * Returns a new "anyOf" schema. The top-level "type" is null,
     * and we set anyOfMode to true. Then you can chain .anyOf(...) calls
     * or pass multiple variants. However, in many cases you'll just do:
     * <pre>
     * OpenAIJsonSchema.anyOf(
     *    OpenAIJsonSchema.objectSchema(). ...,
     *    OpenAIJsonSchema.objectSchema(). ...
     * );
     * </pre>
     * Then place this inside another property if needed.
     */
    static OpenAIJsonSchema anyOf(OpenAIJsonSchema... variants) {
        OpenAIJsonSchemaImpl schema = new OpenAIJsonSchemaImpl(null);
        schema.setAnyOfMode(true);
        for (OpenAIJsonSchema variant : variants) {
            schema.getAnyOfSchemas().put(variant.toJson());
        }
        return schema;
    }
}

/**
 * The default implementation of OpenAIJsonSchema, holding mutable state for
 * the final toJson() call. This allows a fluent usage pattern without requiring
 * an explicit "build()" call at the end.
 */
final class OpenAIJsonSchemaImpl implements OpenAIJsonSchema {

    private String type;           // "object", "string", etc. May be null if we're in anyOfMode
    private String description;    // optional field
    private final JSONObject properties;
    private final JSONArray required;
    private final JSONArray enumValues;
    private OpenAIJsonSchema itemsSchema;
    private final JSONArray anyOfSchemas;
    private boolean additionalProperties;
    private boolean anyOfMode;

    OpenAIJsonSchemaImpl(String type) {
        this.type = type;          // e.g. "object", "array", "string", or null
        this.properties = new JSONObject();
        this.required = new JSONArray();
        this.enumValues = new JSONArray();
        this.anyOfSchemas = new JSONArray();
        this.additionalProperties = false; // structured outputs often require false by default
        this.anyOfMode = false;
    }

    @Override
    public OpenAIJsonSchema description(String desc) {
        this.description = desc;
        return this;
    }

    @Override
    public OpenAIJsonSchema property(String name, OpenAIJsonSchema schema, boolean requiredField) {
        if (anyOfMode) {
            throw new IllegalStateException("Cannot add properties when building an anyOf schema directly. " +
                    "Wrap the anyOf in an object if needed.");
        }
        if (!"object".equals(type)) {
            throw new IllegalStateException("Can only add properties to an object schema.");
        }
        this.properties.put(name, schema.toJson());
        if (requiredField) {
            this.required.put(name);
        }
        return this;
    }

    @Override
    public OpenAIJsonSchema items(OpenAIJsonSchema itemSchema) {
        if (anyOfMode) {
            throw new IllegalStateException("Cannot set items in anyOf mode directly.");
        }
        if (!"array".equals(type)) {
            throw new IllegalStateException("items can only be defined for array schemas.");
        }
        this.itemsSchema = itemSchema;
        return this;
    }

    @Override
    public OpenAIJsonSchema enumValues(String... values) {
        if (anyOfMode) {
            throw new IllegalStateException("Cannot set enum values in anyOf mode.");
        }
        if (this.type == null || !"string".equals(this.type)) {
            throw new IllegalStateException("Enum currently supported only on string type schemas.");
        }
        for (String v : values) {
            this.enumValues.put(v);
        }
        return this;
    }

    @Override
    public OpenAIJsonSchema additionalProperties(boolean allowed) {
        this.additionalProperties = allowed;
        return this;
    }

    // For internal usage by the anyOf(...) static method
    void setAnyOfMode(boolean anyOfMode) {
        this.anyOfMode = anyOfMode;
    }

    // Expose the anyOfSchemas array for the anyOf(...) factory
    JSONArray getAnyOfSchemas() {
        return this.anyOfSchemas;
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();

        // If we're in anyOf mode:
        if (anyOfMode) {
            obj.put("anyOf", anyOfSchemas);
            // Description can still be placed if user wants
            if (description != null && !description.isBlank()) {
                obj.put("description", description);
            }
            return obj;
        }

        // Otherwise we have a "type" schema
        if (type != null) {
            obj.put("type", type);
        }
        if (properties.length() > 0) {
            obj.put("properties", properties);
        }
        if (required.length() > 0) {
            obj.put("required", required);
        }
        if (enumValues.length() > 0) {
            obj.put("enum", enumValues);
        }

        if ("array".equals(type) && itemsSchema != null) {
            obj.put("items", itemsSchema.toJson());
        }

        obj.put("additionalProperties", additionalProperties);

        if (description != null && !description.isBlank()) {
            obj.put("description", description);
        }

        return obj;
    }
}
