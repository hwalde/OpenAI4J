package de.entwicklertraining.openai4j.audio.service;

import de.entwicklertraining.openai4j.OpenAIClient;
import de.entwicklertraining.openai4j.audio.transcriptions.*;
import org.apache.commons.io.FilenameUtils;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

/**
 * Singleton service for transcribing audio via OpenAI's Whisper-1 model,
 * automatically chunking large or unsupported audio files, then combining results.
 */
public final class OpenAITranscribeAudioService {

    private static OpenAITranscribeAudioService instance;
    private static OpenAIClient openai;

    /**
     * Max size threshold in MB above which we chunk.
     */
    private static final double MAX_SIZE_MB = 24.9;

    /**
     * AudioChunker compression settings for forced chunking.
     */
    private static final int BITRATE_KBPS = 128;
    private static final double SILENCE_DB = -70.0;

    /**
     * Supported extensions that do NOT require chunking if below MAX_SIZE_MB:
     * mp3, mp4, mpeg, mpga, m4a, ogg, webm
     */
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "mp3", "mp4", "mpeg", "mpga", "m4a", "ogg", "webm"
    );

    private OpenAITranscribeAudioService() {
        openai = new OpenAIClient();
        // private constructor for singleton
    }

    public static synchronized OpenAITranscribeAudioService getInstance() throws SQLException {
        if (instance == null) {
            instance = new OpenAITranscribeAudioService();
        }
        return instance;
    }

    /**
     * Transcribe audio to plain text (concatenating chunks with spaces).
     */
    public String transcribeAudioToText(Path filePath) {
        return transcribeAudioToText(filePath, new TranscriptionSettings());
    }

    /**
     * Transcribe audio to plain text (concatenating chunks with spaces),
     * with optional settings for temperature, language, etc.
     */
    public String transcribeAudioToText(Path filePath, TranscriptionSettings settings) {
        List<String> chunkOutputs = transcribeMultipleChunks(filePath, settings, TranscriptionResponseFormat.VERBOSE_JSON);
        return String.join(" ", chunkOutputs);
    }

    /**
     * Transcribe audio to SRT format, merging chunk results via {@link SrtService}.
     */
    public String transcribeAudioToSrt(Path filePath) {
        return transcribeAudioToSrt(filePath, new TranscriptionSettings());
    }

    /**
     * Transcribe audio to SRT format, merging chunk results via {@link SrtService}.
     */
    public String transcribeAudioToSrt(Path filePath, TranscriptionSettings settings) {
        List<String> chunkOutputs = transcribeMultipleChunks(filePath, settings, TranscriptionResponseFormat.SRT);
        // Merge them into a single SRT
        String[] array = chunkOutputs.toArray(new String[0]);
        return SrtService.getInstance().combine(array);
    }

    /**
     * Transcribe audio to VTT format, merging chunk results via {@link VttService}.
     */
    public String transcribeAudioToVtt(Path filePath) {
        return transcribeAudioToVtt(filePath, new TranscriptionSettings());
    }

    /**
     * Transcribe audio to VTT format, merging chunk results via {@link VttService}.
     */
    public String transcribeAudioToVtt(Path filePath, TranscriptionSettings settings) {
        List<String> chunkOutputs = transcribeMultipleChunks(filePath, settings, TranscriptionResponseFormat.VTT);
        String[] array = chunkOutputs.toArray(new String[0]);
        return VttService.getInstance().combine(array);
    }

    /**
     * Transcribe audio to a {@link VerboseTranscription}, merging chunk results
     * via {@link OpenAIVerboseTranscriptionService#combine(VerboseTranscription...)}.
     */
    public VerboseTranscription transcribeAudioToVerboseTranscription(Path filePath) {
        return transcribeAudioToVerboseTranscription(filePath, new TranscriptionSettings());
    }

    /**
     * Transcribe audio to a {@link VerboseTranscription}, merging chunk results
     * via {@link OpenAIVerboseTranscriptionService#combine(VerboseTranscription...)}.
     */
    public VerboseTranscription transcribeAudioToVerboseTranscription(Path filePath, TranscriptionSettings settings) {
        List<VerboseTranscription> transcriptions =
                transcribeMultipleChunksVerbose(filePath, settings, TranscriptionResponseFormat.VERBOSE_JSON);
        return OpenAIVerboseTranscriptionService.getInstance().combine(transcriptions.toArray(new VerboseTranscription[0]));
    }

    /**
     * Helper method to transcribe multiple chunks for string-based formats (TEXT, SRT, VTT).
     * Returns each chunk's transcription as a string in the given response format.
     */
    private List<String> transcribeMultipleChunks(Path filePath,
                                                  TranscriptionSettings settings,
                                                  TranscriptionResponseFormat respFormat) {
        List<Path> chunks = prepareChunksIfNeeded(filePath);
        List<String> results = new ArrayList<>();
        for (Path chunk : chunks) {
            String text = transcribeSingleChunkAsString(chunk, respFormat, settings);
            results.add(text == null ? "" : text);
        }
        cleanupChunks(chunks, filePath);
        return results;
    }

    /**
     * Helper method to transcribe multiple chunks for verbose_json format.
     * Returns each chunk's transcription as a {@link VerboseTranscription}.
     */
    private List<VerboseTranscription> transcribeMultipleChunksVerbose(Path filePath,
                                                                       TranscriptionSettings settings,
                                                                       TranscriptionResponseFormat respFormat) {
        List<Path> chunks = prepareChunksIfNeeded(filePath);
        List<VerboseTranscription> results = new ArrayList<>();
        for (Path chunk : chunks) {
            VerboseTranscription vt = transcribeSingleChunkAsVerboseTranscription(chunk, respFormat, settings);
            if (vt != null) {
                results.add(vt);
            }
        }
        cleanupChunks(chunks, filePath);
        return results;
    }

    /**
     * If the file has a supported extension AND is under 24.9 MB,
     * we transcribe it directly. Otherwise, we chunk.
     */
    private List<Path> prepareChunksIfNeeded(Path filePath) {
        try {
            String ext = FilenameUtils.getExtension(filePath.toString()).toLowerCase(Locale.ROOT);
            long sizeBytes = Files.size(filePath);
            double sizeMb = sizeBytes / (1024.0 * 1024.0);

            boolean extensionSupported = SUPPORTED_EXTENSIONS.contains(ext);
            if (extensionSupported && sizeMb <= MAX_SIZE_MB) {
                // no chunking required
                return Collections.singletonList(filePath);
            }

            // otherwise chunk
            Path tmpDir = createUniqueFilepath("chunks");
            Files.createDirectories(tmpDir);

            AudioChunker chunker = new AudioChunker(MAX_SIZE_MB, BITRATE_KBPS, SILENCE_DB);
            chunker.chunkAudio(filePath.toString(), tmpDir.toString());

            // collect all .mp3 from tmpDir
            List<Path> chunkPaths = new ArrayList<>();
            Files.list(tmpDir)
                    .filter(p -> p.getFileName().toString().endsWith(".mp3"))
                    .sorted() // ensure consistent order
                    .forEach(chunkPaths::add);
            return chunkPaths;
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new RuntimeException("Failed to prepare chunks for " + filePath, e);
        }
    }

    private Path createUniqueFilepath(String extension) {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path uniquePath;

        do {
            String uniqueFileName = UUID.randomUUID().toString() + "." + extension;
            uniquePath = tempDir.resolve(uniqueFileName);
        } while (Files.exists(uniquePath));

        return uniquePath;
    }

    /**
     * Cleanup temporary chunk files if chunking was used (i.e., if there are multiple).
     * If only one chunk and it's the original file, do nothing.
     */
    private void cleanupChunks(List<Path> chunks, Path original) {
        if (chunks.size() == 1 && chunks.get(0).equals(original)) {
            return; // no chunking took place
        }
        try {
            for (Path chunk : chunks) {
                Files.deleteIfExists(chunk);
            }
            if (!chunks.isEmpty()) {
                // the parent folder is presumably the chunk folder
                Files.deleteIfExists(chunks.get(0).getParent());
            }
        } catch (IOException ignored) {
            // best effort
        }
    }

    /**
     * Create a single chunk transcription request with the desired format, returning the raw string.
     */
    private String transcribeSingleChunkAsString(Path chunkPath,
                                                 TranscriptionResponseFormat respFormat,
                                                 TranscriptionSettings settings) {
        OpenAICreateTranscriptionResponse response = buildRequest(chunkPath, respFormat, settings).execute();

        // For text-based formats, the entire response is in getJson() or in .text()
        return response.text();
    }

    /**
     * Create a single chunk transcription request with the desired format,
     * returning a {@link VerboseTranscription} if the response is "verbose_json".
     */
    private VerboseTranscription transcribeSingleChunkAsVerboseTranscription(Path chunkPath,
                                                                             TranscriptionResponseFormat respFormat,
                                                                             TranscriptionSettings settings) {
        OpenAICreateTranscriptionResponse response = buildRequest(chunkPath, respFormat, settings).execute();

        // parse as VerboseTranscription
        return OpenAIVerboseTranscriptionService.getInstance()
                .convertJsonToObject(response.getJson());
    }

    /**
     * Builds a {@link OpenAICreateTranscriptionRequest} with the provided settings and format.
     */
    private OpenAICreateTranscriptionRequest.Builder buildRequest(Path audioFile,
                                                                  TranscriptionResponseFormat format,
                                                                  TranscriptionSettings settings) {


        OpenAICreateTranscriptionRequest.Builder builder = openai.audio().transcription()
                .file(audioFile)
                .responseFormat(format);

        // For gpt-4o-transcribe and gpt-4o-mini-transcribe, the only supported format is json.
        if(format.equals(TranscriptionResponseFormat.JSON)) {
            builder.model(SpeechToTextModel.GPT_4o_MINI_TRANSCRIBE);
        } else {
            builder.model(SpeechToTextModel.WHISPER_1);
        }

        if (settings.temperature != null) {
            builder.temperature(settings.temperature);
        }
        if (settings.language != null && !settings.language.isBlank()) {
            builder.language(settings.language);
        }
        if (settings.prompt != null && !settings.prompt.isBlank()) {
            builder.prompt(settings.prompt);
        }
        if (settings.timestampGranularities != null && !settings.timestampGranularities.isEmpty()) {
            builder.timestampGranularities(settings.timestampGranularities);
        }
        return builder;
    }

    /**
     * Holds optional transcription settings such as temperature, language, prompt, etc.
     */
    public static final class TranscriptionSettings {
        private Double temperature;
        private String language;
        private String prompt;
        private List<TimestampGranularity> timestampGranularities;

        public Double getTemperature() {
            return temperature;
        }
        public TranscriptionSettings setTemperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public String getLanguage() {
            return language;
        }
        public TranscriptionSettings setLanguage(String language) {
            this.language = language;
            return this;
        }

        public String getPrompt() {
            return prompt;
        }
        public TranscriptionSettings setPrompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public List<TimestampGranularity> getTimestampGranularities() {
            return timestampGranularities;
        }
        public TranscriptionSettings setTimestampGranularities(List<TimestampGranularity> timestampGranularities) {
            this.timestampGranularities = timestampGranularities;
            return this;
        }
    }
}
