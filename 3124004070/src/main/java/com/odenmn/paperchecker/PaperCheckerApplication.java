package com.odenmn.paperchecker;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Coordinates argument validation, file access and similarity calculation.
 */
public final class PaperCheckerApplication {

    static final int SUCCESS = 0;
    static final int INVALID_ARGUMENTS = 2;
    static final int FILE_ERROR = 3;
    private static final int EXPECTED_ARGUMENT_COUNT = 3;

    private final Utf8FileService fileService;
    private final NGramCosineSimilarity similarityCalculator;

    /**
     * Creates an application with the production components.
     */
    public PaperCheckerApplication() {
        this(new Utf8FileService(), new NGramCosineSimilarity());
    }

    PaperCheckerApplication(Utf8FileService fileService,
                            NGramCosineSimilarity similarityCalculator) {
        this.fileService = fileService;
        this.similarityCalculator = similarityCalculator;
    }

    /**
     * Executes one plagiarism-checking request.
     *
     * @param args three absolute file paths
     * @param errorStream destination for concise error messages
     * @return process-compatible exit code
     */
    public int run(String[] args, PrintStream errorStream) {
        if (args == null || args.length != EXPECTED_ARGUMENT_COUNT) {
            errorStream.println(
                    "Usage: java -jar main.jar <original> <suspicious> <answer>");
            return INVALID_ARGUMENTS;
        }

        try {
            Path originalPath = toAbsolutePath(args[0], "original");
            Path suspiciousPath = toAbsolutePath(args[1], "suspicious");
            Path answerPath = toAbsolutePath(args[2], "answer");
            ensureDistinctAnswerPath(originalPath, suspiciousPath, answerPath);

            String originalText = fileService.read(originalPath);
            String suspiciousText = fileService.read(suspiciousPath);
            double similarity = similarityCalculator.calculate(
                    originalText, suspiciousText);
            fileService.write(answerPath,
                    String.format(Locale.ROOT, "%.2f", similarity));
            return SUCCESS;
        } catch (IllegalArgumentException exception) {
            errorStream.println("Invalid argument: " + exception.getMessage());
            return INVALID_ARGUMENTS;
        } catch (IOException exception) {
            errorStream.println("File error: " + exception.getMessage());
            return FILE_ERROR;
        }
    }

    private Path toAbsolutePath(String rawPath, String argumentName) {
        if (rawPath == null || rawPath.trim().isEmpty()) {
            throw new IllegalArgumentException(argumentName + " path is empty");
        }
        Path path = Paths.get(rawPath).normalize();
        if (!path.isAbsolute()) {
            throw new IllegalArgumentException(
                    argumentName + " path must be absolute: " + rawPath);
        }
        return path;
    }

    private void ensureDistinctAnswerPath(Path originalPath,
                                          Path suspiciousPath,
                                          Path answerPath) {
        if (answerPath.equals(originalPath) || answerPath.equals(suspiciousPath)) {
            throw new IllegalArgumentException(
                    "answer path must differ from both input paths");
        }
    }
}
