package ru.r5am;

import java.util.Map;
import java.io.IOException;
import org.aeonbits.owner.ConfigFactory;

import static ru.r5am.ReadConfig.readConfig;

public class Main {

    private static final AppConfig appConfig = ConfigFactory.create(AppConfig.class);

    public static void main(String[] args) throws IOException {

        // Считать конфигурационные данные
        Map<String, Map<String,String>> backupConfig = readConfig(appConfig.backupsConfigFileName());

        // Архивировать

    }


}
