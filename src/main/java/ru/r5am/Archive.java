package ru.r5am;

import java.io.*;
import java.util.Map;
import lombok.Cleanup;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;

class Archive {

    private Archive(){}

    private static final Logger log = LogManager.getLogger();
    private static Path currentDir = ApplicationStartUpPath.getAppStartUpPath();

    /**
     * Собрать файлы в TAR-пакет
     * @param backupsConfig Конфигурационные данные бекапов
     */
    static void toTar(Map<String, Map<String, String>> backupsConfig) throws IOException {

        for (Map.Entry<String, Map<String, String>> oneBackupConf : backupsConfig.entrySet()) {
            log.debug("Бэкап: {} => конфиг: {}", oneBackupConf.getKey(), oneBackupConf.getValue());

            String sourceDir = oneBackupConf.getValue().get("From");
            String destFileName =
                    oneBackupConf.getValue().get("To") + File.separator + oneBackupConf.getValue().get("FileName");

            // Кастомизированный выходной TAR-поток
            TarArchiveOutputStream out = getTarArchiveOutputStream(destFileName);

            // Добавляем в TAR
            addToTarArchive(out, new File(sourceDir), ".");

        }

    }

    /**
     * Добавляет рекурсивно в TAR-поток все файлы и директории из указанной директории
     * @param tarOutStream Выходной TAR-поток
     * @param file Файл или директория, добавляемая в TAR-архив
     * @param parentDir Родительская директория TAR-архива
     * @throws IOException При ошибках вывода в поток
     */
    private static void addToTarArchive(TarArchiveOutputStream tarOutStream, File file, String parentDir)
            throws IOException {

        String entry = parentDir + File.separator + file.getName();

        if (file.isFile()){     // Если файл
            tarOutStream.putArchiveEntry(new TarArchiveEntry(file, entry));
            try (FileInputStream in = new FileInputStream(file)){
                IOUtils.copy(in, tarOutStream);
            }
            tarOutStream.closeArchiveEntry();
        } else if (file.isDirectory()) {        // Если директория
            File[] children = file.listFiles();     // Файлы-дети в директории
            if (children != null){
                for (File child : children){
                    addToTarArchive(tarOutStream, child, entry);    // Рекурсия
                }
            }
        } else {
            log.error("Not directory and not file - '{}'", file);
        }

    }

    /**
     * Костомизация TarArchiveOutputStream - размер более 8Гб, длинные имена, кириллические имена
     * @param name Имя TAR-файла
     * @return Кастомизированный "TarArchiveOutputStream"
     * @throws IOException При ошибках создания файлового выходного потока
     */
    private static TarArchiveOutputStream getTarArchiveOutputStream(String name) throws FileNotFoundException {

        TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(new FileOutputStream(name));

        // Обойти ограничение TAR в 8 Гб
        tarArchiveOutputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);

        // Поддержка длинных имён файлов
        tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        // Поддержка кириллических имён файлов
        tarArchiveOutputStream.setAddPaxHeadersForNonAsciiNames(true);

        return tarArchiveOutputStream;
    }


    /**
     * Сжать файл с алгоритмом LZMA
     * @param backupConfig Конфигурационные данные бекапов
     * @throws IOException При ошибказ записи в поток
     */
    static void lzmaArchive(Map<String, Map<String, String>> backupConfig) throws IOException {

        int bufferSize = 4096;
        final byte[] buffer = new byte[bufferSize];
//        Path currentDir = ApplicationStartUpPath.getAppStartUpPath();
        log.info("Start!");
        @Cleanup InputStream inputStream = Files.newInputStream(Paths.get(currentDir.toString(), "Музыка.tar"));
        OutputStream outputStream = Files.newOutputStream(Paths.get(currentDir.toString(), "Музыка.tar.lzma"));
        BufferedOutputStream out = new BufferedOutputStream(outputStream);
        LZMACompressorOutputStream lzOut = new LZMACompressorOutputStream(out);

        int n = 0;
        while (-1 != (n = inputStream.read(buffer))) {
            lzOut.write(buffer, 0, n);
        }

        lzOut.close();
        log.info("Готово!");
    }

}
