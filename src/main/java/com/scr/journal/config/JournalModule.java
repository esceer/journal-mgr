package com.scr.journal.config;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.scr.journal.UILoader;
import com.scr.journal.annotations.*;
import com.scr.journal.config.provider.FXMLLoaderProvider;
import com.scr.journal.controllers.JournalController;
import com.scr.journal.dao.*;
import com.scr.journal.model.Journals;
import com.scr.journal.util.JournalRegistry;
import com.scr.journal.util.SettingsRegistry;
import javafx.fxml.FXMLLoader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Locale;

public class JournalModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new PropertyModule());

        bind(FXMLLoader.class).toProvider(FXMLLoaderProvider.class);
        bind(DataBackupHandler.class)
                .annotatedWith(JournalPersister.class)
                .to(Key.get(new TypeLiteral<JsonPersister<Journals>>() {}, JournalPersister.class));
        bind(Key.get(new TypeLiteral<DataPersister<Journals>>() {}, JournalPersister.class))
                .to(Key.get(new TypeLiteral<JsonPersister<Journals>>() {}, JournalPersister.class));
        bind(Key.get(new TypeLiteral<DataLoader<Journals>>() {}, JournalLoader.class))
                .to(Key.get(new TypeLiteral<JsonLoader<Journals>>() {}, JournalLoader.class));
        bind(Key.get(new TypeLiteral<DataLoader<Journals>>() {}, BackupLoader.class))
                .to(Key.get(new TypeLiteral<JsonLoader<Journals>>() {}, BackupLoader.class));
    }

    @Provides
    @Singleton
    public UILoader createUILoader(FXMLLoader fxmlLoader) {
        fxmlLoader.setResources(SettingsRegistry.getResourceBundle());
        return new UILoader(fxmlLoader);
    }

    @Provides
    @JsonFilePath
    public Path createJsonFilePath(@Named("json.file.path") String jsonFilePath) {
        return Paths.get(jsonFilePath);
    }

    @Provides
    @BackupFilePath
    public Path createBackupFilePath(@Named("backup.file.path") String jsonFilePath) {
        return Paths.get(jsonFilePath);
    }

    @Provides
    @Singleton
    @JournalLoader
    public JsonLoader<Journals> createJournalLoader(@JsonFilePath Path filePath) {
        return new JsonLoader<>(filePath, Journals.class);
    }

    @Provides
    @Singleton
    @BackupLoader
    public JsonLoader<Journals> createBackupLoader(@BackupFilePath Path filePath) {
        return new JsonLoader<>(filePath, Journals.class);
    }

    @Provides
    @Singleton
    @JournalPersister
    public JsonPersister<Journals> createJournalPersister(
            @JsonFilePath Path filePath,
            @BackupFilePath Path backupPath) {
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
            UILoader uiLoader,
            JournalRegistry journalRegistry,
            CsvLoader csvLoader,
            ExcelWriter excelWriter,
            NumberFormat numberFormat) {
        return new JournalController(
                uiLoader,
                journalRegistry,
                csvLoader,
                excelWriter,
                numberFormat);
    }

    @Provides
    @Singleton
    public JournalRegistry createJournalRegistry(
            @JournalLoader DataLoader<Journals> journalLoader,
            @JournalPersister DataPersister<Journals> journalPersister,
            @BackupLoader DataLoader<Journals> backupLoader) {
        return new JournalRegistry(journalLoader, journalPersister, backupLoader);
    }

    @Provides
    @Singleton
    public ExcelWriter createExcelWriter() {
        return new ExcelWriter(SettingsRegistry.getResourceBundle());
    }

    @Provides
    @Singleton
    public NumberFormat getNumberFormat(
            @Named("system.number_format.language") String numberFormatLanguage,
            @Named("system.number_format.country") String numberFormatCountry) {
        return NumberFormat.getCurrencyInstance(new Locale(numberFormatLanguage, numberFormatCountry));
    }

}
