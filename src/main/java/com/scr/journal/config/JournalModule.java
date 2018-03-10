package com.scr.journal.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.scr.journal.controllers.JournalController;
import com.scr.journal.dao.CsvLoader;
import com.scr.journal.dao.ExcelWriter;
import com.scr.journal.dao.JsonLoader;
import com.scr.journal.dao.JsonPersister;
import com.scr.journal.model.Journals;
import com.scr.journal.util.JournalRegistry;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class JournalModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new PropertyModule());
    }

    @Provides
    @Singleton
    public Locale getLocale(
            @Named("system.locale.language") String localeLanguage,
            @Named("system.locale.country") String localeCountry) {
        return new Locale(localeLanguage, localeCountry);
    }

    @Provides
    @Singleton
    public ResourceBundle getResourceBundle(Locale locale) {
        return ResourceBundle.getBundle("com/scr/journal/config/ui_lang", locale);
    }

    @Provides
    @Named("json_file_path")
    public Path createJsonFilePath(@Named("json.file.path") String jsonFilePath) {
        return Paths.get(jsonFilePath);
    }

    @Provides
    @Named("backup_file_path")
    public Path createBackupFilePath(@Named("backup.file.path") String jsonFilePath) {
        return Paths.get(jsonFilePath);
    }

    @Provides
    @Singleton
    public JsonLoader<Journals> createJournalLoader(@Named("json_file_path")Path filePath) {
        return new JsonLoader<>(filePath, Journals.class);
    }

    @Provides
    @Singleton
    public JsonPersister<Journals> createJournalPersister(
            @Named("json_file_path") Path filePath,
            @Named("backup_file_path") Path backupPath) {
        return new JsonPersister<>(filePath, backupPath);
    }

    @Provides
    @Singleton
    public CsvLoader createCsvLoader(@Named("system.character.encoding") String charset) {
        return new CsvLoader(charset);
    }

    @Provides
    @Singleton
    public JournalController createJournalController(
            ResourceBundle resourceBundle,
            JournalRegistry journalRegistry,
            CsvLoader csvLoader,
            ExcelWriter excelWriter,
            NumberFormat numberFormat) {
        return new JournalController(resourceBundle, journalRegistry, csvLoader, excelWriter, numberFormat);
    }

    @Provides
    @Singleton
    public JournalRegistry createJournalRegistry(
            JsonLoader<Journals> journalLoader,
            JsonPersister<Journals> journalPersister) {
        return new JournalRegistry(journalLoader, journalPersister);
    }

    @Provides
    @Singleton
    public NumberFormat getNumberFormat(
            @Named("system.number_format.language") String numberFormatLanguage,
            @Named("system.number_format.country") String numberFormatCountry) {
        return NumberFormat.getCurrencyInstance(new Locale(numberFormatLanguage, numberFormatCountry));
    }

    @Provides
    @Singleton
    public ExcelWriter createExcelWriter(ResourceBundle resourceBundle) {
        return new ExcelWriter(resourceBundle);
    }

}
