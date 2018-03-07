package com.scr.journal.util;

import com.scr.journal.dao.DataLoader;
import com.scr.journal.dao.DataPersister;
import com.scr.journal.model.Journal;
import com.scr.journal.model.Journals;

import java.util.Collection;
import java.util.Collections;

public class JournalRegistry {

    private final Collection<Journal> journals;

    private final DataLoader<Journals> journalLoader;
    private final DataPersister<Journals> journalPersister;

    public JournalRegistry(DataLoader<Journals> journalLoader, DataPersister<Journals> journalPersister) {
        this.journals = journalLoader.load().getJournals();
        this.journalLoader = journalLoader;
        this.journalPersister = journalPersister;
    }

    public Collection<Journal> getJournals() {
        return Collections.unmodifiableCollection(journals);
    }

    public void add(Journal journal) {
        journals.add(journal);
        persist();
    }

    public void remove(Journal journal) {
        journals.remove(journal);
        persist();
    }

    private void persist() {
        journalPersister.persist(Journals.from(journals));
    }

}
