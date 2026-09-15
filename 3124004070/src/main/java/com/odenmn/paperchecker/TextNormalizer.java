package com.odenmn.paperchecker;

import java.text.Normalizer;
import java.util.Locale;

/**
 * 将文本转换为稳定的字母和数字序列，减少格式差异对查重结果的影响。
 */
final class TextNormalizer {

    String normalize(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }

        // NFKC 会统一全角、半角等兼容字符，再统一大小写。
        String canonical = Normalizer.normalize(text, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);
        StringBuilder normalized = new StringBuilder(canonical.length());
        // 按 Unicode 码点遍历，既过滤标点和空白，也避免拆分增补字符。
        canonical.codePoints()
                .filter(codePoint -> Character.isLetterOrDigit(codePoint))
                .forEach(normalized::appendCodePoint);
        return normalized.toString();
    }
}
