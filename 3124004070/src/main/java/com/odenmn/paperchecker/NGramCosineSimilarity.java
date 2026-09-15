package com.odenmn.paperchecker;

import java.util.HashMap;
import java.util.Map;

/**
 * 根据字符 N 元语法的频次向量计算余弦相似度。
 */
public final class NGramCosineSimilarity {

    private static final int DEFAULT_N_GRAM_SIZE = 2;
    private final TextNormalizer normalizer;

    /**
     * 创建默认使用二元语法的相似度计算器。
     */
    public NGramCosineSimilarity() {
        this(new TextNormalizer());
    }

    NGramCosineSimilarity(TextNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    /**
     * 计算两段论文文本的相似度，结果范围为 0 到 1（包含边界）。
     *
     * @param original 原文内容
     * @param suspicious 待查重文本内容
     * @return 两段文本的余弦相似度
     */
    public double calculate(String original, String suspicious) {
        String normalizedOriginal = normalizer.normalize(original);
        String normalizedSuspicious = normalizer.normalize(suspicious);

        // 空文本没有可构造的特征：两者都为空视为相同，仅一方为空视为不同。
        if (normalizedOriginal.isEmpty() || normalizedSuspicious.isEmpty()) {
            return normalizedOriginal.equals(normalizedSuspicious) ? 1.0 : 0.0;
        }
        if (normalizedOriginal.equals(normalizedSuspicious)) {
            return 1.0;
        }

        // 单字符文本无法生成二元语法，此时自动降级为一元语法。
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
        // 使用码点数组切分，确保一个 Unicode 字符不会被拆成两个 char。
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
        // 只遍历特征较少的向量，并在较大向量中做哈希查找，以减少循环次数。
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
