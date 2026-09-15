package com.odenmn.paperchecker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * 仅负责读写命令行传入的文件路径，并统一使用 UTF-8 编码。
 */
final class Utf8FileService {

    String read(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IOException("input file does not exist: " + path);
        }
        byte[] bytes = Files.readAllBytes(path);
        try {
            // 遇到损坏的 UTF-8 字节时直接报错，避免替换字符悄悄改变查重结果。
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException exception) {
            throw new IOException("input file is not valid UTF-8: " + path,
                    exception);
        }
    }

    void write(Path path, String content) throws IOException {
        // 若答案文件已存在则完整覆盖，保证其中只包含本次计算结果。
        Files.write(path, content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
    }
}
