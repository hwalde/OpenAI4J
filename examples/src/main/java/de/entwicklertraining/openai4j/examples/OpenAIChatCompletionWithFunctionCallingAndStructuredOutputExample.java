package de.entwicklertraining.openai4j.examples;

import de.entwicklertraining.openai4j.*;
import de.entwicklertraining.openai4j.chat.completion.OpenAIChatCompletionResponse;
import org.json.JSONObject;

import java.util.List;

/**
 * Beispiel, das sowohl mehrere Tools (Function Calls) verwendet
 * als auch ein "response_format" (Structured Outputs) verlangt.
 *
 * Der User fragt: "Gib mir das Wetter und buche mir einen Termin."
 * GPT kann zwei Tools verwenden:
 *   1) fetchWeather(location)
 *   2) scheduleMeeting(topic, date, time, participants)
 *
 * Anschließend soll GPT das finale Ergebnis in einem JSON-Schema 
 * bereitstellen, welches sowohl "weather" als auch "meeting" enthält.
 * Strikte Einhaltung des Schemas (strict = true).
 *
 * Ablauf:
 *   - GPT fragt evtl. ab, welche Stadt und welche Meeting-Infos.
 *   - Es ruft fetchWeather(...) und scheduleMeeting(...) auf (ggf. parallel).
 *   - Wir liefern die Tool-Ergebnisse zurück.
 *   - Abschließend liefert GPT ein JSON, das dem definierten Schema entspricht.
 */
public class OpenAIChatCompletionWithFunctionCallingAndStructuredOutputExample {

    // Datenstruktur fürs finale JSON via Structured Outputs
    public static record CombinedOutput(Weather weather, Meeting meeting) {}

    public static record Weather(String city, String condition, String temperature) {}
    public static record Meeting(String topic, String date, String time, List<String> participants) {}

    public static void main(String[] args) throws Exception {
        // Tools definieren:

        // 1) Wetter-Funktion
        OpenAIToolDefinition weatherTool = OpenAIToolDefinition.builder("fetchWeather")
                .description("Fetch the weather for a given city.")
                // Removed ".build()" calls on OpenAIJsonSchema
                .parameter(
                        "location",
                        OpenAIJsonSchema.stringSchema("The city to fetch the weather for"),
                        true
                )
                .callback(context -> {
                    String location = context.arguments().getString("location");
                    // Hier simulieren wir ein Wetter-Ergebnis
                    String resultJson = new JSONObject()
                            .put("city", location)
                            .put("condition", "Sunny")
                            .put("temperature", "23°C")
                            .toString();
                    return OpenAIToolResult.of(resultJson);
                })
                .build();

        // 2) Meeting-Funktion
        OpenAIToolDefinition meetingTool = OpenAIToolDefinition.builder("scheduleMeeting")
                .description("Schedule a meeting with a topic, date, time, and participants.")
                // Removed ".build()" calls on OpenAIJsonSchema
                .parameter(
                        "topic",
                        OpenAIJsonSchema.stringSchema("Short title or topic of the meeting"),
                        true
                )
                .parameter(
                        "date",
                        OpenAIJsonSchema.stringSchema("Meeting date in YYYY-MM-DD format"),
                        true
                )
                .parameter(
                        "time",
                        OpenAIJsonSchema.stringSchema("Time of the meeting, e.g. '15:00'"),
                        true
                )
                .parameter(
                        "participants",
                        OpenAIJsonSchema.arraySchema(
                                OpenAIJsonSchema.stringSchema("Participant name")
                        ),
                        true
                )
                .callback(context -> {
                    String topic = context.arguments().getString("topic");
                    String date = context.arguments().getString("date");
                    String time = context.arguments().getString("time");
                    // participants ist ein Array
                    List<Object> parts = context.arguments().getJSONArray("participants").toList();

                    // Simuliere, dass wir irgendein Kalender-System aufrufen ...
                    JSONObject meetingJson = new JSONObject()
                            .put("topic", topic)
                            .put("date", date)
                            .put("time", time)
                            .put("participants", parts); // beibehalten als JSON-Array

                    return OpenAIToolResult.of(meetingJson.toString());
                })
                .build();

        // Schema für das finale JSON definieren (structured output):
        // "weather" und "meeting" sind beides Required-Felder (Objekte),
        // jeweils ohne additionalProperties.
        // Removed the ".build()" calls on property expansions
        OpenAIJsonSchema weatherSchema = OpenAIJsonSchema.objectSchema()
                .property("city", OpenAIJsonSchema.stringSchema("Name of city"), true)
                .property("condition", OpenAIJsonSchema.stringSchema("Weather condition"), true)
                .property("temperature", OpenAIJsonSchema.stringSchema("Temperature (e.g. '23°C')"), true)
                .additionalProperties(false);

        OpenAIJsonSchema meetingSchema = OpenAIJsonSchema.objectSchema()
                .property("topic", OpenAIJsonSchema.stringSchema("Meeting topic"), true)
                .property("date", OpenAIJsonSchema.stringSchema("Meeting date"), true)
                .property("time", OpenAIJsonSchema.stringSchema("Meeting time"), true)
                .property("participants",
                        OpenAIJsonSchema.arraySchema(
                                OpenAIJsonSchema.stringSchema("Participant name")
                        ),
                        true
                )
                .additionalProperties(false);

        // Removed final ".build()" call for combined schema
        OpenAIJsonSchema combinedSchema = OpenAIJsonSchema.objectSchema()
                .property("weather", weatherSchema, true)
                .property("meeting", meetingSchema, true)
                .additionalProperties(false);

        // response_format mit "json_schema", strict
        OpenAIResponseFormat respFormat = OpenAIResponseFormat.forJsonSchema(
                "CombinedSchema",
                combinedSchema,
                true
        );

        // Generate OpenAIClient
        OpenAIClient client = new OpenAIClient();

        // Baue die Anfrage
        OpenAIChatCompletionResponse finalResponse = client.chat().completion()
                .model("gpt-4o-mini")  // z.B. GPT-4o der das "json_schema" Format unterstützt
                .responseFormat(respFormat)
                .addSystemMessage("You are an assistant that can fetch the weather and schedule a meeting, " +
                        "and then output a final JSON matching the combined schema. " +
                        "Use the provided tools if the user requests them.")
                .addUserMessage("I'd like to know today's weather in Berlin and schedule a meeting " +
                        "about 'Brainstorming' for tomorrow at 09:00 with Alice and Bob.")
                .addTool(weatherTool)
                .addTool(meetingTool)
                .parallelToolCalls(true)  // Erlaube parallele Aufrufe
                .execute();

        // Prüfung auf Refusal (z.B. "I cannot comply with that request.")
        if (finalResponse.hasRefusal()) {
            System.err.println("Model refused to comply: " + finalResponse.refusal());
            return;
        }

        // Wir erwarten ein valides JSON nach unserem Schema
        // => Parsen und ins CombinedOutput mappen
        CombinedOutput result = finalResponse.convertTo(CombinedOutput.class);

        // Ausgabe
        System.out.println("===== FINAL RESULT =====");
        System.out.println("Weather => city: " + result.weather.city()
                + ", condition: " + result.weather.condition()
                + ", temperature: " + result.weather.temperature());
        System.out.println("Meeting => topic: " + result.meeting.topic()
                + ", date: " + result.meeting.date()
                + ", time: " + result.meeting.time()
                + ", participants: " + result.meeting.participants());
    }
}
