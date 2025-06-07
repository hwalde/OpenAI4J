# OpenAI4J - Audio Service

This optional module extends the core `openai4j` library with a helper
service for audio transcription. Large or unsupported audio files are
split into smaller chunks, transcribed with the Whisper model and then
recombined. The service can return plain text, SRT/VTT subtitles or a
verbose JSON structure with word and segment timestamps.

Add it alongside the main dependency:

```xml
<dependency>
    <groupId>de.entwicklertraining</groupId>
    <artifactId>openai4j-audio-service</artifactId>
    <version>1.1.0</version>
</dependency>
```

See the example in
[`../openai4j-examples/src/main/java/de/entwicklertraining/openai4j/examples/OpenAITranscribeAudioServiceExample.java`](../openai4j-examples/src/main/java/de/entwicklertraining/openai4j/examples/OpenAITranscribeAudioServiceExample.java)
for how to use the service.
