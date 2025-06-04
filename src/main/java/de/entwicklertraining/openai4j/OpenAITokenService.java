package de.entwicklertraining.openai4j;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;

/**
 * Utility for counting tokens for OpenAI models.
 * <ul>
 *   <li>Uses the {@code o200k_base} encoding (e.g. for gpt&#8209;4o) by default.</li>
 *   <li>If tokenisation fails at runtime a safe approximation is used
 *       (roughly 3.6 characters per token, rounded up).</li>
 * </ul>
 */
public final class OpenAITokenService {

    private static final EncodingRegistry REGISTRY = Encodings.newDefaultEncodingRegistry();
    private static final Encoding O200K  = REGISTRY.getEncoding(EncodingType.O200K_BASE);
    private static final double AVG_CHARS_PER_TOKEN = 3.6; // leicht konservativ

    /**
     * Calculates the token count for the supplied text.
     *
     * @param text UTF‑8 input text
     * @return number of tokens (never negative)
     */
    public int calculateTokenCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        try {
            return O200K.encode(text).size();
        } catch (Exception e) {
            // Fallback: konservativ runden
            return (int) Math.ceil(text.length() / AVG_CHARS_PER_TOKEN);
        }
    }

    // optional: Utility für Chat-Nachrichten
    // public int calculateTokenCount(List<Message> messages, ModelType model) { ... }
}
