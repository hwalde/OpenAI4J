package de.entwicklertraining.openai4j;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.openai4j.audio.speech.GptCreateSpeechRequest;
import de.entwicklertraining.openai4j.audio.transcriptions.GptCreateTranscriptionRequest;
import de.entwicklertraining.openai4j.audio.translations.GptCreateTranslationRequest;
import de.entwicklertraining.openai4j.chat.completion.GptChatCompletionRequest;
import de.entwicklertraining.openai4j.embeddings.GptEmbeddingsRequest;
import de.entwicklertraining.openai4j.images.generations.DallE2Request;
import de.entwicklertraining.openai4j.images.generations.DallE3Request;
import de.entwicklertraining.openai4j.images.generations.GptImage1Request;
import de.entwicklertraining.api.base.ApiClientSettings;

/**
 * Enhanced GptClient with improved error handling and exponential backoff
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
public final class GptClient extends ApiClient {
    public GptClient(ApiClientSettings settings) {
        this(settings, "https://api.openai.com/v1");
    }

    public GptClient(ApiClientSettings settings, String customBaseUrl) {
        super(settings);

        setBaseUrl(customBaseUrl);

        /* Fehler­code-Registrierungen wie bisher */
        registerStatusCodeException(400, HTTP_400_RequestRejectedException.class, "The openai4j server could not understand the request due to invalid syntax", false);
        registerStatusCodeException(401, HTTP_401_AuthorizationException.class, "Authentication failed. Received 401 from OpenAI.", false);
        registerStatusCodeException(403, HTTP_403_PermissionDeniedException.class, "The request has been refused (probably due to some policy violation)", false);
        registerStatusCodeException(429, HTTP_429_RateLimitOrQuotaException.class, "Rate limit or quota exceeded.", true);
        registerStatusCodeException(500, HTTP_500_ServerErrorException.class, "HTTP 500 (internal server error)", true);
        registerStatusCodeException(503, HTTP_503_ServerUnavailableException.class, "HTTP 503 (service unavailable)", true);
    }

    public GptChat chat() {
        return new GptChat(this);
    }

    public static class GptChat {
        private final GptClient client;

        public GptChat(GptClient client) {
            this.client = client;
        }

        public GptChatCompletionRequest.Builder completion() {
            return GptChatCompletionRequest.builder(client);
        }
    }

    public GptAudio audio() {
        return new GptAudio(this);
    }

    public static class GptAudio {
        private final GptClient client;

        public GptAudio(GptClient client) {
            this.client = client;
        }

        public GptCreateSpeechRequest.Builder speech() {
            return GptCreateSpeechRequest.builder(client);
        }

        public GptCreateTranscriptionRequest.Builder transcription() {
            return GptCreateTranscriptionRequest.builder(client);
        }

        public GptCreateTranslationRequest.Builder translation() {
            return GptCreateTranslationRequest.builder(client);
        }
    }

    public GptImages images() {
        return new GptImages(this);
    }

    public static class GptImages {
        private final GptClient client;

        public GptImages(GptClient client) {
            this.client = client;
        }

        public GptImagesGenerates generations() {
            return new GptImagesGenerates(client);
        }
    }

    public static class GptImagesGenerates {
        private final GptClient client;

        public GptImagesGenerates(GptClient client) {
            this.client = client;
        }

        public GptImage1Request.Builder image1() {
            return GptImage1Request.builder(client);
        }

        public DallE2Request.Builder dalle2() {
            return DallE2Request.builder(client);
        }

        public DallE3Request.Builder dalle3() {
            return DallE3Request.builder(client);
        }
    }

    public GptEmbeddingsRequest.Builder embeddings() {
        return GptEmbeddingsRequest.builder(this);
    }
}
