package ru.r5am;

import org.aeonbits.owner.Config;

@Config.Sources({ "file:jbackuper.config" })
public interface AppConfig extends Config {

    String backupsConfigFileName();

}
