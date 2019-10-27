package ru.r5am;

import java.io.File;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import org.apache.logging.log4j.Logger;
import java.nio.file.NoSuchFileException;
import org.apache.logging.log4j.LogManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

class ReadConfig {

    private static final Logger log = LogManager.getLogger();

    private ReadConfig() {
    }

    /**
     * Вернуть из файла конфигурации бекапов словарь с параметрами бекапов
     * @param backupsConfigFileName Имя YAML-файла конфигурации бекапов
     * @return Словарь параметров бекапов
     * @throws IOException Если ошибки при чтении файла конфигурации бекапов
     */
    static Map<String, Map<String,String>> readConfig(String backupsConfigFileName) throws IOException {

        Map<String, Map<String,String>> backupsConfig;
        InputStream inputStream = null;
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        Path currentDir = ApplicationStartUpPath.getAppStartUpPath();

        try {
            inputStream = Files.newInputStream(Paths.get(currentDir + File.separator + backupsConfigFileName));
        } catch (FileNotFoundException | NoSuchFileException ex) {
            log.error("File '{}' not found", currentDir + "/" + backupsConfigFileName);

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                //  2 - ERROR_FILE_NOT_FOUND (windows)
                System.exit(2);
            } else {
                // 78 - Configuration error (unix)
                System.exit(78);
            }
        }

        backupsConfig = objectMapper.readValue(inputStream, new TypeReference<Map<String, Map<String,String>>>() {
        });
        log.info("Backups YAML-config: {}", backupsConfig);

        return backupsConfig;
    }
}
