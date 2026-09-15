package com.odenmn.paperchecker;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * 负责协调参数校验、文件读写和相似度计算。
 */
public final class PaperCheckerApplication {

    static final int SUCCESS = 0;
    static final int INVALID_ARGUMENTS = 2;
    static final int FILE_ERROR = 3;
    private static final int EXPECTED_ARGUMENT_COUNT = 3;

    private final Utf8FileService fileService;
    private final NGramCosineSimilarity similarityCalculator;

    /**
     * 使用正式的文件服务和相似度计算器创建应用程序。
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
     * 执行一次完整的论文查重任务。
     *
     * @param args 三个文件的绝对路径
     * @param errorStream 简洁错误信息的输出位置
     * @return 可供操作系统识别的退出码
     */
    public int run(String[] args, PrintStream errorStream) {
        if (args == null || args.length != EXPECTED_ARGUMENT_COUNT) {
            errorStream.println(
                    "Usage: java -jar main.jar <original> <suspicious> <answer>");
            return INVALID_ARGUMENTS;
        }

        try {
            // 先校验全部路径，避免处理到一半时才发现参数无效。
            Path originalPath = toAbsolutePath(args[0], "original");
            Path suspiciousPath = toAbsolutePath(args[1], "suspicious");
            Path answerPath = toAbsolutePath(args[2], "answer");
            ensureDistinctAnswerPath(originalPath, suspiciousPath, answerPath);

            // 文件访问严格限定在命令行提供的三个路径内。
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
        // 禁止答案文件覆盖任一输入文件，确保异常情况下原始数据也不会丢失。
        if (answerPath.equals(originalPath) || answerPath.equals(suspiciousPath)) {
            throw new IllegalArgumentException(
                    "answer path must differ from both input paths");
        }
    }
}
