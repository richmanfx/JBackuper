package ru.r5am;

import java.io.*;
import java.util.Map;
import lombok.Cleanup;
import java.util.Date;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import org.apache.logging.log4j.Logger;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;

class Archive {

    private Archive(){}

    private static final Logger log = LogManager.getLogger();
    private static final AppConfig appConfig = ConfigFactory.create(AppConfig.class);

    /**
     * Собрать файлы в TAR-пакет
     * @param backupsConfig Конфигурационные данные бекапов
     */
    static void toTar(Map<String, Map<String, String>> backupsConfig) throws IOException {

        for (Map.Entry<String, Map<String, String>> oneBackupConf : backupsConfig.entrySet()) {
            log.debug("Бэкап: {} => конфиг: {}", oneBackupConf.getKey(), oneBackupConf.getValue());

            String sourceDir = oneBackupConf.getValue().get("From");
            String destFileName = oneBackupConf.getValue().get("OutFileName") + ".tar";

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
     * @param backupsConfig Конфигурационные данные бекапов
     * @throws IOException При ошибказ записи в поток
     */
    static void lzmaArchive(Map<String, Map<String, String>> backupsConfig) throws IOException {



        for (Map.Entry<String, Map<String, String>> oneBackupConf : backupsConfig.entrySet()) {

            // TODO: Здесь раскидать на разные потоки
            final byte[] buffer = new byte[4096];   // TODO: От балды размер пока

            String tarFileName = oneBackupConf.getValue().get("OutFileName") + ".tar";

            String lzmaFileName = getCompressedName(oneBackupConf);

            @Cleanup InputStream inputStream = Files.newInputStream(Paths.get(tarFileName));

            OutputStream outputStream = Files.newOutputStream(Paths.get(lzmaFileName));
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            LZMACompressorOutputStream lzmaOutStream = new LZMACompressorOutputStream(bufferedOutputStream);

            int n;
            while (-1 != (n = inputStream.read(buffer))) {
                lzmaOutStream.write(buffer, 0, n);
            }

            lzmaOutStream.close();

            // Удалить исходный TAR-файл
            Files.delete(Paths.get(tarFileName));

        }
    }

    private static String getCompressedName(Map.Entry<String, Map<String, String>> oneBackupConf) {

        String compressedFileName;

        String withoutExtFileName = oneBackupConf.getValue().get("OutFileName");

        if (oneBackupConf.getValue().get("DateTime").equals("false")) {
            compressedFileName = withoutExtFileName + ".tar.lzma";
        } else {
            SimpleDateFormat formatForDateNow = new SimpleDateFormat(appConfig.dateTimeFormat());
            compressedFileName = withoutExtFileName + "-" + formatForDateNow.format(new Date()) + ".tar.lzma";
        }

        return compressedFileName;
    }

    static void moveArchiveFiles(Map<String, Map<String, String>> backupsConfig) throws IOException {

        for (Map.Entry<String, Map<String, String>> oneBackupConf : backupsConfig.entrySet()) {

            // Исходный файл
            String sourceFileName = getCompressedName(oneBackupConf);
            File sourceFile = new File(sourceFileName);

            // Файл назначения
            String destFileName = oneBackupConf.getValue().get("To") + File.separator + sourceFileName;
            File destFile = new File(destFileName);

            Files.move(sourceFile.toPath(), destFile.toPath());

        }
    }

}
