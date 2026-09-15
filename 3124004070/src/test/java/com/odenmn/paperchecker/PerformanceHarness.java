package com.odenmn.paperchecker;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Repeats the real calculation long enough for a profiler to collect samples.
 */
public final class PerformanceHarness {

    private static final int ARGUMENT_COUNT = 3;
    private static final long ATTACH_DELAY_MILLIS = 5_000L;

    private PerformanceHarness() {
        // Utility class.
    }

    /**
     * Runs calculations for the requested number of seconds.
     *
     * @param args original path, suspicious path and duration in seconds
     * @throws Exception when a profiling input is invalid
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

        Thread.sleep(ATTACH_DELAY_MILLIS);
        NGramCosineSimilarity calculator = new NGramCosineSimilarity();
        long start = System.nanoTime();
        long deadline = start + durationNanos;
        long iterations = 0L;
        double checksum = 0.0;
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
