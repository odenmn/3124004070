package com.odenmn.paperchecker;

import java.util.HashMap;
import java.util.Map;

/**
 * Calculates cosine similarity from character N-gram frequency vectors.
 */
public final class NGramCosineSimilarity {

    private static final int DEFAULT_N_GRAM_SIZE = 2;
    private final TextNormalizer normalizer;

    /**
     * Creates a bigram-based calculator.
     */
    public NGramCosineSimilarity() {
        this(new TextNormalizer());
    }

    NGramCosineSimilarity(TextNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    /**
     * Returns a value in the inclusive range from zero to one.
     *
     * @param original original paper text
     * @param suspicious suspicious paper text
     * @return cosine similarity
     */
    public double calculate(String original, String suspicious) {
        String normalizedOriginal = normalizer.normalize(original);
        String normalizedSuspicious = normalizer.normalize(suspicious);

        if (normalizedOriginal.isEmpty() || normalizedSuspicious.isEmpty()) {
            return normalizedOriginal.equals(normalizedSuspicious) ? 1.0 : 0.0;
        }
        if (normalizedOriginal.equals(normalizedSuspicious)) {
            return 1.0;
        }

        int nGramSize = Math.min(DEFAULT_N_GRAM_SIZE,
                Math.min(normalizedOriginal.codePointCount(
                                0, normalizedOriginal.length()),
                        normalizedSuspicious.codePointCount(
                                0, normalizedSuspicious.length())));
        Map<String, Integer> originalFrequencies = frequencies(
                normalizedOriginal, nGramSize);
        Map<String, Integer> suspiciousFrequencies = frequencies(
                normalizedSuspicious, nGramSize);

        double dotProduct = dotProduct(
                originalFrequencies, suspiciousFrequencies);
        double originalNorm = squaredNorm(originalFrequencies);
        double suspiciousNorm = squaredNorm(suspiciousFrequencies);
        double similarity = dotProduct
                / Math.sqrt(originalNorm * suspiciousNorm);
        return Math.max(0.0, Math.min(1.0, similarity));
    }

    private Map<String, Integer> frequencies(String text, int nGramSize) {
        int[] codePoints = text.codePoints().toArray();
        Map<String, Integer> frequencies = new HashMap<>();
        for (int index = 0; index <= codePoints.length - nGramSize; index++) {
            String nGram = new String(codePoints, index, nGramSize);
            Integer previous = frequencies.get(nGram);
            frequencies.put(nGram, previous == null ? 1 : previous + 1);
        }
        return frequencies;
    }

    private double dotProduct(Map<String, Integer> left,
                              Map<String, Integer> right) {
        Map<String, Integer> smaller = left.size() <= right.size() ? left : right;
        Map<String, Integer> larger = left.size() <= right.size() ? right : left;
        double product = 0.0;
        for (Map.Entry<String, Integer> entry : smaller.entrySet()) {
            Integer matchingFrequency = larger.get(entry.getKey());
            if (matchingFrequency != null) {
                product += (double) entry.getValue() * matchingFrequency;
            }
        }
        return product;
    }

    private double squaredNorm(Map<String, Integer> frequencies) {
        double sum = 0.0;
        for (Integer frequency : frequencies.values()) {
            sum += (double) frequency * frequency;
        }
        return sum;
    }
}
