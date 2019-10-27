package ru.r5am;

import java.util.Map;
import lombok.Cleanup;
import java.nio.file.Path;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;

class Archive {

    private Archive(){}

    static void lzmaArchive(Map<String, Map<String, String>> backupConfig) throws IOException {

        int bufferSize = 4096;
        final byte[] buffer = new byte[bufferSize];
        Path currentDir = ApplicationStartUpPath.getAppStartUpPath();
        @Cleanup InputStream inputStream = Files.newInputStream(Paths.get(currentDir.toString(), "archive.tar"));
        OutputStream outputStream = Files.newOutputStream(Paths.get(currentDir.toString(), "archive.tar.lzma"));
        BufferedOutputStream out = new BufferedOutputStream(outputStream);
        LZMACompressorOutputStream lzOut = new LZMACompressorOutputStream(out);

        int n = 0;
        while (-1 != (n = inputStream.read(buffer))) {
            lzOut.write(buffer, 0, n);
        }

        lzOut.close();
    }

}
