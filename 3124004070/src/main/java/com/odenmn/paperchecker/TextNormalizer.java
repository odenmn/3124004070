package com.odenmn.paperchecker;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Converts text into a stable sequence of letters and digits.
 */
final class TextNormalizer {

    String normalize(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }

        String canonical = Normalizer.normalize(text, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);
        StringBuilder normalized = new StringBuilder(canonical.length());
        canonical.codePoints()
                .filter(codePoint -> Character.isLetterOrDigit(codePoint))
                .forEach(normalized::appendCodePoint);
        return normalized.toString();
    }
}
