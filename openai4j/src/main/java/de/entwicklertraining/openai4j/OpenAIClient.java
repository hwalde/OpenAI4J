package de.entwicklertraining.openai4j;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.openai4j.audio.speech.OpenAICreateSpeechRequest;
import de.entwicklertraining.openai4j.audio.transcriptions.OpenAICreateTranscriptionRequest;
import de.entwicklertraining.openai4j.audio.translations.OpenAICreateTranslationRequest;
import de.entwicklertraining.openai4j.chat.completion.OpenAIChatCompletionRequest;
import de.entwicklertraining.openai4j.embeddings.OpenAIEmbeddingsRequest;
import de.entwicklertraining.openai4j.images.generations.DallE2Request;
import de.entwicklertraining.openai4j.images.generations.DallE3Request;
import de.entwicklertraining.openai4j.images.generations.OpenAIImage1Request;
import de.entwicklertraining.api.base.ApiClientSettings;

import java.util.Optional;

/**
 * Enhanced OpenAIClient with improved error handling and exponential backoff
 * for certain HTTP error codes (429, 500, 503).
 *
 * Error behavior:
 *   - 400 -> throw ApiClient.HTTP_400_RequestRejectedException
 *   - 401 -> throw ApiClient.HTTP_401_AuthorizationException
 *   - 403 -> throw ApiClient.HTTP_403_PermissionDeniedException
 *   - 429 -> attempt exponential backoff; if still not resolved after max tries -> throw ApiClient.HTTP_429_RateLimitOrQuotaException
 *   - 500 -> attempt exponential backoff; if still not resolved after max tries -> throw ApiClient.HTTP_500_ServerErrorException
 *   - 503 -> attempt exponential backoff; if still not resolved after max tries -> throw ApiClient.HTTP_503_ServerUnavailableException
 *   - else -> throw ApiClient.ApiClientException
 */
public final class OpenAIClient extends ApiClient {
    public OpenAIClient() {
        this(ApiClientSettings.builder().build(), "https://api.openai.com/v1");
    }

    public OpenAIClient(ApiClientSettings settings) {
        this(settings, "https://api.openai.com/v1");
    }

    public OpenAIClient(ApiClientSettings settings, String customBaseUrl) {
        super(settings);

        setBaseUrl(customBaseUrl);

        // if no API key is provided, try to read it from the environment variable
        if(settings.getBearerAuthenticationKey().isEmpty() && System.getenv("OPENAI_API_KEY")!=null) {
            this.settings = this.settings.toBuilder().setBearerAuthenticationKey(System.getenv("OPENAI_API_KEY")).build();
        }

        /* Fehler­code-Registrierungen wie bisher */
        registerStatusCodeException(400, HTTP_400_RequestRejectedException.class, "The openai4j server could not understand the request due to invalid syntax", false);
        registerStatusCodeException(401, HTTP_401_AuthorizationException.class, "Authentication failed. Received 401 from OpenAI.", false);
        registerStatusCodeException(403, HTTP_403_PermissionDeniedException.class, "The request has been refused (probably due to some policy violation)", false);
        registerStatusCodeException(429, HTTP_429_RateLimitOrQuotaException.class, "Rate limit or quota exceeded.", true);
        registerStatusCodeException(500, HTTP_500_ServerErrorException.class, "HTTP 500 (internal server error)", true);
        registerStatusCodeException(503, HTTP_503_ServerUnavailableException.class, "HTTP 503 (service unavailable)", true);
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
}
