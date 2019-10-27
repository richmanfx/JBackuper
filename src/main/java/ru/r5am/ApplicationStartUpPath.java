package ru.r5am;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

class ApplicationStartUpPath {

    private ApplicationStartUpPath(){}

    /**
     * Вернуть путь к директории, из которой стартовало приложение
     * @return Путь к директории
     */
    static Path getAppStartUpPath() {
        URL startupUrl = ApplicationStartUpPath.class.getProtectionDomain().getCodeSource().getLocation();
        Path path;

        try {
            path = Paths.get(startupUrl.toURI());
        } catch (Exception e) {
            try {
                path = Paths.get(new URL(startupUrl.getPath()).getPath());
            } catch (Exception ipe) {
                path = Paths.get(startupUrl.getPath());
            }
        }
        return path.getParent();
    }

}
