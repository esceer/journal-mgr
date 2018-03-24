package com.scr.journal.util;

import com.scr.journal.dao.DataLoader;
import com.scr.journal.dao.DataPersister;
import com.scr.journal.dao.JsonLoader;
import com.scr.journal.dao.JsonPersister;

import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public final class SettingsRegistry {

    private static SettingsRegistry INSTANCE;

    private final DataLoader<Settings> settingsLoader;
    private final DataPersister<Settings> settingsPersister;

    private SoftReference<Settings> settingsRef;

    private SettingsRegistry(Path settingsFilePath) {
        this.settingsLoader = new JsonLoader<>(settingsFilePath, Settings.class);
        this.settingsPersister = new JsonPersister<>(settingsFilePath, null);
        this.settingsRef = new SoftReference<>(null);
    }

    public static void install(Properties properties) {
        Path settingsFilePath = Paths.get(properties.getProperty("settings.file.path"));
        INSTANCE = new SettingsRegistry(settingsFilePath);

        if (Files.exists(settingsFilePath)) {
            Settings settings = get().read();
            properties.setProperty("system.locale.language", settings.language);
            properties.setProperty("system.locale.country", settings.country);
            properties.setProperty("system.search_date_format", ConversionUtils.convert(settings.searchDateFormat));
        } else {
            get().save(new Settings(
                    properties.getProperty("system.locale.language"),
                    properties.getProperty("system.locale.country"),
                    ConversionUtils.convert(properties.getProperty("system.search_date_format"), SearchDateFormat.class)));
        }
    }

    public static SettingsRegistry get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("SettingsRegistry must be configured before its use. Ensure you call 'install' first.");
        }
        return INSTANCE;
    }

    private void invalidate() {
        settingsRef.clear();
    }

    public Settings read() {
        if (settingsRef.get() == null) {
            settingsRef = new SoftReference<>(settingsLoader.load());
        }
        return settingsRef.get();
    }

    public void save(Settings settings) {
        settingsPersister.persist(settings);
        invalidate();
    }

    public Locale getLocale() {
        Settings settings = read();
        return new Locale(settings.language, settings.country);
    }

    public void setLocale(String language, String country) {
        Settings origSettings = read();
        save(new Settings(language, country, origSettings.searchDateFormat));
        invalidate();
    }

    public ResourceBundle getResourceBundle() {
        return ResourceBundle.getBundle("com/scr/journal/config/ui_lang", getLocale());
    }

    public SearchDateFormat getSearchDateFormat() {
        Settings settings = read();
        return settings.searchDateFormat;
    }

    public void setSearchDateFormat(SearchDateFormat searchDateFormat) {
        Settings origSettings = read();
        save(new Settings(origSettings.language, origSettings.country, searchDateFormat));
        invalidate();
    }

    public static class Settings {
        private final String language;
        private final String country;
        private final SearchDateFormat searchDateFormat;

        public Settings(
                String language,
                String country,
                SearchDateFormat searchDateFormat) {
            this.language = language;
            this.country = country;
            this.searchDateFormat = searchDateFormat;
        }
    }

}
