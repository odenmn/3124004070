package com.odenmn.paperchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NGramCosineSimilarityTest {

    private static final double TOLERANCE = 1.0e-9;
    private final NGramCosineSimilarity calculator =
            new NGramCosineSimilarity();

    @Test
    void identicalChineseTextHasFullSimilarity() {
        assertEquals(1.0, calculator.calculate("今天天气晴朗", "今天天气晴朗"),
                TOLERANCE);
    }

    @Test
    void punctuationWhitespaceAndLatinCaseAreIgnored() {
        assertEquals(1.0, calculator.calculate("Hello，世界！", " hello世界 "),
                TOLERANCE);
    }

    @Test
    void compatibilityCharactersAreNormalized() {
        assertEquals(1.0, calculator.calculate("ＡＢＣ１２３", "abc123"),
                TOLERANCE);
    }

    @Test
    void unrelatedTextHasZeroSimilarity() {
        assertEquals(0.0, calculator.calculate("甲乙丙丁", "天地玄黄"),
                TOLERANCE);
    }

    @Test
    void partialBigramOverlapHasExpectedCosine() {
        assertEquals(2.0 / 3.0, calculator.calculate("abcd", "abce"),
                TOLERANCE);
    }

    @Test
    void repeatedBigramsAreWeightedByFrequency() {
        double expected = 6.0 / Math.sqrt(45.0);
        assertEquals(expected, calculator.calculate("aaaa", "aaab"),
                TOLERANCE);
    }

    @Test
    void twoEmptyNormalizedTextsHaveFullSimilarity() {
        assertEquals(1.0, calculator.calculate("，。！", " \r\n"), TOLERANCE);
    }

    @Test
    void onlyOneEmptyNormalizedTextHasZeroSimilarity() {
        assertEquals(0.0, calculator.calculate("中文", "，。！"), TOLERANCE);
    }

    @Test
    void differentSingleCharactersUseUnigrams() {
        assertEquals(0.0, calculator.calculate("甲", "乙"), TOLERANCE);
    }

    @Test
    void nullOriginalTextIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculate(null, "文本"));
    }

    @Test
    void nullSuspiciousTextIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculate("文本", null));
    }
}
