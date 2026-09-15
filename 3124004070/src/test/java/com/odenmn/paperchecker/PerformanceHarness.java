package com.odenmn.paperchecker;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * 重复执行真实的相似度计算，为 JProfiler 提供足够的性能采样时间。
 */
public final class PerformanceHarness {

    private static final int ARGUMENT_COUNT = 3;
    private static final long ATTACH_DELAY_MILLIS = 5_000L;

    private PerformanceHarness() {
        // 工具类不需要创建实例。
    }

    /**
     * 在指定秒数内持续运行相似度计算。
     *
     * @param args 原文路径、抄袭版路径和运行秒数
     * @throws Exception 性能分析输入无效或文件读取失败时抛出
     */
    public static void main(String[] args) throws Exception {
        if (args.length != ARGUMENT_COUNT) {
            throw new IllegalArgumentException(
                    "Expected: <original> <suspicious> <duration-seconds>");
        }
        Path originalPath = Paths.get(args[0]);
        Path suspiciousPath = Paths.get(args[1]);
        long durationNanos = Math.multiplyExact(
                Long.parseLong(args[2]), 1_000_000_000L);
        String original = new String(Files.readAllBytes(originalPath),
                StandardCharsets.UTF_8);
        String suspicious = new String(Files.readAllBytes(suspiciousPath),
                StandardCharsets.UTF_8);

        // 预留五秒，方便在循环开始前连接 JProfiler。
        Thread.sleep(ATTACH_DELAY_MILLIS);
        NGramCosineSimilarity calculator = new NGramCosineSimilarity();
        long start = System.nanoTime();
        long deadline = start + durationNanos;
        long iterations = 0L;
        double checksum = 0.0;
        // 按持续时间而非固定次数执行，使不同性能的机器都能获得稳定采样。
        do {
            checksum += calculator.calculate(original, suspicious);
            iterations++;
        } while (System.nanoTime() < deadline);

        double elapsedSeconds = (System.nanoTime() - start) / 1_000_000_000.0;
        System.out.printf(Locale.ROOT,
                "iterations=%d, elapsedSeconds=%.3f, operationsPerSecond=%.2f, checksum=%.4f%n",
                iterations, elapsedSeconds, iterations / elapsedSeconds, checksum);
    }
}
