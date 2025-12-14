package de.entwicklertraining.openai4j;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.api.base.ApiClientSettings;
import de.entwicklertraining.api.base.ApiHttpConfiguration;
import de.entwicklertraining.openai4j.audio.speech.OpenAICreateSpeechRequest;
import de.entwicklertraining.openai4j.audio.transcriptions.OpenAICreateTranscriptionRequest;
import de.entwicklertraining.openai4j.audio.translations.OpenAICreateTranslationRequest;
import de.entwicklertraining.openai4j.chat.completion.OpenAIChatCompletionRequest;
import de.entwicklertraining.openai4j.embeddings.OpenAIEmbeddingsRequest;
import de.entwicklertraining.openai4j.images.generations.DallE2Request;
import de.entwicklertraining.openai4j.images.generations.DallE3Request;
import de.entwicklertraining.openai4j.images.generations.OpenAIImage1Request;

import java.util.function.ToIntFunction;

/**
 * Enhanced OpenAIClient with improved error handling and exponential backoff
 * for certain HTTP error codes (429, 500, 503).
 *
 * <p>The client automatically reads the API key from the OPENAI_API_KEY environment variable
 * if not explicitly provided via {@link ApiHttpConfiguration}.
 *
 * <p>Error behavior:
 * <ul>
 *   <li>400 -> throw ApiClient.HTTP_400_RequestRejectedException</li>
 *   <li>401 -> throw ApiClient.HTTP_401_AuthorizationException</li>
 *   <li>403 -> throw ApiClient.HTTP_403_PermissionDeniedException</li>
 *   <li>429 -> attempt exponential backoff; if still not resolved -> throw ApiClient.HTTP_429_RateLimitOrQuotaException</li>
 *   <li>500 -> attempt exponential backoff; if still not resolved -> throw ApiClient.HTTP_500_ServerErrorException</li>
 *   <li>503 -> attempt exponential backoff; if still not resolved -> throw ApiClient.HTTP_503_ServerUnavailableException</li>
 *   <li>else -> throw ApiClient.ApiClientException</li>
 * </ul>
 */
public final class OpenAIClient extends ApiClient {

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";

    /**
     * Creates a new OpenAIClient with default settings.
     * The API key is read from the OPENAI_API_KEY environment variable.
     */
    public OpenAIClient() {
        this(ApiClientSettings.builder().build(), null, DEFAULT_BASE_URL);
    }

    /**
     * Creates a new OpenAIClient with custom settings.
     * The API key is read from the OPENAI_API_KEY environment variable.
     *
     * @param settings Client settings for retry behavior and timeouts
     */
    public OpenAIClient(ApiClientSettings settings) {
        this(settings, null, DEFAULT_BASE_URL);
    }

    /**
     * Creates a new OpenAIClient with custom settings and HTTP configuration.
     *
     * @param settings Client settings for retry behavior and timeouts
     * @param httpConfig HTTP configuration including authentication headers
     */
    public OpenAIClient(ApiClientSettings settings, ApiHttpConfiguration httpConfig) {
        this(settings, httpConfig, DEFAULT_BASE_URL);
    }

    /**
     * Creates a new OpenAIClient with custom settings, HTTP configuration, and base URL.
     *
     * @param settings Client settings for retry behavior and timeouts
     * @param httpConfig HTTP configuration including authentication headers (can be null)
     * @param customBaseUrl Custom base URL for the API
     */
    public OpenAIClient(ApiClientSettings settings, ApiHttpConfiguration httpConfig, String customBaseUrl) {
        super(settings, buildHttpConfig(httpConfig));

        setBaseUrl(customBaseUrl);

        // Register status code exceptions
        registerStatusCodeException(400, HTTP_400_RequestRejectedException.class, "The OpenAI server could not understand the request due to invalid syntax", false);
        registerStatusCodeException(401, HTTP_401_AuthorizationException.class, "Authentication failed. Received 401 from OpenAI.", false);
        registerStatusCodeException(403, HTTP_403_PermissionDeniedException.class, "The request has been refused (probably due to some policy violation)", false);
        registerStatusCodeException(429, HTTP_429_RateLimitOrQuotaException.class, "Rate limit or quota exceeded.", true);
        registerStatusCodeException(500, HTTP_500_ServerErrorException.class, "HTTP 500 (internal server error)", true);
        registerStatusCodeException(503, HTTP_503_ServerUnavailableException.class, "HTTP 503 (service unavailable)", true);
    }

    /**
     * Builds the HTTP configuration, adding the API key from environment variable if not already set.
     */
    private static ApiHttpConfiguration buildHttpConfig(ApiHttpConfiguration existingConfig) {
        // Check if we already have an Authorization header
        if (existingConfig != null && existingConfig.getGlobalHeaders().containsKey("Authorization")) {
            return existingConfig;
        }

        // Try to get API key from environment variable
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            // No API key available - return existing config or empty config
            // The request will fail with 401, which is appropriate
            return existingConfig != null ? existingConfig : new ApiHttpConfiguration();
        }

        // Build new config with API key
        ApiHttpConfiguration.Builder builder = existingConfig != null
            ? existingConfig.toBuilder()
            : ApiHttpConfiguration.builder();

        return builder
            .header("Authorization", "Bearer " + apiKey)
            .build();
    }

    public OpenAIChat chat() {
        return new OpenAIChat(this);
    }

    public static class OpenAIChat {
        private final OpenAIClient client;

        public OpenAIChat(OpenAIClient client) {
            this.client = client;
        }

        public OpenAIChatCompletionRequest.Builder completion() {
            return OpenAIChatCompletionRequest.builder(client);
        }
    }

    public OpenAIAudio audio() {
        return new OpenAIAudio(this);
    }

    public static class OpenAIAudio {
        private final OpenAIClient client;

        public OpenAIAudio(OpenAIClient client) {
            this.client = client;
        }

        public OpenAICreateSpeechRequest.Builder speech() {
            return OpenAICreateSpeechRequest.builder(client);
        }

        public OpenAICreateTranscriptionRequest.Builder transcription() {
            return OpenAICreateTranscriptionRequest.builder(client);
        }

        public OpenAICreateTranslationRequest.Builder translation() {
            return OpenAICreateTranslationRequest.builder(client);
        }
    }

    public OpenAIImages images() {
        return new OpenAIImages(this);
    }

    public static class OpenAIImages {
        private final OpenAIClient client;

        public OpenAIImages(OpenAIClient client) {
            this.client = client;
        }

        public OpenAIImagesGenerates generations() {
            return new OpenAIImagesGenerates(client);
        }
    }

    public static class OpenAIImagesGenerates {
        private final OpenAIClient client;

        public OpenAIImagesGenerates(OpenAIClient client) {
            this.client = client;
        }

        public OpenAIImage1Request.Builder image1() {
            return OpenAIImage1Request.builder(client);
        }

        public DallE2Request.Builder dalle2() {
            return DallE2Request.builder(client);
        }

        public DallE3Request.Builder dalle3() {
            return DallE3Request.builder(client);
        }
    }

    public OpenAIEmbeddingsRequest.Builder embeddings() {
        return OpenAIEmbeddingsRequest.builder(this);
    }

    public ToIntFunction<String> tokenCounter() {
        return new OpenAITokenService()::calculateTokenCount;
    }
}
