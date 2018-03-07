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

public class JournalModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new PropertyModule());
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
    public CsvLoader createCsvLoader(@Named("character.encoding") String charset) {
        return new CsvLoader(charset);
    }

    @Provides
    @Singleton
    public JournalController createJournalController(
            JournalRegistry journalRegistry,
            CsvLoader csvLoader,
            ExcelWriter excelWriter) {
        return new JournalController(journalRegistry, csvLoader, excelWriter);
    }

    @Provides
    @Singleton
    public JournalRegistry createJournalRegistry(
            JsonLoader<Journals> journalLoader,
            JsonPersister<Journals> journalPersister) {
        return new JournalRegistry(journalLoader, journalPersister);
    }

}
