package com.scr.journal.util;

import com.scr.journal.dao.DataLoader;
import com.scr.journal.dao.DataPersister;
import com.scr.journal.dao.JsonLoader;
import com.scr.journal.dao.JsonPersister;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class SettingsRegistry {

    private static final Path settingsFilePath = Paths.get("settings.json");

    private static final DataLoader<Settings> settingsLoader;
    private static final DataPersister<Settings> settingsPersister;

    static {
        settingsLoader = new JsonLoader<>(settingsFilePath, Settings.class);
        settingsPersister = new JsonPersister<>(settingsFilePath, null);
    }

    public static Locale getLocale() {
        Settings settings = read();
        return new Locale(settings.getLanguage(), settings.getCountry());
    }

    public static ResourceBundle getResourceBundle() {
        return ResourceBundle.getBundle("com/scr/journal/config/ui_lang", getLocale());
    }

    public static void install(Properties properties) {
        if (Files.exists(settingsFilePath)) {
            Settings settings = read();
            properties.setProperty("system.locale.language", settings.getLanguage());
            properties.setProperty("system.locale.country", settings.getCountry());
        } else {
            save(new Settings(
                    properties.getProperty("system.locale.language"),
                    properties.getProperty("system.locale.country")));
        }
    }

    public static Settings read() {
        return settingsLoader.load();
    }

    public static void save(Settings settings) {
        settingsPersister.persist(settings);
    }

    public static class Settings {
        private final String language;
        private final String country;

        public Settings(
                String language,
                String country) {
            this.language = language;
            this.country = country;
        }

        public String getLanguage() {
            return language;
        }

        public String getCountry() {
            return country;
        }
    }

}
