package com.scr.journal.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.scr.journal.controllers.JournalController;
import com.scr.journal.dao.DataLoader;
import com.scr.journal.dao.DataPersister;
import com.scr.journal.dao.JsonLoader;
import com.scr.journal.dao.JsonPersister;
import com.scr.journal.model.Journals;

import java.nio.file.Path;
import java.nio.file.Paths;

public class JournalModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new PropertyModule());
    }

    @Provides
    public Path createJsonFilePath(@Named("json.file.path") String jsonFilePath) {
        return Paths.get(jsonFilePath);
    }

    @Provides
    @Singleton
    public DataLoader<Journals> createJournalLoader(Path filePath) {
        return new JsonLoader<>(filePath, Journals.class);
    }

    @Provides
    @Singleton
    public DataPersister<Journals> createJournalPersister(Path filePath) {
        return new JsonPersister<>(filePath);
    }

    @Provides
    @Singleton
    public JournalController createJournalController(DataLoader<Journals> journalLoader, DataPersister<Journals> journalPersister) {
        return new JournalController(journalLoader, journalPersister);
    }

}
