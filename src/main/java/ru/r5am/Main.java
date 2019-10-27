package ru.r5am;

import java.util.Map;
import java.io.IOException;
import org.aeonbits.owner.ConfigFactory;

import static ru.r5am.Archive.*;
import static ru.r5am.ReadConfig.readConfig;

public class Main {

    private static final AppConfig appConfig = ConfigFactory.create(AppConfig.class);

    public static void main(String[] args) throws IOException {

        // Считать конфигурационные данные
        Map<String, Map<String,String>> backupsConfig = readConfig(appConfig.backupsConfigFileName());

        // Собрать TAR
        toTar(backupsConfig);

        // Архивировать
        lzmaArchive(backupsConfig);

    }

}
