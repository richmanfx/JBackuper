package ru.r5am;

import org.aeonbits.owner.Config;

@Config.Sources({ "file:jbackuper.config" })
public interface AppConfig extends Config {

    // Имя файла конфигурации бекапов
    String backupsConfigFileName();

    // Используемое количество потоков при компресии
    int threadsCount();

    // Формат добавляемый к имени архива даты и времени
    String dateTimeFormat();

}
