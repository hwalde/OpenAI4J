package de.entwicklertraining.openai4j;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;

/**
 * Exakte Tokenzählung für OpenAI-Modelle.
 *  - Nutzt O200K_BASE (z.B. für gpt-4o) als Default.
 *  - Fällt bei Laufzeitfehlern auf eine sichere Approximation zurück
 *    (3,6 Zeichen ≈ 1 Token) und rundet auf.
 */
public final class OpenAITokenService {

    private static final EncodingRegistry REGISTRY = Encodings.newDefaultEncodingRegistry();
    private static final Encoding O200K  = REGISTRY.getEncoding(EncodingType.O200K_BASE);
    private static final double AVG_CHARS_PER_TOKEN = 3.6; // leicht konservativ

    /**
     * Liefert die Token-Anzahl des übergebenen Textes.
     *
     * @param text Eingabetext (UTF-8)
     * @return Anzahl Tokens (niemals < 0)
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
