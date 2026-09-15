package com.odenmn.paperchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PaperCheckerApplicationTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void validFilesProduceTwoDecimalAnswer() throws Exception {
        Path original = writeText("original.txt", "今天天气晴朗");
        Path suspicious = writeText("suspicious.txt", "今天天气晴朗");
        Path answer = temporaryDirectory.resolve("answer.txt");

        RunResult result = run(original.toString(), suspicious.toString(),
                answer.toString());

        assertEquals(PaperCheckerApplication.SUCCESS, result.exitCode);
        assertEquals("1.00", new String(Files.readAllBytes(answer),
                StandardCharsets.UTF_8));
        assertTrue(result.errorMessage.isEmpty());
    }

    @Test
    void wrongArgumentCountPrintsUsageAndDoesNotWriteFile() {
        Path answer = temporaryDirectory.resolve("answer.txt");

        RunResult result = run("only-one-argument");

        assertEquals(PaperCheckerApplication.INVALID_ARGUMENTS,
                result.exitCode);
        assertTrue(result.errorMessage.contains("Usage:"));
        assertFalse(Files.exists(answer));
    }

    @Test
    void nullArgumentsAreRejected() {
        ByteArrayOutputStream errorBytes = new ByteArrayOutputStream();
        PrintStream errorStream = new PrintStream(errorBytes);

        int exitCode = new PaperCheckerApplication().run(null, errorStream);

        assertEquals(PaperCheckerApplication.INVALID_ARGUMENTS, exitCode);
    }

    @Test
    void relativeInputPathIsRejected() {
        RunResult result = run("relative.txt", "another.txt", "answer.txt");

        assertEquals(PaperCheckerApplication.INVALID_ARGUMENTS,
                result.exitCode);
        assertTrue(result.errorMessage.contains("must be absolute"));
    }

    @Test
    void emptyPathIsRejected() {
        RunResult result = run(" ", temporaryDirectory.resolve("b.txt").toString(),
                temporaryDirectory.resolve("answer.txt").toString());

        assertEquals(PaperCheckerApplication.INVALID_ARGUMENTS,
                result.exitCode);
        assertTrue(result.errorMessage.contains("path is empty"));
    }

    @Test
    void answerCannotOverwriteAnInputFile() throws Exception {
        Path original = writeText("original.txt", "不能覆盖的内容");
        Path suspicious = writeText("suspicious.txt", "其他内容");

        RunResult result = run(original.toString(), suspicious.toString(),
                original.toString());

        assertEquals(PaperCheckerApplication.INVALID_ARGUMENTS,
                result.exitCode);
        assertTrue(result.errorMessage.contains("answer path must differ"));
        assertEquals("不能覆盖的内容", new String(Files.readAllBytes(original),
                StandardCharsets.UTF_8));
    }

    @Test
    void missingInputFileReturnsFileError() throws Exception {
        Path existing = writeText("existing.txt", "文本");
        Path missing = temporaryDirectory.resolve("missing.txt");
        Path answer = temporaryDirectory.resolve("answer.txt");

        RunResult result = run(missing.toString(), existing.toString(),
                answer.toString());

        assertEquals(PaperCheckerApplication.FILE_ERROR, result.exitCode);
        assertTrue(result.errorMessage.contains("does not exist"));
        assertFalse(Files.exists(answer));
    }

    @Test
    void malformedUtf8ReturnsFileError() throws Exception {
        Path malformed = temporaryDirectory.resolve("malformed.txt");
        Files.write(malformed, new byte[]{(byte) 0xC3, (byte) 0x28});
        Path existing = writeText("existing.txt", "文本");
        Path answer = temporaryDirectory.resolve("answer.txt");

        RunResult result = run(malformed.toString(), existing.toString(),
                answer.toString());

        assertEquals(PaperCheckerApplication.FILE_ERROR, result.exitCode);
        assertTrue(result.errorMessage.contains("not valid UTF-8"));
        assertFalse(Files.exists(answer));
    }

    @Test
    void missingOutputDirectoryReturnsFileError() throws Exception {
        Path original = writeText("original.txt", "原文");
        Path suspicious = writeText("suspicious.txt", "抄袭文本");
        Path answer = temporaryDirectory.resolve("missing").resolve("answer.txt");

        RunResult result = run(original.toString(), suspicious.toString(),
                answer.toString());

        assertEquals(PaperCheckerApplication.FILE_ERROR, result.exitCode);
        assertTrue(result.errorMessage.contains("File error:"));
    }

    @Test
    void emptyInputFilesProduceFullSimilarity() throws Exception {
        Path original = writeText("original.txt", "");
        Path suspicious = writeText("suspicious.txt", "");
        Path answer = temporaryDirectory.resolve("answer.txt");

        RunResult result = run(original.toString(), suspicious.toString(),
                answer.toString());

        assertEquals(PaperCheckerApplication.SUCCESS, result.exitCode);
        assertEquals("1.00", new String(Files.readAllBytes(answer),
                StandardCharsets.UTF_8));
    }

    private Path writeText(String name, String content) throws Exception {
        Path path = temporaryDirectory.resolve(name);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        return path;
    }

    private RunResult run(String... arguments) {
        ByteArrayOutputStream errorBytes = new ByteArrayOutputStream();
        PrintStream errorStream = new PrintStream(errorBytes);
        int exitCode = new PaperCheckerApplication().run(arguments, errorStream);
        errorStream.flush();
        return new RunResult(exitCode,
                new String(errorBytes.toByteArray(), StandardCharsets.UTF_8));
    }

    private static final class RunResult {
        private final int exitCode;
        private final String errorMessage;

        private RunResult(int exitCode, String errorMessage) {
            this.exitCode = exitCode;
            this.errorMessage = errorMessage;
        }
    }
}
