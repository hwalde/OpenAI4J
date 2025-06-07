Wichtig: Aktualisiere AGENTS.md nach jedem Task.
Wichtig: Aktualisiere die README.md nach jedem Task nur wenn die Informationen darin veraltet sind
# OpenAI4J

OpenAI4J is a fluent Java wrapper for the [OpenAI API](https://platform.openai.com/docs/api-reference).
It builds on top of the lightweight [`api-base`](https://github.com/hwalde/api-base) library which
handles HTTP communication, authentication and exponential backoff. The goal is to provide a type safe
and convenient way to access OpenAI services from modern Java (JDK&nbsp;21+).

## Features

* Chat Completions including tool calling, structured outputs and vision inputs
* Image generation with GPT-4o's `image-1`, DALL·E&nbsp;2 and DALL·E&nbsp;3
* Embeddings with helper for cosine similarity
* Speech synthesis (TTS), audio transcription and translation, plus a service to chunk and merge audio files
* Token counting utilities via `jtokkit`
* Fluent builder APIs for all requests
* Examples demonstrating each feature

## Installation

Add the dependency from Maven Central:

```xml
<dependency>
    <groupId>de.entwicklertraining</groupId>
    <artifactId>openai4j</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Basic Usage

Instantiate a `OpenAIClient` and use the builders exposed by its fluent API. The
[function calling example](examples/src/main/java/de/entwicklertraining/openai4j/examples/OpenAIChatCompletionWithFunctionCallingExample.java)
shows how tools can be defined and executed:

```java
OpenAIToolDefinition weatherFunction = OpenAIToolDefinition.builder("get_local_weather")
        .description("Get weather information for a location.")
        .parameter("location", OpenAIJsonSchema.stringSchema("Name of the city"), true)
        .callback(ctx -> {
            String loc = ctx.arguments().getString("location");
            return OpenAIToolResult.of("Sunny in " + loc + " with a high of 25°C.");
        })
        .build();

OpenAIClient client = new OpenAIClient(); // this will read the API key from the environment variable OPENAI_API_KEY

OpenAIChatCompletionResponse resp = client.chat().completion()
        .model("gpt-4o-mini")
        .addSystemMessage("You are a helpful assistant.")
        .addUserMessage("What's the weather in Berlin and the current time?")
        .addTool(weatherFunction)
        .execute();
System.out.println(resp.assistantMessage());
```
【F:examples/src/main/java/de/entwicklertraining/openai4j/examples/OpenAIChatCompletionWithFunctionCallingExample.java†L10-L44】

The [DALL·E&nbsp;3 example](examples/src/main/java/de/entwicklertraining/openai4j/examples/DallE3Example.java)
illustrates image generation:

```java
OpenAIClient client = new OpenAIClient();
DallE3Response response = client.images().generations().dalle3()
        .prompt("A futuristic city floating in the sky, with neon lights")
        .size(ImageSize.SIZE_1024x1024)
        .responseFormat(ResponseFormat.B64_JSON)
        .n(1)
        .quality(ImageQuality.HD)
        .style(ImageStyle.VIVID)
        .execute();
List<String> images = response.images();
```
【F:examples/src/main/java/de/entwicklertraining/openai4j/examples/DallE3Example.java†L26-L43】

See the `examples` module for more demonstrations (embeddings, speech, translation, web search
and vision).

## Project Structure

The library follows a clear structure:

* **`OpenAIClient`** – entry point for all API calls. Extends `ApiClient` from *api-base*
  and registers error handling. It exposes sub clients (`chat()`, `images()`, `audio()`, `embeddings()`).
* **Request/Response classes** – located in packages like
  `chat.completion`, `images.generations`, `audio.*`, `embeddings`.
  Each request extends `OpenAIRequest` and has an inner `Builder` that extends
  `ApiRequestBuilderBase` from *api-base*. Responses extend `OpenAIResponse`.
* **Tool calling** – defined via `OpenAIToolDefinition` and handled by
  `OpenAIToolsCallback` and `OpenAIToolCallContext`.
* **Structured outputs** – use `OpenAIJsonSchema` and `OpenAIResponseFormat`.
* **Token utilities** – `OpenAITokenService` counts tokens via `jtokkit`.
* **Audio services** – module `openai4j-audio-service` provides `OpenAITranscribeAudioService` for chunking files and merging SRT/VTT outputs.

The examples directory mirrors these packages and can be used as a quick start.

## Extending OpenAI4J

1. **Create a Request** – subclass `OpenAIRequest` and implement `getRelativeUrl`,
   `getHttpMethod`, `getBody` and `createResponse`. Provide a nested builder
   extending `ApiRequestBuilderBase`.
2. **Create a Response** – subclass `OpenAIResponse` and parse the JSON or binary
   payload returned by OpenAI.
3. **Expose a builder** – add a convenience method in `OpenAIClient` returning your
   new builder so users can call it fluently.

Thanks to *api-base*, sending the request is handled by calling
`client.sendRequest(request)` or by using the builder’s `execute()` method which
internally delegates to `sendRequest` with optional exponential backoff.
See [api-base’s Readme](https://github.com/hwalde/api-base) for details on available
settings like retries, timeouts or capture hooks.

## Building

This project uses Maven. Compile the library and run examples with:

```bash
mvn package
```

## License

OpenAI4J is distributed under the MIT License as defined in the project `pom.xml`.


## Additional Details

This Maven-based project targets JDK 21 and provides a fluent Java wrapper around the OpenAI REST API. The main module `openai4j` exposes builders for chat completions, image generation, embeddings and audio endpoints. The `openai4j-audio-service` submodule builds on top and offers `OpenAITranscribeAudioService` to handle chunking and merging of audio files. It also provides `AudioChunker`, `SrtService`, `VttService` and `OpenAIVerboseTranscriptionService` for working with chunked audio and subtitle formats. Example usages are located in `examples/src/main/java/de/entwicklertraining/openai4j/examples`.

Important packages include:
- `chat.completion` – classes like `OpenAIChatCompletionRequest` and `OpenAIChatCompletionResponse` implement the Chat Completions API.
- `images.generations` – includes request builders such as `DallE3Request` for image generation.
- `audio.*` – speech, transcription and translation requests.
- `embeddings` – utilities for embeddings and cosine similarity.
- `audio.service` – utilities in the `openai4j-audio-service` module for chunking audio and creating SRT/VTT outputs.

The project has no automated tests, but it can be compiled with `mvn package`. The examples module depends on `openai4j` and `openai4j-audio-service` and demonstrates features such as tool calling, DALL·E 3 image generation and speech synthesis.


## Maintenance
- Run `mvn package` to ensure the project builds. There are no tests.
- Keep this AGENTS.md file in sync with repository changes.
- Only update README.md if the information becomes outdated.

### Recent Changes
* Added missing Javadoc comments across the code base to address build warnings.
* Added an important notice at the beginning of README about supported OpenAI features.
* Expanded README with new examples including ApiClientSettings usage and embeddings.
* Introduced new submodule `openai4j-audio-service` with helpers for chunking audio and creating SRT/VTT files.
* Bumped project version to 1.1.0.
* Fixed broken Javadoc links in OpenAITranscribeAudioService.
* Documented the optional audio-service module and updated the README with an Optional Modules section.
* Added CHANGELOG summarizing version history.

Wichtig: Aktualisiere AGENTS.md nach jedem Task.
Wichtig: Aktualisiere die README.md nach jedem Task nur wenn die Informationen darin veraltet sind
